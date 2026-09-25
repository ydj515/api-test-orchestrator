package com.example.reportserver.domain.catalog.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class GatewayContract {
    String id;
    String title;
    String org;
    String service;
    String contractPath;
    String checksum;
    List<String> apis;
    Map<String, String> operationIdsByApi;

    private GatewayContract(
            String id,
            String title,
            String org,
            String service,
            String contractPath,
            String checksum,
            List<String> apis,
            Map<String, String> operationIdsByApi) {
        this.id = id;
        this.title = title;
        this.org = org;
        this.service = service;
        this.contractPath = contractPath;
        this.checksum = checksum;
        this.apis = apis == null ? List.of() : List.copyOf(apis);
        this.operationIdsByApi = operationIdsByApi == null ? Map.of() : Map.copyOf(operationIdsByApi);
    }

    public String findOperationId(String api) {
        if (api == null || api.isBlank() || operationIdsByApi == null) {
            return null;
        }
        return operationIdsByApi.get(api);
    }
}
