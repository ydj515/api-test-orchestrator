package com.example.reportserver.application.catalog.service;

import com.example.reportserver.application.catalog.port.out.ContractCatalog;
import com.example.reportserver.domain.catalog.model.GatewayContract;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContractQueryService {
    private final ContractCatalog catalog;

    public GatewayContract resolve(String contractId, String contractPath, String org, String service) {
        return catalog.resolveContract(contractId, contractPath)
                .or(() -> catalog.findByOrgService(org, service))
                .orElseThrow(() -> new IllegalArgumentException("contractId/contractPath or org/service is required"));
    }
}
