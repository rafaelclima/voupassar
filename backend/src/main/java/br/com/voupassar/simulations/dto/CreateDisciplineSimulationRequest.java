package br.com.voupassar.simulations.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Criação de simulado por disciplina (TASK 5.1).
 *
 * <p>Disciplina e quantidade são obrigatórias; dificuldade é opcional (NULL =
 * todas — o filtro por dificuldade herda a limitação da estimativa: palpite
 * com confiança BAIXA global, questões sem estimativa ficam fora do recorte).
 * Modo segue o {@code CHECK} da DDL V1 para execuções ({@code ESTUDO/PROVA} —
 * Revisão não gera simulado novo, só filtra erros, ERD §2.6); minúsculas são
 * normalizadas pelo serviço antes de persistir.
 */
public record CreateDisciplineSimulationRequest(
    @Schema(example = "MATEMATICA")
        @NotBlank(message = "Disciplina é obrigatória.")
        String disciplineCode,
    @Schema(example = "10", description = "Quantidade de questões [1–100], validada contra o banco disponível.")
        @NotNull(message = "Quantidade é obrigatória.")
        @Min(value = 1, message = "Quantidade deve ser >= 1.")
        @Max(value = 100, message = "Quantidade deve ser <= 100.")
        Integer questionCount,
    @Schema(example = "MEDIA", nullable = true, description = "FACIL, MEDIA ou DIFICIL (NULL = todas).")
        @Pattern(
            regexp = "(?i)FACIL|MEDIA|DIFICIL",
            message = "Dificuldade deve ser FACIL, MEDIA ou DIFICIL.")
        String difficulty,
    @Schema(example = "PROVA")
        @NotBlank(message = "Modo é obrigatório.")
        @Pattern(
            regexp = "(?i)ESTUDO|PROVA",
            message = "Modo deve ser ESTUDO ou PROVA.")
        String mode) {}
