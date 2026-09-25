package com.example.reportserver.application.run.result;

import com.example.reportserver.domain.run.model.TestCase;
import com.example.reportserver.domain.run.model.TestCaseKind;
import com.example.reportserver.domain.run.model.TestRun;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.SuppressMatchType;
import java.util.List;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        matchType = SuppressMatchType.EXACT,
        justification = "Internal result carries the mutable run used during publication and view mapping")
public record RunDetail(TestRun run, List<TestCase> cases, List<String> availableApis,
                        List<TestCaseKind> availableKinds) {
    public RunDetail {
        cases = List.copyOf(cases);
        availableApis = List.copyOf(availableApis);
        availableKinds = List.copyOf(availableKinds);
    }
}
