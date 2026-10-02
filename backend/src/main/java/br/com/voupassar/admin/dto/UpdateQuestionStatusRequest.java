package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

/**
 * Atualização dos status de curadoria de uma questão (TASK 12.1).
 *
 * <p>Ao menos um campo deve ser informado. Regra auditável (TASK 11.1 §7):
 * {@code PUBLICAVEL} exige questão {@code APPROVED} — nunca publicar dado
 * não revisado, nem silenciosamente.
 */
public record UpdateQuestionStatusRequest(
    @Schema(example = "REVIEWED", nullable = true)
        @Pattern(
            regexp = "PENDING|REVIEWED|APPROVED|REJECTED",
            message = "validationStatus deve ser PENDING, REVIEWED, APPROVED ou REJECTED.")
        String validationStatus,
    @Schema(example = "SOMENTE_REFERENCIA", nullable = true)
        @Pattern(
            regexp = "PUBLICAVEL|NAO_PUBLICAVEL|PENDENTE_REVISAO|SOMENTE_REFERENCIA",
            message =
                "publicationStatus deve ser PUBLICAVEL, NAO_PUBLICAVEL,"
                    + " PENDENTE_REVISAO ou SOMENTE_REFERENCIA.")
        String publicationStatus) {}
