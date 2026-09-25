package com.example.reportserver.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "report")
public record ReportProperties(String dataDir, String gatewayContractCatalogPath) {
    public ReportProperties {
        requirePath(dataDir, "report.data-dir");
        requirePath(gatewayContractCatalogPath, "report.gateway-contract-catalog-path");
    }

    private static void requirePath(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        Path.of(value);
    }

    public Path storagePath() {
        return Path.of(dataDir).toAbsolutePath().normalize();
    }

    public Path catalogPath() {
        return Path.of(gatewayContractCatalogPath).toAbsolutePath().normalize();
    }
}
