package com.example.reportserver.presentation.run.web.dto;

import com.example.reportserver.application.run.query.RunFilter;
import com.example.reportserver.domain.run.model.TestSource;
import com.example.reportserver.domain.run.model.TestStatus;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

public record RunFilterRequest(String api, String method, TestSource source, TestStatus status,
                               Integer httpStatus, String caseName, String endpoint,
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime from,
                               @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime to) {
    public RunFilter toFilter(String org, String service) {
        return RunFilter.builder().org(org).service(service).api(api).httpMethod(method).source(source).status(status)
                .httpStatus(httpStatus).caseName(caseName).endpoint(endpoint).from(from).to(to).build();
    }
}
