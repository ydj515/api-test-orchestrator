package com.example.reportserver.infrastructure.persistence;

import com.example.reportserver.application.run.result.ParsedRun;
import com.example.reportserver.domain.run.model.TestRun;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileRunPublicationStoreTest {
    @TempDir Path directory;

    @Test
    void parserFailureLeavesNoPublishedOrStagedRunAndPreservesCause() throws Exception {
        Path source = Files.createDirectory(directory.resolve("source"));
        Files.writeString(source.resolve("index.html"), "report");
        Path runs = directory.resolve("runs");
        var publisher = new FileRunPublicationStore(new RunStorageService(runs, new ObjectMapper()));
        var original = new IllegalArgumentException("invalid report");
        assertThatThrownBy(() -> publisher.publish("failed-run", source, staged -> { throw original; }))
                .isSameAs(original);
        try (var children = Files.list(runs)) {
            assertThat(children.toList()).isEmpty();
        }
    }

    @Test
    void incompleteStagingDirectoryIsNeverListed() throws Exception {
        var storage = new RunStorageService(directory, new ObjectMapper());
        storage.writeRun(directory.resolve(".run.tmp-staging"), TestRun.builder().id("run").build());
        assertThat(storage.listAllRuns()).isEmpty();
        storage.saveRun(TestRun.builder().id(".complete").build());
        assertThat(storage.listAllRuns()).extracting(TestRun::getId).containsExactly(".complete");
    }

    @Test
    void repeatedPublicationDoesNotOverwriteAnExistingRun() throws Exception {
        Path source = Files.createDirectory(directory.resolve("source"));
        Files.writeString(source.resolve("index.html"), "report");
        var storage = new RunStorageService(directory.resolve("runs"), new ObjectMapper());
        var publisher = new FileRunPublicationStore(storage);
        publisher.publish("existing", source, staged -> new ParsedRun(TestRun.builder().id("existing").build(), List.of()));
        assertThatThrownBy(() -> publisher.publish("existing", source, staged -> {
            throw new AssertionError("Existing run must be rejected before parsing");
        })).isInstanceOf(IllegalStateException.class);
        assertThat(storage.findRun("existing")).isPresent();
    }
}
