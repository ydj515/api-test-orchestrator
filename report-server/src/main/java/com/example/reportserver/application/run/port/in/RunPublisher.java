package com.example.reportserver.application.run.port.in;

import com.example.reportserver.domain.catalog.model.GatewayContract;
import com.example.reportserver.domain.run.model.TestCaseGranularity;
import java.nio.file.Path;

/** Shared publishing contract for the HTTP and CLI delivery adapters. */
public interface RunPublisher {
    String publishKarate(String runId, Path reportDir, GatewayContract contract,
                         String api, TestCaseGranularity granularity);
    String publishCats(String runId, Path reportDir, GatewayContract contract, String api);
}
