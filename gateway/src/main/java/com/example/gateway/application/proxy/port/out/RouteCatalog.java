package com.example.gateway.application.proxy.port.out;

import com.example.gateway.domain.routing.model.ApiRoute;
import java.util.Optional;

public interface RouteCatalog {
    Optional<ApiRoute> find(String org, String service, String api, String method);
}
