package com.example.reportserver.presentation.run.web;

import com.example.reportserver.application.run.query.RunFilter;
import com.example.reportserver.application.run.result.RunDetail;
import com.example.reportserver.application.run.result.RunHistoryRow;
import com.example.reportserver.application.run.result.ServiceSummary;
import com.example.reportserver.application.run.service.RunQueryService;
import com.example.reportserver.application.run.service.ServiceSummaryService;
import com.example.reportserver.domain.run.model.TestRun;
import com.example.reportserver.domain.run.model.TestSource;
import com.example.reportserver.domain.run.model.TestStatus;
import com.example.reportserver.presentation.run.web.dto.RunFilterRequest;
import com.example.reportserver.presentation.run.web.view.CaseView;
import com.example.reportserver.presentation.run.web.view.HistoryRowView;
import com.example.reportserver.presentation.run.web.view.PageJson;
import com.example.reportserver.presentation.run.web.view.RunView;
import com.example.reportserver.presentation.run.web.view.ServiceView;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private static final DateTimeFormatter DATETIME_LOCAL_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ServiceSummaryService serviceSummaryService;
    private final RunQueryService runQueryService;
    private final PageJson pageJson;

    @GetMapping("/")
    public String index(Model model) {
        List<ServiceSummary> services = serviceSummaryService.listServiceSummaries();
        model.addAttribute("services", services.stream().map(ServiceView::from).toList());
        return "index";
    }

    @GetMapping("/services/{org}/{service}")
    public String history(
            @PathVariable String org,
            @PathVariable String service,
            @ModelAttribute RunFilterRequest request,
            Model model
    ) {
        RunFilter filter = request.toFilter(org, service);

        List<RunHistoryRow> historyRows = runQueryService.findHistoryRows(filter);
        long visibleHistoryCount = historyRows.stream().filter(RunHistoryRow::isVisible).count();

        List<HistoryRowView> rows = historyRows.stream().map(HistoryRowView::from).toList();
        model.addAttribute("historyRows", rows);
        model.addAttribute("historyPageDataJson", pageJson.write(rows));
        model.addAttribute("visibleHistoryCount", visibleHistoryCount);
        model.addAttribute("historyMetadataComplete", runQueryService.requiresHistoryCaseMetadata(filter));
        model.addAttribute("org", org);
        model.addAttribute("service", service);
        model.addAttribute("filter", filter);
        model.addAttribute("allSources", TestSource.values());
        model.addAttribute("allStatuses", TestStatus.values());
        model.addAttribute("fromStr", request.from() != null ? request.from().format(DATETIME_LOCAL_FORMAT) : "");
        model.addAttribute("toStr", request.to() != null ? request.to().format(DATETIME_LOCAL_FORMAT) : "");
        return "history";
    }

    @GetMapping("/runs/{runId}")
    public String runDetail(@PathVariable String runId, Model model) {
        RunDetail detail = runQueryService.findDetail(runId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found: " + runId));
        model.addAttribute("run", RunView.from(detail.run()));
        model.addAttribute("cases", detail.cases().stream().map(CaseView::from).toList());
        model.addAttribute("availableApisJson", pageJson.write(detail.availableApis()));
        model.addAttribute("availableKinds", detail.availableKinds());
        model.addAttribute("allStatuses", TestStatus.values());
        return "detail";
    }

    @GetMapping("/reports/{runId}")
    public String rawReportRedirect(@PathVariable String runId) {
        TestRun run = runQueryService.findRun(runId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Run not found: " + runId));
        if (run.getReportPath() == null || run.getReportPath().isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report path not found: " + runId);
        }
        return "redirect:" + run.getReportPath();
    }

}
