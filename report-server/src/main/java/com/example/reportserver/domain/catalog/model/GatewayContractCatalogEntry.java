package com.example.reportserver.domain.catalog.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GatewayContractCatalogEntry {
    String id;
    String org;
    String service;
    String openapiPath;
}
