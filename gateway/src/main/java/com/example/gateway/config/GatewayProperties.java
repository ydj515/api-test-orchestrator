package com.example.gateway.config;

import com.example.gateway.application.proxy.port.out.RouteCatalog;
import com.example.gateway.domain.routing.model.ApiRoute;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway")
public class GatewayProperties implements RouteCatalog {

    private List<ApiRoute> apis = List.of();
    private Map<RouteKey, ApiRoute> routeIndex = Map.of();

    public List<ApiRoute> getApis() {
        return apis;
    }

    public void setApis(List<ApiRoute> routes) {
        List<ApiRoute> snapshot = routes == null ? List.of() : List.copyOf(routes);
        Map<RouteKey, ApiRoute> index = new HashMap<>();
        for (ApiRoute route : snapshot) {
            RouteKey key = RouteKey.of(route.org(), route.service(), route.api(), route.method());
            if (index.putIfAbsent(key, route) != null) {
                throw new IllegalArgumentException("Duplicate gateway route: " + key);
            }
        }
        this.apis = snapshot;
        this.routeIndex = Map.copyOf(index);
    }

    public Optional<ApiRoute> find(String org, String service, String api, String method) {
        return Optional.ofNullable(routeIndex.get(RouteKey.of(org, service, api, method)));
    }

    private record RouteKey(String org, String service, String api, String method) {
        private static RouteKey of(String org, String service, String api, String method) {
            return new RouteKey(required(org, "org"), required(service, "service"),
                    required(api, "api"), required(method, "method").toUpperCase(Locale.ROOT));
        }

        private static String required(String value, String name) {
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Gateway route " + name + " is required");
            }
            return value;
        }
    }
}
