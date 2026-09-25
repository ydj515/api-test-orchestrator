package com.example.reportserver.config;

import com.example.reportserver.application.catalog.service.ContractQueryService;
import com.example.reportserver.application.run.service.RunPublishService;
import com.example.reportserver.infrastructure.catalog.GatewayContractCatalogService;
import com.example.reportserver.infrastructure.parser.CatsReportParser;
import com.example.reportserver.infrastructure.parser.KarateCaseParser;
import com.example.reportserver.infrastructure.parser.KarateReportParser;
import com.example.reportserver.infrastructure.parser.SourceReportParser;
import com.example.reportserver.infrastructure.persistence.FileRunPublicationStore;
import com.example.reportserver.infrastructure.persistence.RunStorageService;
import com.example.reportserver.presentation.run.cli.PublishingServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.time.Clock;

/** Standalone composition root: publishing commands do not start a web server. */
public final class CliConfig {
    private CliConfig() {
    }

    public static PublishingServices services(Path dataDir, Path catalogPath) {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var parser = new SourceReportParser(new KarateReportParser(mapper), new KarateCaseParser(mapper),
                new CatsReportParser(mapper));
        var publications = new FileRunPublicationStore(new RunStorageService(dataDir, mapper));
        return new PublishingServices(new RunPublishService(parser, publications, Clock.systemDefaultZone()),
                new ContractQueryService(new GatewayContractCatalogService(catalogPath)));
    }
}
