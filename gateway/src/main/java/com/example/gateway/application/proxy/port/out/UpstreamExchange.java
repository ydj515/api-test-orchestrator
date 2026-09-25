package com.example.gateway.application.proxy.port.out;

import com.example.gateway.application.proxy.result.ProxyResponse;
import com.example.gateway.domain.routing.model.ApiRoute;
import java.net.URI;
import java.util.Map;

public interface UpstreamExchange {
    ProxyResponse post(ApiRoute route, URI uri, String checksum, Map<String, Object> requestBody);
}
