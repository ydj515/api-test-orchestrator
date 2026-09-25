package com.example.gateway.application.proxy.port.out;

import com.example.gateway.domain.routing.model.ApiRoute;

public interface ResponseDecoder {
    String decrypt(ApiRoute route, String body);
}
