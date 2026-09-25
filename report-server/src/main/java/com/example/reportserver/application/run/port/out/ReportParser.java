package com.example.reportserver.application.run.port.out;

import com.example.reportserver.application.run.result.ParsedRun;
import com.example.reportserver.domain.run.model.TestCaseGranularity;
import com.example.reportserver.domain.run.model.TestSource;
import java.nio.file.Path;

/** Parses a staged local report; implementations own the source-specific file formats. */
public interface ReportParser {
    ParsedRun parse(TestSource source, Path directory, String runId, String org, String service,
                    String api, TestCaseGranularity granularity);
}
