package com.example.reportserver.domain.run.model;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestRun {

    private String id;
    private String org;
    private String service;
    private String api;
    private String operationId;
    private String contractId;
    private String contractPath;
    private String contractChecksum;
    private TestSource source;
    private TestCaseGranularity caseGranularity;

    private LocalDateTime startedAt;

    private long durationMs;
    private int totalCount;
    private int passCount;
    private int failCount;
    private String reportPath;
}
