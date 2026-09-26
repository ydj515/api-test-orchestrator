package com.example.buildlogic;

import com.github.spotbugs.snom.SpotBugsExtension;
import com.github.spotbugs.snom.SpotBugsTask;
import org.gradle.api.Project;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.Pmd;
import org.gradle.api.plugins.quality.PmdExtension;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.testing.Test;
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension;
import org.gradle.testing.jacoco.tasks.JacocoReport;
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification;
import java.io.File;
import java.math.BigDecimal;
import java.util.List;

/** Shared, failing quality gates for both aggregate and standalone module builds. */
final class JavaQuality {
    private JavaQuality() { }

    static void configure(Project project, String basePackage) {
        File root = project.getRootDir();
        if (!new File(root, "config/checkstyle/checkstyle.xml").isFile()) {
            root = root.getParentFile();
        }
        File config = new File(root, "config");
        VersionCatalog libs = project.getExtensions().getByType(VersionCatalogsExtension.class).named("libs");
        String spotbugsVersion = libs.findVersion("spotbugs-engine").orElseThrow().getRequiredVersion();
        // Imported application BOMs can downgrade the analyzer's own annotation API.
        project.getPluginManager().withPlugin("io.spring.dependency-management", ignored ->
                project.getConfigurations().configureEach(configuration ->
                        configuration.getResolutionStrategy().eachDependency(dependency -> {
                            if (dependency.getRequested().getGroup().equals("com.github.spotbugs")
                                    && dependency.getRequested().getName().equals("spotbugs-annotations")) {
                                dependency.useVersion(spotbugsVersion);
                                dependency.because("Keep SpotBugs annotations compatible with the pinned analyzer");
                            }
                        })));
        for (String plugin : List.of("checkstyle", "pmd", "com.github.spotbugs", "jacoco")) {
            project.getPluginManager().apply(plugin);
        }
        var checkstyle = project.getExtensions().getByType(CheckstyleExtension.class);
        checkstyle.setToolVersion(libs.findVersion("checkstyle").orElseThrow().getRequiredVersion());
        checkstyle.setConfigFile(new File(config, "checkstyle/checkstyle.xml"));
        checkstyle.setIgnoreFailures(false);
        checkstyle.setMaxWarnings(0);
        project.getTasks().withType(Checkstyle.class).configureEach(task -> {
            task.getReports().getXml().getRequired().set(true);
            task.getReports().getHtml().getRequired().set(true);
        });
        var pmd = project.getExtensions().getByType(PmdExtension.class);
        pmd.setToolVersion(libs.findVersion("pmd").orElseThrow().getRequiredVersion());
        pmd.setRuleSets(List.of());
        pmd.setRuleSetFiles(project.files(new File(config, "pmd/ruleset.xml")));
        pmd.setIgnoreFailures(false);
        pmd.setConsoleOutput(true);
        project.getTasks().withType(Pmd.class).configureEach(task -> {
            task.getReports().getXml().getRequired().set(true);
            task.getReports().getHtml().getRequired().set(true);
        });
        var spotbugs = project.getExtensions().getByType(SpotBugsExtension.class);
        spotbugs.getToolVersion().set(spotbugsVersion);
        spotbugs.getIgnoreFailures().set(false);
        spotbugs.getExcludeFilter().set(new File(config, "spotbugs/exclude-filter.xml"));
        project.getTasks().withType(SpotBugsTask.class).configureEach(task -> {
            task.getReports().create("html").getRequired().set(true);
            task.getReports().create("xml").getRequired().set(true);
        });
        project.getDependencies().add("testImplementation", libs.findLibrary("archunit").orElseThrow());
        project.getDependencies().add("compileOnly", libs.findLibrary("spotbugs-annotations").orElseThrow());
        project.getDependencies().add("testCompileOnly", libs.findLibrary("spotbugs-annotations").orElseThrow());
        var sources = project.getExtensions().getByType(SourceSetContainer.class);
        sources.getByName("test").getJava().srcDir(new File(config, "architecture/src/test/java"));
        var architecture = project.getTasks().register("archUnitTest", Test.class, task -> {
            task.setGroup("verification");
            task.setDescription("Verify compiled architecture rules and rule failure fixtures with ArchUnit");
            task.setTestClassesDirs(sources.getByName("test").getOutput().getClassesDirs());
            task.setClasspath(sources.getByName("test").getRuntimeClasspath());
            task.getFilter().includeTestsMatching("architecture.*ArchitectureTest");
            task.systemProperty("architecture.basePackage", basePackage);
            task.useJUnitPlatform();
        });
        project.getTasks().named("test", Test.class, task -> {
            task.getFilter().excludeTestsMatching("architecture.*ArchitectureTest");
        });
        project.getTasks().named("architectureTest", task -> task.dependsOn(architecture));
        var jacoco = project.getExtensions().getByType(JacocoPluginExtension.class);
        jacoco.setToolVersion(libs.findVersion("jacoco").orElseThrow().getRequiredVersion());
        var report = project.getTasks().named("jacocoTestReport", JacocoReport.class, task -> {
            task.dependsOn(project.getTasks().named("test"));
            task.getReports().getXml().getRequired().set(true);
            task.getReports().getHtml().getRequired().set(true);
        });
        if (!project.getName().equals("karate-tests")) {
            var coverageInputs = project.getTasks().register("verifyCoverageInputs", CoverageInputsCheck.class, task -> {
                task.setGroup("verification");
                task.dependsOn(project.getTasks().named("test"));
                task.getExecutionData().set(project.getLayout().getBuildDirectory().file("jacoco/test.exec"));
            });
            report.configure(task -> task.dependsOn(coverageInputs));
            var coverage = project.getTasks().named("jacocoTestCoverageVerification", JacocoCoverageVerification.class, task -> {
                task.dependsOn(report);
                String[] minimums = switch (project.getName()) {
                    case "gateway" -> new String[]{"0.82", "0.67"};
                    case "mock-rest-api-server" -> new String[]{"0.59", "0.35"};
                    case "report-server" -> new String[]{"0.72", "0.53"};
                    default -> throw new IllegalArgumentException("Coverage floors required for " + project.getName());
                };
                task.getViolationRules().rule(rule -> {
                    rule.limit(limit -> {
                        limit.setCounter("LINE");
                        limit.setMinimum(new BigDecimal(minimums[0]));
                    });
                    rule.limit(limit -> {
                        limit.setCounter("BRANCH");
                        limit.setMinimum(new BigDecimal(minimums[1]));
                    });
                });
            });
            project.getTasks().named("check", task -> task.dependsOn(coverage));
        }
    }
}
