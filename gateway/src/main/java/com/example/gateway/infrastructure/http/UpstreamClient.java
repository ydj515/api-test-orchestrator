package com.example.gateway.infrastructure.http;

import com.example.gateway.application.proxy.port.out.UpstreamExchange;
import com.example.gateway.application.proxy.result.ProxyResponse;
import com.example.gateway.domain.routing.model.ApiRoute;
import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class UpstreamClient implements UpstreamExchange {

    private final RestClient restClient;
    private final GatewayHeaderFactory headerFactory = new GatewayHeaderFactory();

    public UpstreamClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public ProxyResponse post(
            ApiRoute route, URI uri, String checksum,
            Map<String, Object> requestBody) {
        ResponseEntity<String> response = post(uri, headerFactory.build(route, checksum), requestBody);
        return new ProxyResponse(response.getStatusCode().value(), response.getBody());
    }

    public ResponseEntity<String> post(URI uri, HttpHeaders headers, Map<String, Object> requestBody) {
        try {
            return restClient.post()
                    .uri(uri)
                    .headers(httpHeaders -> httpHeaders.putAll(headers))
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class);
        } catch (RestClientResponseException ex) {
            HttpHeaders responseHeaders = ex.getResponseHeaders() != null ? ex.getResponseHeaders() : HttpHeaders.EMPTY;
            return ResponseEntity.status(ex.getStatusCode())
                    .headers(responseHeaders)
                    .body(ex.getResponseBodyAsString());
        }
    }
}
