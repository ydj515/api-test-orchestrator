package com.example.reportserver.application.run.port.out;

import com.example.reportserver.application.run.result.ParsedRun;
import java.nio.file.Path;
import java.util.function.Function;

public interface RunPublicationStore {
    /** Publishes a complete run or leaves no visible run when parsing or writing fails. */
    void publish(String runId, Path reportDir, Function<Path, ParsedRun> parseStagedReport);
}
