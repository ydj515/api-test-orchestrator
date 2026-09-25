package com.example.reportserver.infrastructure.parser.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class KarateFeatureResult {

    private String name;
    private String resultDate;
    private double durationMillis;
    private int passedCount;
    private List<ScenarioResult> scenarioResults;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScenarioResult {
        private String name;
        private List<String> tags;
        private double durationMillis;
        private boolean failed;
        private long startTime;
        private List<StepResult> stepResults;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepResult {
        private Step step;
        private String stepLog;
        private StepResultDetail result;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        private String text;
        private String prefix;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StepResultDetail {
        private double millis;
        private String status;
        private String error;
    }
}
