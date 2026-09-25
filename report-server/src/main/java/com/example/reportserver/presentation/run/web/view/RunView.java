package com.example.reportserver.presentation.run.web.view;

import com.example.reportserver.domain.run.model.TestCaseGranularity;
import com.example.reportserver.domain.run.model.TestRun;
import com.example.reportserver.domain.run.model.TestSource;
import com.example.reportserver.domain.run.model.TestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record RunView(
        String id,
        String org,
        String service,
        String api,
        String operationId,
        String contractId,
        String contractPath,
        String contractChecksum,
        TestSource source,
        TestCaseGranularity caseGranularity,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime startedAt,
        long durationMs,
        int totalCount,
        int passCount,
        int failCount,
        String reportPath) {
    public static RunView from(TestRun value) {
        return new RunView(
                value.getId(),
                value.getOrg(),
                value.getService(),
                value.getApi(),
                value.getOperationId(),
                value.getContractId(),
                value.getContractPath(),
                value.getContractChecksum(),
                value.getSource(),
                value.getCaseGranularity(),
                value.getStartedAt(),
                value.getDurationMs(),
                value.getTotalCount(),
                value.getPassCount(),
                value.getFailCount(),
                value.getReportPath());
    }

    public TestStatus status() {
        return failCount > 0 ? TestStatus.FAIL : TestStatus.PASS;
    }

    public String badgeClass() {
        return ViewFormat.badge(status());
    }

    public String durationLabel() {
        return ViewFormat.duration(durationMs);
    }

    public String startedAtLabel() {
        return ViewFormat.dateTime(startedAt);
    }

    public String apiLabel() {
        return api == null || api.isBlank() ? "전체 API" : api;
    }

    public boolean hasReport() {
        return reportPath != null && !reportPath.isBlank();
    }
}
