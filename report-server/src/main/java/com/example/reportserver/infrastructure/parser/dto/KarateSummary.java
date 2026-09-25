package com.example.reportserver.infrastructure.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KarateSummary {

    private String resultDate;
    private double elapsedTime;
    private int scenariosPassed;

    @JsonProperty("scenariosfailed")
    private int scenariosFailed;

    private List<FeatureItem> featureSummary;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FeatureItem {
        private String packageQualifiedName;
        private String relativePath;
        private int scenarioCount;
        private int passedCount;
        private int failedCount;
        private double durationMillis;
        private boolean failed;
    }
}
