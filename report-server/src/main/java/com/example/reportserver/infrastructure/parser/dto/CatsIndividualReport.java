package com.example.reportserver.infrastructure.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * 개별 TestN.json 구조 — 타임스탬프 추출용
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatsIndividualReport {

    private Request request;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Request {
        private String timestamp;
    }
}
