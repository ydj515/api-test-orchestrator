package com.example.gateway.application.proxy.result;

/** Status and payload of a transparent proxy exchange, independent of Spring HTTP types. */
public record ProxyResponse(int status, String body) {
}
