package com.example.reportserver.presentation.run.web.view;

import com.example.reportserver.application.run.result.ServiceSummary;
import com.example.reportserver.domain.run.model.TestStatus;
import java.time.LocalDateTime;

public record ServiceView(String org, String service, int apiCount, LocalDateTime lastRunAt, TestStatus lastStatus) {
    public static ServiceView from(ServiceSummary summary) {
        return new ServiceView(summary.getOrg(), summary.getService(), summary.getApiCount(),
                summary.getLastRunAt(), summary.getLastStatus());
    }

    public String badgeClass() {
        return ViewFormat.badge(lastStatus);
    }

    public String statusLabel() {
        return lastStatus == null ? "미실행" : lastStatus.name();
    }

    public String lastRunLabel() {
        return ViewFormat.dateTime(lastRunAt);
    }
}
