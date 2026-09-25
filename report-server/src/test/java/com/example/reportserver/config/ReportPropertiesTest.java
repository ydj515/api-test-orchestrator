package com.example.reportserver.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ReportPropertiesTest {
    private final ApplicationContextRunner context = new ApplicationContextRunner().withUserConfiguration(Config.class);

    @EnableConfigurationProperties(ReportProperties.class)
    static class Config { }

    @Test
    void bindsPathsThroughACompleteImmutableConfiguration() {
        context.withPropertyValues("report.data-dir=./runs", "report.gateway-contract-catalog-path=./catalog.yaml")
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx.getBean(ReportProperties.class).storagePath()).isAbsolute();
                });
    }

    @Test
    void blankRequiredPathFailsStartup() {
        context.withPropertyValues("report.data-dir=", "report.gateway-contract-catalog-path=./catalog.yaml")
                .run(ctx -> assertThat(ctx).hasFailed());
    }
}
