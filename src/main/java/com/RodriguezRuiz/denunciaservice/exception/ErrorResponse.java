package com.RodriguezRuiz.denunciaservice.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime timestamp,
        int status,
        String error,
        String mensaje,
        Map<String, String> detalles
) {
    public ErrorResponse(int status, String error, String mensaje) {
        this(LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS), status, error, mensaje, null);
    }

    public ErrorResponse(int status, String error, String mensaje, Map<String, String> detalles) {
        this(LocalDateTime.now(ZoneId.of("America/Lima")).truncatedTo(ChronoUnit.SECONDS), status, error, mensaje, detalles);
    }
}
