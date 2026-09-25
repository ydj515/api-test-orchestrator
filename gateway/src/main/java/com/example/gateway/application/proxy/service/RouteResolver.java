package com.example.gateway.application.proxy.service;

import com.example.gateway.application.proxy.exception.RouteNotFoundException;
import com.example.gateway.application.proxy.port.out.RouteCatalog;
import com.example.gateway.domain.routing.model.ApiRoute;
import org.springframework.stereotype.Component;

@Component
public class RouteResolver {

    private final RouteCatalog gatewayProperties;

    public RouteResolver(RouteCatalog gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    public ApiRoute resolve(String org, String service, String api, String method) {
        return gatewayProperties.find(org, service, api, method)
                .orElseThrow(() -> new RouteNotFoundException("Route not found for %s/%s/%s (%s)".formatted(org, service, api, method)));
    }
}
