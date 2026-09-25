package com.example.gateway.application.proxy.service;

import com.example.gateway.application.proxy.port.out.ChecksumModule;
import com.example.gateway.application.proxy.port.out.CryptoModule;
import com.example.gateway.application.proxy.port.out.RequestPayloads;
import com.example.gateway.application.proxy.port.out.ResponseDecoder;
import com.example.gateway.application.proxy.port.out.UpstreamExchange;
import com.example.gateway.application.proxy.result.ProxyResponse;
import com.example.gateway.domain.routing.model.ApiRoute;
import java.net.URI;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public final class GatewayProxyService {

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{1,100}$");

    private final CryptoModule cryptoModule;
    private final ChecksumModule checksumModule;
    private final RouteResolver routeResolver;
    private final UpstreamExchange upstreamClient;
    private final ResponseDecoder responseBodyDecryptor;
    private final RequestPayloads requestPayloads;

    public GatewayProxyService(CryptoModule cryptoModule,
                               ChecksumModule checksumModule,
                               RouteResolver routeResolver,
                               UpstreamExchange upstreamClient,
                               ResponseDecoder responseBodyDecryptor,
                               RequestPayloads requestPayloads) {
        this.cryptoModule = cryptoModule;
        this.checksumModule = checksumModule;
        this.routeResolver = routeResolver;
        this.upstreamClient = upstreamClient;
        this.responseBodyDecryptor = responseBodyDecryptor;
        this.requestPayloads = requestPayloads;
    }

    public ProxyResponse proxyPost(String org, String service, String api, String plainBody) {
        validatePathSegment("org", org);
        validatePathSegment("service", service);
        validatePathSegment("api", api);

        ApiRoute route = routeResolver.resolve(org, service, api, "POST");
        URI targetUri = requestPayloads.targetUri(route, plainBody);

        if (route.isEncrypted()) {
            String encryptedData = cryptoModule.encrypt(route.key(), plainBody);
            String checksum = checksumModule.checksum(encryptedData);
            ProxyResponse upstream = upstreamClient.post(route, targetUri, checksum, Map.of("data", encryptedData));
            return new ProxyResponse(upstream.status(), responseBodyDecryptor.decrypt(route, upstream.body()));
        }

        ProxyResponse upstream = upstreamClient.post(route, targetUri, null, requestPayloads.parse(plainBody));
        return upstream;
    }

    private void validatePathSegment(String segmentName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid path segment: " + segmentName);
        }
        if (!PATH_VARIABLE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid path segment format: " + segmentName);
        }
    }
}
