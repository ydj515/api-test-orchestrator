package com.example.reportserver.application.run.result;

import com.example.reportserver.domain.run.model.TestCase;
import com.example.reportserver.domain.run.model.TestRun;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.umd.cs.findbugs.annotations.SuppressMatchType;
import java.util.List;
import java.util.Objects;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
        matchType = SuppressMatchType.EXACT,
        justification = "Internal result carries the mutable run used during publication and view mapping")
public record ParsedRun(TestRun run, List<TestCase> cases) {
    public ParsedRun {
        Objects.requireNonNull(run, "run");
        cases = List.copyOf(cases);
    }
}
