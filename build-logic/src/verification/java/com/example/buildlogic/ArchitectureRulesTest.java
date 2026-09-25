package com.example.buildlogic;

/** No additional test dependency is needed to verify the package policy. */
public final class ArchitectureRulesTest {
    public static void main(String[] args) {
        String graph = "<!-- gradle-module-graph:start -->\n:gateway -> []\n<!-- gradle-module-graph:end -->";
        ModuleGraphCheck.verifyGraph(graph, java.util.List.of(":gateway -> []"));
        expectGraphFailure(graph, java.util.List.of(":gateway -> [:report-server]"));
        expectGraphFailure("missing markers", java.util.List.of(":gateway -> []"));
        for (String base : java.util.List.of("com.example.gateway", "com.example.mockserver", "com.example.reportserver")) {
            expect(true, base, "application.run.service.Publish", "presentation.run.web.Controller");
            expect(true, base, "application.run.service.Publish", "infrastructure.parser.Parser");
            expect(true, base, "application.run.service.Publish", "config.Properties");
            expect(true, base, "domain.run.model.Run", "application.run.service.Publish");
            expect(true, base, "presentation.run.web.Controller", "application.run.port.out.Store");
            expect(true, base, "presentation.run.web.Controller", "infrastructure.persistence.Store");
            expect(true, base, "presentation.run.web.Controller", "domain.booking.BookingOperations");
            expect(true, base, "infrastructure.persistence.Store", "application.run.service.Publish");
            expect(true, base, "service.LegacyService", "domain.run.model.Run");
            expect(true, base, "RootHelper", "application.run.service.Publish");
            expect(true, base, "presentation.run.web.Controller", "RootHelper");
            expect(false, base, "application.run.service.Publish", "application.run.port.out.Store");
            expect(false, base, "application.run.service.Publish", "domain.run.model.Run");
            expect(false, base, "infrastructure.persistence.Store", "application.run.port.out.Store");
            expect(false, base, "presentation.run.web.Controller", "application.run.port.in.Publish");
            expect(false, base, "presentation.run.web.Controller", "domain.run.model.Run");
            expect(false, base, "config.Composition", "infrastructure.persistence.Store");
            expectExternal(true, base, "domain.run.model.Run", "org.springframework.stereotype.Component");
            expectExternal(true, base, "domain.run.model.Run", "com.fasterxml.jackson.annotation.JsonFormat");
            expectExternal(true, base, "application.run.service.Publish", "org.springframework.http.ResponseEntity");
            expectExternal(true, base, "application.run.service.Publish", "com.fasterxml.jackson.databind.ObjectMapper");
            expectExternal(true, base, "presentation.run.web.Controller", "org.springframework.transaction.annotation.Transactional");
            expectExternal(false, base, "application.run.service.Publish", "org.springframework.stereotype.Service");
            expectExternal(false, base, "application.run.service.Publish", "org.springframework.transaction.annotation.Transactional");
        }
        expectExternal(true, "karate", "Runner", "com.example.gateway.application.proxy.service.Proxy");

    }

    private static void expectExternal(boolean forbidden, String base, String source, String target) {
        if (ArchitectureCheck.forbidden(base, base + "." + source, target) != forbidden) {
            throw new AssertionError("Unexpected external dependency policy: " + source + " -> " + target);
        }
    }

    private static void expectGraphFailure(String document, java.util.List<String> actual) {
        try {
            ModuleGraphCheck.verifyGraph(document, actual);
        } catch (org.gradle.api.GradleException expected) {
            return;
        }
        throw new AssertionError("Invalid module documentation must fail verification");
    }

    private static void expect(boolean forbidden, String base, String source, String target) {
        if (ArchitectureCheck.forbidden(base, base + "." + source, base + "." + target) != forbidden) {
            throw new AssertionError("Unexpected dependency policy: " + source + " -> " + target);
        }
    }
}
