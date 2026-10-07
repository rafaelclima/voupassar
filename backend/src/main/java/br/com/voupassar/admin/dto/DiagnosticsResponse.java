package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

/**
 * Diagnóstico técnico da API (TASK 22.2) — só `CURATOR`/`ADMIN`.
 *
 * <p>Sem PII, sem conteúdo de questão: serviço, versão, banco, migração,
 * uptime e contadores de eventos de negócio (`TechMetrics`). O health
 * público (`GET /api/v1/health`) continua mínimo.
 */
public record DiagnosticsResponse(
    @Schema(example = "voupassar-backend") String service,
    @Schema(example = "0.1.0") String version,
    @Schema(example = "UP", allowableValues = {"UP", "DOWN"}) String dbStatus,
    @Schema(example = "V16", description = "Última migration aplicada (flyway_schema_history).") String lastMigration,
    @Schema(example = "3600000", description = "Uptime da JVM em ms.") long uptimeMillis,
    @Schema(description = "Contadores voupassar.* (por instância, desde o boot).")
    Map<String, Double> metrics) {}
