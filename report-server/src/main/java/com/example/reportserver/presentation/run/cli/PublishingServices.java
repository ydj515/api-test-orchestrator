package com.example.reportserver.presentation.run.cli;

import com.example.reportserver.application.catalog.service.ContractQueryService;
import com.example.reportserver.application.run.port.in.RunPublisher;

public record PublishingServices(RunPublisher publisher, ContractQueryService contracts) {
}
