package com.example.reportserver.config;

import com.example.reportserver.infrastructure.catalog.GatewayContractCatalogService;
import com.example.reportserver.infrastructure.persistence.FileRunPublicationStore;
import com.example.reportserver.infrastructure.persistence.RunStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ReportProperties.class)
public class ReportConfig {
    @Bean
    RunStorageService runStorageService(ReportProperties properties, ObjectMapper mapper) {
        return new RunStorageService(properties.storagePath(), mapper);
    }

    @Bean
    GatewayContractCatalogService gatewayContractCatalogService(ReportProperties properties) {
        return new GatewayContractCatalogService(properties.catalogPath());
    }

    @Bean
    FileRunPublicationStore fileRunPublicationStore(RunStorageService storage) {
        return new FileRunPublicationStore(storage);
    }

    @Bean
    Clock clock() {
        return Clock.systemDefaultZone();
    }
}
