package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Contagem importada por disciplina dentro de uma edição. */
@Schema(description = "Questões importadas (TASK 2.3) por disciplina na edição.")
public record DisciplineStatResponse(
    @Schema(example = "LINGUA_PORTUGUESA") String disciplineCode,
    @Schema(example = "Língua Portuguesa") String disciplineName,
    @Schema(description = "Esperado pelo caderno daquela edição.", example = "20")
        int expected,
    @Schema(example = "20") long imported,
    @Schema(description = "Anuladas (X no gabarito) — contam como conteúdo, sem pontuar.", example = "1")
        long annulled) {}
