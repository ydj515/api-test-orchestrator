package com.example.gateway.domain.routing.model;

public record ApiRoute(
        String org,
        String service,
        String api,
        String method,
        String host,
        String externalPath,
        String key,
        String apiKey,
        String insCode,
        Boolean encrypted
) {
    public boolean isEncrypted() {
        return encrypted == null || encrypted;
    }

    @Override
    public String toString() {
        return "ApiRoute[org=%s, service=%s, api=%s, method=%s]".formatted(org, service, api, method);
    }
}
