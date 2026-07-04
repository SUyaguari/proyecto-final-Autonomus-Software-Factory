package org.ups.inscripcionoratorio.shared.infrastructure.web;

import java.time.OffsetDateTime;
import java.util.List;

public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> camposFaltantes) {

    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, null);
    }

    public static ErrorResponse of(int status, String error, String message, String path, List<String> camposFaltantes) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, path, camposFaltantes);
    }
}
