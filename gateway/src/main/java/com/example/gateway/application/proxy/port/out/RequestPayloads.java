package com.example.gateway.application.proxy.port.out;

import com.example.gateway.domain.routing.model.ApiRoute;
import java.net.URI;
import java.util.Map;

public interface RequestPayloads {
    URI targetUri(ApiRoute route, String body);
    Map<String, Object> parse(String body);
}
