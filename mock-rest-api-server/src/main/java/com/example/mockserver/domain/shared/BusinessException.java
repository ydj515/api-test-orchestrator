package com.example.mockserver.domain.shared;

public class BusinessException extends RuntimeException {
    public enum Kind { INVALID_INPUT, NOT_FOUND, CONFLICT }

    private final String code;
    private final Kind kind;

    public BusinessException(String code, String message, Kind kind) {
        super(message);
        this.code = code;
        this.kind = kind;
    }

    public String getCode() { return code; }
    public Kind getKind() { return kind; }
}
