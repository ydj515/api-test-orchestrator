package com.example.buildlogic;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

/** Prevent JaCoCo's no-data skip from silently passing a production coverage gate. */
@DisableCachingByDefault(because = "Validates required test execution data before report generation")
public abstract class CoverageInputsCheck extends DefaultTask {
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getExecutionData();

    @TaskAction
    public void verify() {
        if (getExecutionData().get().getAsFile().length() == 0) {
            throw new GradleException("Coverage execution data is empty; run the module tests");
        }
    }
}
