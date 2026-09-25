package com.example.gateway.infrastructure.http;

import com.example.gateway.domain.routing.model.ApiRoute;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class GatewayHeaderFactory {

    public HttpHeaders build(ApiRoute route, String checksum) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        if (checksum != null) {
            headers.set(GatewayHeaderNames.X_CHECKSUM, checksum);
        }
        if (route.apiKey() != null) {
            headers.set(GatewayHeaderNames.X_API_KEY, route.apiKey());
        }
        if (route.insCode() != null) {
            headers.set(GatewayHeaderNames.X_INS_CODE, route.insCode());
        }
        return headers;
    }
}
