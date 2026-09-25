package com.example.reportserver.controller;

import com.example.reportserver.model.TestCase;
import com.example.reportserver.model.TestCaseKind;
import com.example.reportserver.model.TestCaseType;
import com.example.reportserver.model.TestRun;
import com.example.reportserver.model.TestSource;
import com.example.reportserver.model.TestStatus;
import com.example.reportserver.service.RunStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportViewTest {

    @TempDir
    static Path dataDir;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("report.data-dir", () -> dataDir.toString());
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    RunStorageService storage;

    @BeforeEach
    void prepareRun() {
        storage.saveRun(TestRun.builder()
                .id("ui-view-test").org("orgA").service("reservation").source(TestSource.KARATE)
                .startedAt(LocalDateTime.of(2026, 9, 25, 10, 30))
                .totalCount(2).passCount(1).failCount(1).durationMs(1200).build());
        storage.saveCases("ui-view-test", List.of(
                TestCase.builder().id("failed").runId("ui-view-test").caseType(TestCaseType.SCENARIO)
                        .kind(TestCaseKind.SINGLE_API).api("createReservation").name("Failed case")
                        .status(TestStatus.FAIL).httpMethod("POST").httpStatus(500)
                        .endpoint("/api/reservations").failureMsg("<script>alert('unsafe')</script>\nExpected 201")
                        .build(),
                TestCase.builder().id("passed").runId("ui-view-test").caseType(TestCaseType.SCENARIO)
                        .api("listResources").name("Passed case").status(TestStatus.PASS).build()));
    }

    @Test
    void serviceOverviewRendersCatalogAndUnrunState() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("서비스별 테스트 현황")))
                .andExpect(content().string(containsString("/services/orgA/reservation")))
                .andExpect(content().string(containsString("미실행")));
    }

    @Test
    void historyKeepsDeepLinkedFiltersAndDetailNavigation() throws Exception {
        mvc.perform(get("/services/orgA/reservation").param("status", "FAIL").param("caseName", "Failed"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("value=\"Failed\"")))
                .andExpect(content().string(containsString("/runs/ui-view-test")))
                .andExpect(content().string(containsString("data-status-control=\"historyStatusFilter\"")))
                .andExpect(content().string(containsString("data-metadata-complete=\"true\"")));
    }

    @Test
    void detailEscapesMessagesWhileExposingSelectableCasesAndRawReport() throws Exception {
        mvc.perform(get("/runs/ui-view-test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aria-controls=\"caseInspector\"")))
                .andExpect(content().string(containsString("data-failure=\"&lt;script&gt;")))
                .andExpect(content().string(not(containsString("<script>alert('unsafe')</script>"))))
                .andExpect(content().string(containsString("/reports/ui-view-test/report/karate-summary.html")))
                .andExpect(content().string(containsString("Passed case")));
    }

    @Test
    void catsDetailRendersHttpCasesWithoutInventingScenarioKinds() throws Exception {
        storage.saveRun(TestRun.builder().id("cats-ui-test").org("catsOrg").service("booking")
                .source(TestSource.CATS).totalCount(1).passCount(1).build());
        storage.saveCases("cats-ui-test", List.of(TestCase.builder().id("http-case")
                .caseType(TestCaseType.HTTP_CALL).name("CATS call").status(TestStatus.PASS).build()));
        mvc.perform(get("/runs/cats-ui-test"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("CATS call")))
                .andExpect(content().string(not(containsString("id=\"kindFilter\""))))
                .andExpect(content().string(containsString("시각 정보 없음")));
    }

    @Test
    void breadcrumbsRespectServletContextPath() throws Exception {
        mvc.perform(get("/report/services/orgA/reservation").contextPath("/report"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/report/\"")));
        mvc.perform(get("/report/runs/ui-view-test").contextPath("/report"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/report/services/orgA/reservation\"")));
    }

    @Test
    void emptyHistoryAndMissingRunHaveExplicitResponses() throws Exception {
        mvc.perform(get("/services/orgB/visit"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("historyEmptyState")));
        mvc.perform(get("/runs/nonexistent-ui-run")).andExpect(status().isNotFound());
    }
}
