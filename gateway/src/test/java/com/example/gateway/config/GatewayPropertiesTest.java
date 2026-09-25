package com.example.gateway.config;

import com.example.gateway.domain.routing.model.ApiRoute;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(Config.class)
            .withPropertyValues(
                    "gateway.apis[0].org=o",
                    "gateway.apis[0].service=s",
                    "gateway.apis[0].api=a",
                    "gateway.apis[0].method=POST",
                    "gateway.apis[0].host=http://example.com",
                    "gateway.apis[0].externalPath=/ext",
                    "gateway.apis[0].key=secret",
                    "gateway.apis[0].apiKey=api-key",
                    "gateway.apis[0].insCode=ins"
            );

    @EnableConfigurationProperties(GatewayProperties.class)
    static class Config {
    }

    @Test
    // properties 값이 GatewayProperties로 정상 바인딩되는지 검증 (성공 케이스)
    void bindsApiRoutes() {
        // given
        // ApplicationContextRunner에 property values 설정 완료

        // when then
        contextRunner.run(ctx -> {
            // then
            GatewayProperties props = ctx.getBean(GatewayProperties.class);
            assertThat(props.getApis()).hasSize(1);
            ApiRoute route = props.getApis().get(0);
            assertThat(route.org()).isEqualTo("o");
            assertThat(route.service()).isEqualTo("s");
            assertThat(route.api()).isEqualTo("a");
            assertThat(route.method()).isEqualTo("POST");
            assertThat(route.host()).isEqualTo("http://example.com");
            assertThat(route.externalPath()).isEqualTo("/ext");
            assertThat(route.key()).isEqualTo("secret");
            assertThat(route.apiKey()).isEqualTo("api-key");
            assertThat(route.insCode()).isEqualTo("ins");
        });
    }

    @Test
    // setApis 이후 find가 캐시된 키로 조회되며 method 대소문자를 무시하는지 검증
    void findUsesCachedIndexWithCaseInsensitiveMethod() {
        // given
        GatewayProperties props = new GatewayProperties();
        props.setApis(List.of(new ApiRoute("o", "s", "a", "POST", "http://example.com", "/ext", "k", null, null, null)));

        // when
        var found = props.find("o", "s", "a", "post");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().host()).isEqualTo("http://example.com");
    }

    @Test
    // setApis가 재호출되면 캐시가 갱신되는지 검증
    void setApisRebuildsRouteIndex() {
        // given
        GatewayProperties props = new GatewayProperties();
        props.setApis(List.of(new ApiRoute("o1", "s1", "a1", "GET", "http://example.com", "/one", "k1", null, null, null)));

        // when
        props.setApis(List.of(new ApiRoute("o2", "s2", "a2", "POST", "http://example.net", "/two", "k2", null, null, null)));

        // then
        assertThat(props.find("o1", "s1", "a1", "GET")).isEmpty();
        assertThat(props.find("o2", "s2", "a2", "POST")).isPresent();
    }

    @Test
    void protectsRouteListFromExternalMutation() {
        GatewayProperties props = new GatewayProperties();
        var routes = new java.util.ArrayList<ApiRoute>();
        routes.add(new ApiRoute("o", "s", "a", "POST", "http://example.com", "/ext", null, null, null, false));
        props.setApis(routes);
        routes.clear();
        assertThat(props.getApis()).hasSize(1);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> props.getApis().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(props.find("o", "s", "a", "POST")).isPresent();
    }

    @Test
    void rejectsDuplicateKeysWithoutReplacingExistingConfiguration() {
        GatewayProperties props = new GatewayProperties();
        ApiRoute route = new ApiRoute("o", "s", "a", "POST", "http://example.com", "/ext", null, null, null, false);
        props.setApis(List.of(route));
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> props.setApis(List.of(route, route)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Duplicate gateway route");
        assertThat(props.getApis()).containsExactly(route);
    }

    @Test
    void rejectsMissingMethodWithAnExplicitConfigurationError() {
        GatewayProperties props = new GatewayProperties();
        ApiRoute route = new ApiRoute("o", "s", "a", null, "http://example.com", "/ext", null, null, null, false);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> props.setApis(List.of(route)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("method is required");
    }

    @Test
    void routeDiagnosticsDoNotExposeCredentials() {
        ApiRoute route = new ApiRoute("o", "s", "a", "POST", "http://example.com", "/ext",
                "encryption-secret", "api-secret", "institution-code", true);
        assertThat(route.toString()).contains("org=o", "service=s", "api=a")
                .doesNotContain("encryption-secret", "api-secret", "institution-code");
    }
}
