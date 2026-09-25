package com.example.reportserver.application.run.query;

import com.example.reportserver.domain.run.model.TestSource;
import com.example.reportserver.domain.run.model.TestStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RunFilter {
    String org;
    String service;
    String api;
    String httpMethod;
    TestSource source;
    TestStatus status;
    Integer httpStatus;
    String caseName;
    String endpoint;
    LocalDateTime from;
    LocalDateTime to;
}
