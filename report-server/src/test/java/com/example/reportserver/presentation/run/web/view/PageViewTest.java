package com.example.reportserver.presentation.run.web.view;

import com.example.reportserver.domain.run.model.TestRun;
import com.example.reportserver.domain.run.model.TestStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PageViewTest {
    @Test
    void jsonCannotTerminateScriptAndRoundTripsOriginalText() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String dangerous = "</script><script>alert('x')</script>&\u2028\u2029";
        String json = new PageJson(mapper).write(List.of(dangerous));
        assertThat(json).doesNotContain("<", ">", "&", "\u2028", "\u2029");
        assertThat(mapper.readTree(json).get(0).textValue()).isEqualTo(dangerous);
    }

    @Test
    void snapshotDoesNotChangeWhenStoredRunIsMutated() {
        TestRun run = TestRun.builder().id("run").failCount(1).durationMs(1200).build();
        RunView view = RunView.from(run);
        run.setFailCount(0);
        assertThat(view.status()).isEqualTo(TestStatus.FAIL);
        assertThat(view.startedAtLabel()).isEqualTo("시각 정보 없음");
        assertThat(view.durationLabel()).isEqualTo("1.2s");
    }
}
