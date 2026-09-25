package com.example.reportserver.infrastructure.persistence;

import com.example.reportserver.domain.run.model.TestRun;
import com.example.reportserver.domain.run.model.TestSource;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.file.Path;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

class RunStorageServiceTest {

    @TempDir
    Path dataDir;

    private RunStorageService storageService;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        storageService = new RunStorageService(dataDir, objectMapper);
    }

    @Test
    void findRun_legacyKarateReportPath를SummaryHtml로보정한다() {
        storageService.saveRun(run("karate-legacy", TestSource.KARATE, "/reports/karate-legacy"));

        TestRun run = storageService.findRun("karate-legacy").orElseThrow();

        assertThat(run.getReportPath()).isEqualTo("/reports/karate-legacy/report/karate-summary.html");
    }

    @Test
    void listAllRuns_legacyCatsReportPath를IndexHtml로보정한다() {
        storageService.saveRun(run("cats-legacy", TestSource.CATS, "/reports/cats-legacy"));

        assertThat(storageService.listAllRuns())
                .singleElement()
                .extracting(TestRun::getReportPath)
                .isEqualTo("/reports/cats-legacy/report/index.html");
    }

    @Test
    void findRun_정상ReportPath는유지한다() {
        storageService.saveRun(run(
                "karate-current",
                TestSource.KARATE,
                "/reports/karate-current/report/karate-summary.html"
        ));

        TestRun run = storageService.findRun("karate-current").orElseThrow();

        assertThat(run.getReportPath()).isEqualTo("/reports/karate-current/report/karate-summary.html");
    }

    private TestRun run(String runId, TestSource source, String reportPath) {
        return TestRun.builder()
                .id(runId)
                .org("catsOrg")
                .service("booking")
                .source(source)
                .startedAt(LocalDateTime.of(2026, 4, 23, 10, 0))
                .durationMs(1)
                .totalCount(1)
                .passCount(1)
                .failCount(0)
                .reportPath(reportPath)
                .build();
    }
    @Test
    void storageOwnsTimestampFormatWithoutMutatingSharedMapper() throws Exception {
        ObjectMapper shared = new ObjectMapper().registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var storage = new RunStorageService(dataDir, shared);
        TestRun run = run("timestamp", TestSource.KARATE, "/reports/timestamp");
        run.setStartedAt(LocalDateTime.of(2026, 9, 25, 10, 30, 0, 123_000_000));
        storage.saveRun(run);

        assertThat(java.nio.file.Files.readString(dataDir.resolve("timestamp/meta.json")))
                .contains("2026-09-25T10:30:00\"")
                .doesNotContain("2026-09-25T10:30:00.123");
        assertThat(storage.findRun("timestamp").orElseThrow().getStartedAt())
                .isEqualTo(run.getStartedAt().withNano(0));
        assertThat(shared.writeValueAsString(run)).contains("2026-09-25T10:30:00.123");
    }

}
