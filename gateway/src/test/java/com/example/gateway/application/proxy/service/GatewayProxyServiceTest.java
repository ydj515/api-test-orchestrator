package com.example.gateway.application.proxy.service;

import com.example.gateway.application.proxy.port.out.ChecksumModule;
import com.example.gateway.application.proxy.port.out.CryptoModule;
import com.example.gateway.application.proxy.port.out.ResponseDecoder;
import com.example.gateway.application.proxy.port.out.UpstreamExchange;
import com.example.gateway.application.proxy.result.ProxyResponse;
import com.example.gateway.domain.routing.model.ApiRoute;
import com.example.gateway.infrastructure.http.JacksonRequestPayloads;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GatewayProxyServiceTest {

    @Mock
    private CryptoModule cryptoModule;
    @Mock
    private ChecksumModule checksumModule;
    @Mock
    private RouteResolver routeResolver;
    @Mock
    private UpstreamExchange upstreamClient;
    @Mock
    private ResponseDecoder responseBodyDecryptor;

    private GatewayProxyService gatewayProxyService;

    private final ApiRoute route = new ApiRoute("o", "s", "a", "POST", "http://example.com", "/ext", "key", null, null, null);

    @BeforeEach
    void setUp() {
        gatewayProxyService = new GatewayProxyService(
                cryptoModule,
                checksumModule,
                routeResolver,
                upstreamClient,
                responseBodyDecryptor,
                new JacksonRequestPayloads(new ObjectMapper())
        );
    }

    @Test
    // encrypt → checksum → upstream 호출 → decrypt 흐름이 정상 동작하는 성공 케이스
    void proxyPostEncryptsCallsUpstreamAndDecrypts() {
        // given
        when(routeResolver.resolve("o", "s", "a", "POST")).thenReturn(route);
        when(cryptoModule.encrypt("key", "plain")).thenReturn("encrypted");
        when(checksumModule.checksum("encrypted")).thenReturn("chk");

        ProxyResponse upstreamResponse = new ProxyResponse(201, "cipher");
        when(upstreamClient.post(any(ApiRoute.class), any(URI.class), eq("chk"), anyMap())).thenReturn(upstreamResponse);
        when(responseBodyDecryptor.decrypt(route, "cipher")).thenReturn("plain-response");

        // when
        ProxyResponse result = gatewayProxyService.proxyPost("o", "s", "a", "plain");

        // then
        assertThat(result.status()).isEqualTo(201);
        assertThat(result.body()).isEqualTo("plain-response");

        verify(routeResolver).resolve("o", "s", "a", "POST");
        verify(cryptoModule).encrypt("key", "plain");
        verify(checksumModule).checksum("encrypted");
        verify(responseBodyDecryptor).decrypt(route, "cipher");

        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);
        verify(upstreamClient).post(eq(route), any(URI.class), eq("chk"), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).containsEntry("data", "encrypted");
    }

    @Test
    // externalPath 템플릿이 존재하면 body의 변수값을 치환해 업스트림 URI를 만든다.
    void proxyPostExpandsExternalPathTemplateFromBody() {
        ApiRoute templateRoute = new ApiRoute(
                "o", "s", "inventory", "POST", "http://example.com",
                "/resources/{resourceId}/inventory", "key", null, null, null
        );
        when(routeResolver.resolve("o", "s", "inventory", "POST")).thenReturn(templateRoute);
        when(cryptoModule.encrypt("key", "{\"resourceId\":\"R-100\"}")).thenReturn("encrypted");
        when(checksumModule.checksum("encrypted")).thenReturn("chk");

        when(upstreamClient.post(any(ApiRoute.class), any(URI.class), eq("chk"), anyMap()))
                .thenReturn(new ProxyResponse(200, "cipher"));
        when(responseBodyDecryptor.decrypt(templateRoute, "cipher")).thenReturn("plain-response");

        gatewayProxyService.proxyPost("o", "s", "inventory", "{\"resourceId\":\"R-100\"}");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(upstreamClient).post(eq(templateRoute), uriCaptor.capture(), eq("chk"), anyMap());
        assertThat(uriCaptor.getValue().toString()).isEqualTo("http://example.com/resources/R-100/inventory");
    }

    @Test
    // 템플릿 변수값이 body에 없으면 예외를 던진다.
    void proxyPostThrowsWhenTemplateVariableMissing() {
        ApiRoute templateRoute = new ApiRoute(
                "o", "s", "inventory", "POST", "http://example.com",
                "/resources/{resourceId}/inventory", "key", null, null, null
        );
        when(routeResolver.resolve("o", "s", "inventory", "POST")).thenReturn(templateRoute);

        assertThatThrownBy(() -> gatewayProxyService.proxyPost("o", "s", "inventory", "{\"foo\":\"bar\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing template variable: resourceId");
    }

    @Test
    // path 변수에 주입성 문자열이 들어오면 업스트림 호출 전에 400 대상 예외로 차단한다.
    void proxyPostThrowsWhenTemplateVariableHasInvalidFormat() {
        ApiRoute templateRoute = new ApiRoute(
                "o", "s", "inventory", "POST", "http://example.com",
                "/resources/{resourceId}/inventory", "key", null, null, null
        );
        when(routeResolver.resolve("o", "s", "inventory", "POST")).thenReturn(templateRoute);

        assertThatThrownBy(() -> gatewayProxyService.proxyPost("o", "s", "inventory", "{\"resourceId\":\"; ls -la\",\"date\":\"2026-03-02\"}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid path variable format: resourceId");
    }
}
