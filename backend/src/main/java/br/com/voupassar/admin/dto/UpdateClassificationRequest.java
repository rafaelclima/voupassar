package br.com.voupassar.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Revisão humana de uma classificação (TASK 12.2, operada pela 12.1).
 *
 * <p>Carimba {@code reviewed_by/at} com o curador autenticado. A aprovação é
 * terminal: sair de {@code APPROVED} só para {@code REJECTED} (revogação),
 * nunca de volta a {@code PENDING} — o índice parcial
 * {@code uq_qclass_approved} garante uma vigente por questão.
 */
public record UpdateClassificationRequest(
    @Schema(example = "APPROVED")
        @NotBlank(message = "status é obrigatório.")
        @Pattern(
            regexp = "REVIEWED|APPROVED|REJECTED",
            message = "status deve ser REVIEWED, APPROVED ou REJECTED.")
        String status,
    @Schema(
            example = "Assunto confirmado contra o caderno, pág. 12.",
            nullable = true)
        @Size(max = 2000, message = "observation deve ter até 2000 caracteres.")
        String observation) {}
