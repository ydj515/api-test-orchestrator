package com.example.reportserver.presentation.run.web.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CatsPublishRequest {

    private String runId;
    private String reportDir;
    private String contractId;
    private String contractPath;
    private String org;
    private String service;
    private String api;
}
