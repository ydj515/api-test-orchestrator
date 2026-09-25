package com.example.reportserver.infrastructure.persistence;

import com.example.reportserver.application.run.port.out.RunPublicationStore;
import com.example.reportserver.application.run.result.ParsedRun;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

public final class FileRunPublicationStore implements RunPublicationStore {
    private final RunStorageService storage;

    public FileRunPublicationStore(RunStorageService storage) {
        this.storage = storage;
    }

    @Override
    public synchronized void publish(String runId, Path reportDir, Function<Path, ParsedRun> parseStagedReport) {
        Path finalRunDir = storage.runDir(runId);
        if (!Files.isDirectory(reportDir)) {
            throw new IllegalArgumentException("Report directory does not exist: " + reportDir);
        }
        if (Files.exists(finalRunDir)) {
            throw new IllegalStateException("Run already exists: " + runId);
        }
        Path staging = storage.baseDir().resolve("." + runId + ".tmp-" + UUID.randomUUID());
        try {
            copyDirectory(reportDir, staging.resolve("report"));
            ParsedRun parsed = parseStagedReport.apply(staging.resolve("report"));
            storage.writeRun(staging, parsed.run());
            storage.writeCases(staging, parsed.cases());
            movePublishedRun(staging, finalRunDir);
        } catch (IOException cause) {
            UncheckedIOException failure = new UncheckedIOException("Failed to publish run: " + runId, cause);
            cleanup(staging, failure);
            throw failure;
        } catch (RuntimeException failure) {
            cleanup(staging, failure);
            throw failure;
        }
    }

    private void cleanup(Path staging, RuntimeException failure) {
        try {
            deleteDirectoryIfExists(staging);
        } catch (RuntimeException cleanupFailure) {
            failure.addSuppressed(cleanupFailure);
        }
    }

    private void copyDirectory(Path sourceDir, Path targetDir) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDir)) {
            paths.forEach(source -> copyPath(sourceDir, source, targetDir));
        }
    }

    private void copyPath(Path sourceDir, Path source, Path targetDir) {
        Path target = targetDir.resolve(sourceDir.relativize(source).toString());
        try {
            if (Files.isDirectory(source)) {
                Files.createDirectories(target);
            } else {
                Files.createDirectories(target.getParent());
                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void movePublishedRun(Path tempRunDir, Path finalRunDir) throws IOException {
        try {
            Files.move(tempRunDir, finalRunDir, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(tempRunDir, finalRunDir);
        }
    }

    private void deleteDirectoryIfExists(Path directory) {
        if (!Files.exists(directory)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(this::deletePath);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void deletePath(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
