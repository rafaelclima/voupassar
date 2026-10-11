package br.com.voupassar.aiprova.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Pedido de geração de prova-IA (TASK A.3).
 *
 * <p>{@code institution} é obrigatória (IFRN|EAJ — sem default global);
 * {@code editionModel} ausente = edição mais recente da trilha no banco
 * (nunca inventada); {@code mode} é ESTUDO ou PROVA.
 */
public record AiGenerateRequest(
    @Schema(example = "IFRN", description = "Processo existente: IFRN ou EAJ (obrigatório).")
    String institution,
    @Schema(example = "IFRN-2026",
        description = "Edição-modelo vigente (só edições presentes; ausente = mais recente da trilha).")
    String editionModel,
    @Schema(example = "PROVA", description = "ESTUDO (feedback imediato) ou PROVA (gabarito oculto).")
    String mode) {}
