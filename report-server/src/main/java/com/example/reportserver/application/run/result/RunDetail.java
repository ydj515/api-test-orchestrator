package com.example.reportserver.application.run.result;

import com.example.reportserver.domain.run.model.TestCase;
import com.example.reportserver.domain.run.model.TestCaseKind;
import com.example.reportserver.domain.run.model.TestRun;
import java.util.List;

public record RunDetail(TestRun run, List<TestCase> cases, List<String> availableApis,
                        List<TestCaseKind> availableKinds) {
    public RunDetail {
        cases = List.copyOf(cases);
        availableApis = List.copyOf(availableApis);
        availableKinds = List.copyOf(availableKinds);
    }
}
