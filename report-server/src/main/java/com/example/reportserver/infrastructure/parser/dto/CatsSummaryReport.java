package com.example.reportserver.infrastructure.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatsSummaryReport {

    private List<TestCaseItem> testCases;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestCaseItem {
        private String id;
        private String scenario;
        private String result;
        private String resultReason;
        private String resultDetails;
        private String path;
        private String fuzzer;
        private double timeToExecuteInSec;
        private String timeToExecuteInMs;
        private String httpMethod;
        private boolean switchedResult;
        private int httpResponseCode;
    }
}
