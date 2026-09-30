package br.com.voupassar.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Envelope de erro consistente (docs/architecture.md §3):
 * {@code {code, message, details, traceId, timestamp, path}}.
 *
 * <p>Nunca inclui stack trace — tratamento seguro de erros
 * (AGENTS.md §15).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    String code,
    String message,
    List<String> details,
    String traceId,
    OffsetDateTime timestamp,
    String path) {}
