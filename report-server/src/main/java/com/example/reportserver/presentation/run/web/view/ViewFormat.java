package com.example.reportserver.presentation.run.web.view;

import com.example.reportserver.domain.run.model.TestStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class ViewFormat {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private ViewFormat() { }

    static String dateTime(LocalDateTime value) {
        return value == null ? "시각 정보 없음" : value.format(DATE_TIME);
    }

    static String duration(long milliseconds) {
        return String.format(Locale.ROOT, "%.1fs", milliseconds / 1000.0);
    }

    static String badge(TestStatus status) {
        if (status == null) {
            return "text-bg-secondary";
        }
        return status == TestStatus.FAIL ? "text-bg-danger" : "text-bg-success";
    }
}
