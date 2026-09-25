package com.example.reportserver.application.run.service;

import com.example.reportserver.application.run.port.in.RunPublisher;
import com.example.reportserver.application.run.port.out.ReportParser;
import com.example.reportserver.application.run.port.out.RunPublicationStore;
import com.example.reportserver.application.run.result.ParsedRun;
import com.example.reportserver.domain.catalog.model.GatewayContract;
import com.example.reportserver.domain.run.model.TestCaseGranularity;
import com.example.reportserver.domain.run.model.TestRun;
import com.example.reportserver.domain.run.model.TestSource;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RunPublishService implements RunPublisher {
    private static final DateTimeFormatter RUN_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");
    private static final Pattern RUN_ID_PATTERN = Pattern.compile("(?=.*[A-Za-z0-9])[A-Za-z0-9._-]+");

    private final ReportParser parser;
    private final RunPublicationStore publications;
    private final Clock clock;

    public String publishKarate(String runId, Path reportDir, GatewayContract contract,
                                String api, TestCaseGranularity caseGranularity) {
        validateContract(contract);
        String effectiveRunId = canonicalRunId(runId, TestSource.KARATE);
        TestCaseGranularity granularity = caseGranularity == null ? TestCaseGranularity.BOTH : caseGranularity;
        publications.publish(effectiveRunId, reportDir, stagedReport -> {
            ParsedRun parsed = parser.parse(TestSource.KARATE, stagedReport, effectiveRunId,
                    contract.getOrg(), contract.getService(), api, granularity);
            applyContractMetadata(parsed.run(), contract, api);
            return parsed;
        });
        return effectiveRunId;
    }

    public String publishCats(String runId, Path reportDir, GatewayContract contract, String api) {
        validateContract(contract);
        String effectiveRunId = canonicalRunId(runId, TestSource.CATS);
        publications.publish(effectiveRunId, reportDir, stagedReport -> {
            ParsedRun parsed = parser.parse(TestSource.CATS, stagedReport, effectiveRunId,
                    contract.getOrg(), contract.getService(), api, TestCaseGranularity.BOTH);
            applyContractMetadata(parsed.run(), contract, api);
            return parsed;
        });
        return effectiveRunId;
    }

    private String generateRunId(TestSource source) {
        String prefix = source == TestSource.CATS ? "cats" : "karate";
        return prefix + "-" + LocalDateTime.now(clock).format(RUN_ID_FORMATTER) + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String canonicalRunId(String runId, TestSource source) {
        String effectiveRunId = runId == null || runId.isBlank() ? generateRunId(source) : runId.trim();
        if (!RUN_ID_PATTERN.matcher(effectiveRunId).matches()) {
            throw new IllegalArgumentException("Run ID may contain only letters, numbers, dots, underscores, and hyphens: " + runId);
        }
        return effectiveRunId;
    }

    private void validateContract(GatewayContract contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Gateway contract is required");
        }
    }

    private void applyContractMetadata(TestRun run, GatewayContract contract, String api) {
        run.setOrg(contract.getOrg());
        run.setService(contract.getService());
        run.setApi(api);
        run.setOperationId(contract.findOperationId(api));
        run.setContractId(contract.getId());
        run.setContractPath(contract.getContractPath());
        run.setContractChecksum(contract.getChecksum());
    }

}
