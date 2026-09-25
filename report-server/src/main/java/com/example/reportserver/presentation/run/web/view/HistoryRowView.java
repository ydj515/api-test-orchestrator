package com.example.reportserver.presentation.run.web.view;

import com.example.reportserver.application.run.result.RunHistoryRow;
import java.util.List;

public record HistoryRowView(RunView run, List<String> apis, List<String> caseNames,
                             List<String> endpoints, List<String> httpMethods,
                             List<Integer> httpStatuses, boolean visible) {
    public HistoryRowView {
        apis = List.copyOf(apis);
        caseNames = List.copyOf(caseNames);
        endpoints = List.copyOf(endpoints);
        httpMethods = List.copyOf(httpMethods);
        httpStatuses = List.copyOf(httpStatuses);
    }

    public static HistoryRowView from(RunHistoryRow row) {
        return new HistoryRowView(RunView.from(row.getRun()), row.getApis(), row.getCaseNames(),
                row.getEndpoints(), row.getHttpMethods(), row.getHttpStatuses(), row.isVisible());
    }
}
