package com.example.reportserver.presentation.run.web.dto;

import com.example.reportserver.domain.run.model.TestCaseGranularity;
import com.example.reportserver.domain.run.model.TestRun;
import com.example.reportserver.domain.run.model.TestSource;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record RunResponse(
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
    public static RunResponse from(TestRun value) {
        return new RunResponse(
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
}
