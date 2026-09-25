package com.example.reportserver.application.run.port.out;

import com.example.reportserver.domain.run.model.TestCase;
import com.example.reportserver.domain.run.model.TestRun;
import java.util.List;
import java.util.Optional;

public interface RunReader {
    List<TestRun> listAllRuns();
    Optional<TestRun> findRun(String runId);
    List<TestCase> findCases(String runId);
}
