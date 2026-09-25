package com.example.gateway.infrastructure.http;

import com.example.gateway.application.proxy.port.out.CryptoModule;
import com.example.gateway.application.proxy.port.out.ResponseDecoder;
import com.example.gateway.domain.routing.model.ApiRoute;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ResponseBodyDecryptor implements ResponseDecoder {

    private static final Logger log = LoggerFactory.getLogger(ResponseBodyDecryptor.class);

    private final CryptoModule cryptoModule;

    public ResponseBodyDecryptor(CryptoModule cryptoModule) {
        this.cryptoModule = cryptoModule;
    }

    public String decrypt(ApiRoute route, String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }

        try {
            String encryptedResponse = JsonPath.read(responseBody, "$.data");
            if (encryptedResponse == null) {
                return responseBody;
            }
            return cryptoModule.decrypt(route.key(), encryptedResponse);
        } catch (RuntimeException ex) {
            log.warn("Failed to decrypt upstream response for {}/{}/{} ({})",
                    route.org(), route.service(), route.api(), ex.getClass().getSimpleName());
            return responseBody;
        }
    }
}
