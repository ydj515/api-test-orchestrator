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
        return architecture.policy.LayerPolicy.forbidden(base, source, target);
    }
}
