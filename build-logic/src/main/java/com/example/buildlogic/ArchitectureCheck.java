package com.example.buildlogic;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Checks compiled dependencies, including method bodies, without resolving an analysis library. */
@DisableCachingByDefault(because = "Invokes a local JDK analysis tool; outputs remain available for inspection")
public abstract class ArchitectureCheck extends DefaultTask {
    private static final Pattern DEPENDENCY = Pattern.compile("^\\s*(\\S+)\\s+->\\s+(\\S+)\\s+.*$");

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getClasses();

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getJdeps();

    @Input
    public abstract Property<String> getBasePackage();

    @OutputFile
    public abstract RegularFileProperty getReportFile();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @TaskAction
    public void checkDependencies() throws IOException {
        List<String> command = new ArrayList<>(List.of(getJdeps().get().getAsFile().getAbsolutePath(),
                "--ignore-missing-deps", "-verbose:class", "-filter:none"));
        getClasses().getFiles().stream().filter(File::isDirectory)
                .map(File::getAbsolutePath).forEach(command::add);
        String output = "No compiled production classes\n";
        if (command.size() > 4) {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            getExecOperations().exec(spec -> {
                spec.commandLine(command);
                spec.setStandardOutput(buffer);
                spec.setErrorOutput(buffer);
            }).assertNormalExitValue();
            output = buffer.toString(StandardCharsets.UTF_8);
        }
        List<String> violations = new ArrayList<>();
        String base = getBasePackage().get();
        for (String line : output.lines().toList()) {
            var match = DEPENDENCY.matcher(line);
            if (match.matches() && forbidden(base, match.group(1), match.group(2))) {
                violations.add(match.group(1) + " -> " + match.group(2));
            }
        }
        var report = getReportFile().get().getAsFile().toPath();
        Files.createDirectories(report.getParent());
        Files.writeString(report, output, StandardCharsets.UTF_8);
        if (!violations.isEmpty()) {
            throw new GradleException("Architecture violations:\n" + String.join("\n", violations));
        }
    }

    static boolean forbidden(String base, String source, String target) {
        if (!source.startsWith(base + ".")) return false;
        if (target.startsWith("com.example.") && !target.startsWith(base + ".")) return true;
        String from = source.substring(base.length() + 1);
        String layer = from.contains(".") ? from.substring(0, from.indexOf('.')) : "bootstrap";
        if (base.equals("karate")) return false;
        String bootstrap = switch (base) {
            case "com.example.gateway" -> "GatewayApplication";
            case "com.example.mockserver" -> "MockRestApiServerApplication";
            case "com.example.reportserver" -> "ReportServerApplication";
            default -> "";
        };
        if (layer.equals("bootstrap") && !from.equals(bootstrap) && !from.startsWith(bootstrap + "$")) {
            return true;
        }
        if (!List.of("bootstrap", "presentation", "application", "domain", "infrastructure", "config").contains(layer)) {
            return true;
        }
        if (target.equals("org.springframework.transaction.annotation.Transactional")) {
            return !layer.equals("application");
        }
        boolean spring = target.startsWith("org.springframework.");
        boolean serialization = target.startsWith("com.fasterxml.jackson.") || target.startsWith("org.yaml.");
        boolean servlet = target.startsWith("jakarta.");
        if (layer.equals("domain") && (spring || serialization || servlet)) return true;
        if (layer.equals("application")) {
            if (serialization || servlet) return true;
            if (spring && !target.startsWith("org.springframework.stereotype.")
                    && !target.startsWith("org.springframework.transaction.annotation.")) return true;
        }
        if (!target.startsWith(base + ".")) return false;
        String to = target.substring(base.length() + 1);
        if (!to.contains(".") && !layer.equals("bootstrap") && !layer.equals("config")) return true;
        return switch (layer) {
            case "domain" -> !to.startsWith("domain.");
            case "application" -> !to.startsWith("application.") && !to.startsWith("domain.");
            case "presentation" -> to.startsWith("infrastructure.") || to.startsWith("config.")
                    || to.contains(".port.out.")
                    || (to.startsWith("domain.") && !(to.contains(".model.")
                        || to.contains(".exception.") || to.equals("domain.shared.BusinessException")
                        || to.startsWith("domain.shared.BusinessException$")));
            case "infrastructure" -> to.startsWith("presentation.") || to.startsWith("config.")
                    || (to.startsWith("application.") && to.contains(".service."));
            default -> false;
        };
    }
}
