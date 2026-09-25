package com.example.reportserver.application.run.result;

import com.example.reportserver.domain.run.model.TestCase;
import com.example.reportserver.domain.run.model.TestRun;
import java.util.List;
import java.util.Objects;

public record ParsedRun(TestRun run, List<TestCase> cases) {
    public ParsedRun {
        Objects.requireNonNull(run, "run");
        cases = List.copyOf(cases);
    }
}
