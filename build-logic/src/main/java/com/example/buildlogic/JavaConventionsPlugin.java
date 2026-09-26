package com.example.buildlogic;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.jvm.toolchain.JavaToolchainService;
import java.util.Map;
import org.gradle.api.tasks.testing.Test;
import org.gradle.jvm.toolchain.JavaLanguageVersion;

/** Shared Java compilation and test conventions for standalone and aggregate builds. */
public final class JavaConventionsPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("java");
        project.getExtensions().getByType(JavaPluginExtension.class).getToolchain()
                .getLanguageVersion().set(JavaLanguageVersion.of(21));
        project.getTasks().withType(JavaCompile.class).configureEach(task -> {
            task.getOptions().setEncoding("UTF-8");
            task.getOptions().getRelease().set(21);
        });
        String basePackage = Map.of(
                "gateway", "com.example.gateway",
                "mock-rest-api-server", "com.example.mockserver",
                "report-server", "com.example.reportserver",
                "karate-tests", "karate").get(project.getName());
        if (basePackage != null) {
            var sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            var toolchains = project.getExtensions().getByType(JavaToolchainService.class);
            var launcher = toolchains.launcherFor(spec -> spec.getLanguageVersion().set(JavaLanguageVersion.of(21)));
            var architecture = project.getTasks().register("architectureTest", ArchitectureCheck.class, task -> {
                task.setGroup("verification");
                task.setDescription("Verify compiled package dependencies with the Java 21 toolchain");
                task.getBasePackage().set(basePackage);
                String sourceSet = project.getName().equals("karate-tests") ? "test" : "main";
                task.getClasses().from(sourceSets.getByName(sourceSet).getOutput().getClassesDirs());
                task.getJdeps().set(launcher.map(java -> java.getMetadata().getInstallationPath().file("bin/jdeps")));
                task.getReportFile().set(project.getLayout().getBuildDirectory().file("reports/architecture/dependencies.txt"));
            });
            project.getTasks().named("check", task -> task.dependsOn(architecture));
        }
        project.getTasks().withType(Test.class).configureEach(Test::useJUnitPlatform);
        if (basePackage != null) {
            JavaQuality.configure(project, basePackage);
        }
    }
}
