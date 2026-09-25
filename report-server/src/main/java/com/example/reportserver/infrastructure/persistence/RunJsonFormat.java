package com.example.reportserver.infrastructure.persistence;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

abstract class RunJsonFormat {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    abstract LocalDateTime getStartedAt();
}
