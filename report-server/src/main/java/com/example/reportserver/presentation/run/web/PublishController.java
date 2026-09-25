package com.example.reportserver.presentation.run.web;

import com.example.reportserver.application.catalog.service.ContractQueryService;
import com.example.reportserver.application.run.port.in.RunPublisher;
import com.example.reportserver.domain.catalog.model.GatewayContract;
import com.example.reportserver.domain.run.model.TestCaseGranularity;
import com.example.reportserver.presentation.run.web.dto.CatsPublishRequest;
import com.example.reportserver.presentation.run.web.dto.KaratePublishRequest;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
@RequiredArgsConstructor
public class PublishController {

    private final RunPublisher runPublishService;
    private final ContractQueryService contractQueryService;

    @PostMapping("/karate")
    public ResponseEntity<String> publishKarate(@RequestBody KaratePublishRequest request) {
        GatewayContract contract = contractQueryService.resolve(
                request.getContractId(),
                request.getContractPath(),
                request.getOrg(),
                request.getService()
        );
        String runId = runPublishService.publishKarate(
                request.getRunId(),
                Path.of(request.getReportDir()),
                contract,
                request.getApi(),
                TestCaseGranularity.from(request.getCaseGranularity())
        );
        return ResponseEntity.ok(runId);
    }

    @PostMapping("/cats")
    public ResponseEntity<String> publishCats(@RequestBody CatsPublishRequest request) {
        GatewayContract contract = contractQueryService.resolve(
                request.getContractId(),
                request.getContractPath(),
                request.getOrg(),
                request.getService()
        );
        String runId = runPublishService.publishCats(
                request.getRunId(),
                Path.of(request.getReportDir()),
                contract,
                request.getApi()
        );
        return ResponseEntity.ok(runId);
    }

}
