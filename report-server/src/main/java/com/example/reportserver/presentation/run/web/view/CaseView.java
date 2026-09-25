package com.example.reportserver.presentation.run.web.view;

import com.example.reportserver.domain.run.model.TestCase;
import com.example.reportserver.domain.run.model.TestCaseKind;
import com.example.reportserver.domain.run.model.TestCaseType;
import com.example.reportserver.domain.run.model.TestStatus;

public record CaseView(
        String id,
        String runId,
        TestCaseType caseType,
        TestCaseKind kind,
        String api,
        String name,
        String scenarioName,
        int sequence,
        String endpoint,
        String httpMethod,
        int httpStatus,
        TestStatus status,
        long durationMs,
        String failureMsg) {
    public static CaseView from(TestCase value) {
        return new CaseView(
                value.getId(),
                value.getRunId(),
                value.getCaseType(),
                value.getKind(),
                value.getApi(),
                value.getName(),
                value.getScenarioName(),
                value.getSequence(),
                value.getEndpoint(),
                value.getHttpMethod(),
                value.getHttpStatus(),
                value.getStatus(),
                value.getDurationMs(),
                value.getFailureMsg());
    }

    public String badgeClass() {
        return ViewFormat.badge(status);
    }

    public String statusLabel() {
        return status == null ? "알 수 없음" : status.name();
    }

    public String nameLabel() {
        return name == null ? "이름 없는 케이스" : name;
    }

    public String durationLabel() {
        return durationMs > 0 ? ViewFormat.duration(durationMs) : "—";
    }

    public String httpStatusLabel() {
        return httpStatus > 0 ? Integer.toString(httpStatus) : "—";
    }

    public boolean hasDistinctScenario() {
        return scenarioName != null && !scenarioName.equals(name);
    }
}
