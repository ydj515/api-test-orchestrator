package com.example.reportserver.application.run.result;

import com.example.reportserver.domain.run.model.TestRun;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class RunHistoryRow {
    TestRun run;
    List<String> apis;
    List<String> caseNames;
    List<String> endpoints;
    List<String> httpMethods;
    List<Integer> httpStatuses;
    boolean visible;

    private RunHistoryRow(
            TestRun run,
            List<String> apis,
            List<String> caseNames,
            List<String> endpoints,
            List<String> httpMethods,
            List<Integer> httpStatuses,
            boolean visible) {
        this.run = run;
        this.apis = apis == null ? List.of() : List.copyOf(apis);
        this.caseNames = caseNames == null ? List.of() : List.copyOf(caseNames);
        this.endpoints = endpoints == null ? List.of() : List.copyOf(endpoints);
        this.httpMethods = httpMethods == null ? List.of() : List.copyOf(httpMethods);
        this.httpStatuses = httpStatuses == null ? List.of() : List.copyOf(httpStatuses);
        this.visible = visible;
    }

}
