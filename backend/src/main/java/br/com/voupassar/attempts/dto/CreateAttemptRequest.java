package br.com.voupassar.attempts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/**
 * Registro de tentativa (TASK 3.7).
 *
 * <p>Campos calculados pelo servidor (nunca pelo cliente): {@code isCorrect},
 * {@code wasAnnulled} (do gabarito vigente da questão) e {@code answeredAt}
 * (relógio do servidor). {@code selectedOption} aceita {@code A/B/C/D} ou
 * {@code BLANK} (em branco — conta como erro quando a questão não é anulada,
 * nunca como acerto). {@code timeSpentSeconds} é opcional (NULL = não
 * medido). Pelo menos um vínculo é obrigatório ({@code studySessionId} ou
 * {@code simulationAttemptId}) — resposta órfã é proibida pelo ERD §5/6.
 */
public record CreateAttemptRequest(
    @Schema(example = "12") @NotNull(message = "Questão é obrigatória.") @Positive(message = "Questão inválida.")
        Long questionId,
    @Schema(example = "C", description = "A, B, C, D ou BLANK (em branco).")
        @NotBlank(message = "Resposta é obrigatória.")
        @Pattern(
            regexp = "(?i)A|B|C|D|BLANK",
            message = "Resposta deve ser A, B, C, D ou BLANK.")
        String selectedOption,
    @Schema(example = "ESTUDO")
        @NotBlank(message = "Modo é obrigatório.")
        @Pattern(
            regexp = "(?i)ESTUDO|PROVA|REVISAO",
            message = "Modo deve ser ESTUDO, PROVA ou REVISAO.")
        String mode,
    @Schema(example = "95", nullable = true)
        @Min(value = 0, message = "Tempo deve ser >= 0 segundos.")
        Integer timeSpentSeconds,
    @Schema(nullable = true, description = "Sessão do dono do token (fluxo da TASK 3.7).")
        @Positive(message = "Sessão inválida.")
        Long studySessionId,
    @Schema(nullable = true, description = "Execução de simulado do dono (Fase 5 cria; aqui só valida).")
        @Positive(message = "Simulado inválido.")
        Long simulationAttemptId) {}
