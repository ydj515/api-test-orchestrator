package com.example.reportserver.presentation.run.web;

import com.example.reportserver.application.run.query.RunFilter;
import com.example.reportserver.application.run.service.RunQueryService;
import com.example.reportserver.application.run.service.ServiceSummaryService;
import com.example.reportserver.presentation.run.web.dto.RunFilterRequest;
import com.example.reportserver.presentation.run.web.dto.RunResponse;
import com.example.reportserver.presentation.run.web.dto.ServiceSummaryResponse;
import com.example.reportserver.presentation.run.web.view.HistoryRowView;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReportApiController {
    private final ServiceSummaryService serviceSummaryService;
    private final RunQueryService runQueryService;

    @GetMapping("/api/services")
    public ResponseEntity<List<ServiceSummaryResponse>> apiServices() {
        return ResponseEntity.ok(serviceSummaryService.listServiceSummaries().stream().map(ServiceSummaryResponse::from).toList());
    }

    @GetMapping("/api/services/{org}/{service}/history-metadata")
    public ResponseEntity<List<HistoryRowView>> apiHistoryMetadata(
            @PathVariable String org,
            @PathVariable String service,
            @ModelAttribute RunFilterRequest request
    ) {
        RunFilter filter = request.toFilter(org, service);
        return ResponseEntity.ok(runQueryService.findHistoryRowsWithCaseMetadata(filter).stream().map(HistoryRowView::from).toList());
    }

    @GetMapping("/api/runs")
    public ResponseEntity<List<RunResponse>> apiRuns(
            @RequestParam(required = false) String org,
            @RequestParam(required = false) String service,
            @ModelAttribute RunFilterRequest request
    ) {
        RunFilter filter = request.toFilter(org, service);
        return ResponseEntity.ok(runQueryService.findRuns(filter).stream().map(RunResponse::from).toList());
    }

}
