package com.example.reportserver.application.run.query;

import com.example.reportserver.domain.run.model.TestCaseKind;
import com.example.reportserver.domain.run.model.TestStatus;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CaseFilter {
    String api;
    TestStatus status;
    TestCaseKind kind;
}
