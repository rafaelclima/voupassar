package br.com.voupassar.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Pedido de criação da sessão de revisão (TASK 5.4).
 *
 * <p>Os filtros são os mesmos da fila (TASK 4.5): o caderno congela o top-N
 * da fila ordenada deterministicamente no momento da criação. Validação de
 * domínio (disciplina/assunto inexistentes → 404) acontece no serviço via
 * {@code ReviewService} — aqui só o formato (borda → 400
 * {@code VALIDATION_ERROR}).
 */
public record CreateReviewSessionRequest(
    @Schema(description = "Máximo de itens do caderno [1–100] (padrão 20).",
        example = "20", nullable = true)
    @Min(value = 1, message = "Limite deve ser 1–100.")
    @Max(value = 100, message = "Limite deve ser 1–100.")
    Integer limit,
    @Schema(description = "Filtro por disciplina (ex. MATEMATICA).",
        example = "MATEMATICA", nullable = true)
    String discipline,
    @Schema(description = "Filtro por assunto (id do tópico).",
        example = "5", nullable = true)
    Long topicId,
    @Schema(description = "Quando true, só erros (ERRO_SEM_ACERTO e ERRO_RECENTE).",
        example = "false", nullable = true)
    Boolean onlyErrors) {}
