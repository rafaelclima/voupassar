package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Estatísticas de uma edição.
 *
 * <p>Distingue <b>esperado</b> (o que a capa/documentos declaram para aquela
 * edição) de <b>importado</b> (o que o banco contém via TASK 2.3). Em um banco
 * recém-migrado sem importação, {@code questionsImported = 0} — sem inventar.
 */
@Schema(description = "Estatísticas da edição: esperado (capa) × importado (banco).")
public record EditionStatsResponse(
    int year,
    String edital,
    @Schema(example = "40") int objectiveExpected,
    @Schema(example = "40") long questionsImported,
    @Schema(example = "39") long confirmed,
    @Schema(description = "Questões com X no gabarito.", example = "1") long annulled,
    List<DisciplineStatResponse> perDiscipline,
    @Schema(example = "2") long documentCount,
    @Schema(example = "1") long versionCount,
    boolean hasEssay,
    @Schema(description = "false = scoring_rule DESCONHECIDA.", example = "false")
        boolean scoringRuleKnown,
    List<String> notes) {}
