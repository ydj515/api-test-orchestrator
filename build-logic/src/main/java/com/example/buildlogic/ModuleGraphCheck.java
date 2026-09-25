package com.example.buildlogic;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.IOException;
import java.nio.file.Files;

@DisableCachingByDefault(because = "Small documentation consistency check with no generated output")
public abstract class ModuleGraphCheck extends DefaultTask {
    @Input
    public abstract ListProperty<String> getActualGraph();

    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract RegularFileProperty getArchitectureDocument();

    @TaskAction
    public void verify() throws IOException {
        verifyGraph(Files.readString(getArchitectureDocument().get().getAsFile().toPath()), getActualGraph().get());
    }

    static void verifyGraph(String document, java.util.List<String> graph) {
        String startMarker = "<!-- gradle-module-graph:start -->";
        String endMarker = "<!-- gradle-module-graph:end -->";
        int start = document.indexOf(startMarker);
        int end = document.indexOf(endMarker);
        if (start < 0 || end <= start) {
            throw new GradleException("Architecture document must declare the Gradle module graph");
        }
        var documented = document.substring(start + startMarker.length(), end).lines()
                .map(String::strip).filter(line -> line.startsWith(":")).sorted().toList();
        var actual = graph.stream().sorted().toList();
        if (!actual.equals(documented)) {
            throw new GradleException("Documented module graph differs from Gradle: expected " + actual
                    + ", documented " + documented);
        }
    }
}
