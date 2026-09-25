package com.example.reportserver.presentation.run.web.dto;

import com.example.reportserver.application.run.result.ServiceSummary;
import com.example.reportserver.domain.run.model.TestStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ServiceSummaryResponse(String contractId, String org, String service, List<String> apis,
                                     int apiCount,
                                     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime lastRunAt,
                                     TestStatus lastStatus, String lastRunId) {
    public static ServiceSummaryResponse from(ServiceSummary summary) {
        return new ServiceSummaryResponse(summary.getContractId(), summary.getOrg(), summary.getService(),
                summary.getApis(), summary.getApiCount(), summary.getLastRunAt(), summary.getLastStatus(),
                summary.getLastRunId());
    }
}
