package com.example.reportserver.infrastructure.parser;

import com.example.reportserver.application.run.port.out.ReportParser;
import com.example.reportserver.application.run.result.ParsedRun;
import com.example.reportserver.domain.run.model.TestCaseGranularity;
import com.example.reportserver.domain.run.model.TestSource;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public record SourceReportParser(KarateReportParser karate, KarateCaseParser karateCases,
                                 CatsReportParser cats) implements ReportParser {
    @Override
    public ParsedRun parse(TestSource source, Path directory, String runId, String org, String service,
                           String api, TestCaseGranularity granularity) {
        return switch (source) {
            case KARATE -> new ParsedRun(karate.parse(directory, runId, org, service, api, granularity),
                    karateCases.parse(directory, runId, granularity));
            case CATS -> new ParsedRun(cats.parseRun(directory, runId, org, service, api),
                    cats.parseCases(directory, runId, org, service));
        };
    }
}
