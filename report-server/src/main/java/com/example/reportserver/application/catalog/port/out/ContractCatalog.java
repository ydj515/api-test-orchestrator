package com.example.reportserver.application.catalog.port.out;

import com.example.reportserver.domain.catalog.model.GatewayContract;
import java.util.List;
import java.util.Optional;

public interface ContractCatalog {
    List<GatewayContract> listContracts();
    Optional<GatewayContract> findByOrgService(String org, String service);
    Optional<GatewayContract> resolveContract(String contractId, String contractPath);
}
