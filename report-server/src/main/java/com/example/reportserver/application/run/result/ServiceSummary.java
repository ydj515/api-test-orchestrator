package com.example.reportserver.application.run.result;

import com.example.reportserver.domain.run.model.TestStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ServiceSummary {
    String contractId;
    String org;
    String service;
    List<String> apis;
    int apiCount;
    LocalDateTime lastRunAt;
    TestStatus lastStatus;
    String lastRunId;

    private ServiceSummary(
            String contractId,
            String org,
            String service,
            List<String> apis,
            int apiCount,
            LocalDateTime lastRunAt,
            TestStatus lastStatus,
            String lastRunId) {
        this.contractId = contractId;
        this.org = org;
        this.service = service;
        this.apis = apis == null ? List.of() : List.copyOf(apis);
        this.apiCount = apiCount;
        this.lastRunAt = lastRunAt;
        this.lastStatus = lastStatus;
        this.lastRunId = lastRunId;
    }

}
