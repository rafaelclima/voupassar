package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Detalhe de uma edição: versões + documentos + prompt da discursiva. */
@Schema(description = "Detalhe de uma edição. Estrutura pertence àquela edição (nunca regra universal).")
public record EditionDetailResponse(
    int year,
    String edital,
    int durationMinutes,
    int objectiveCount,
    int lpCount,
    int matCount,
    boolean hasEssay,
    @Schema(description = "NULL = DESCONHECIDA.", nullable = true) String scoringRule,
    List<ExamVersionResponse> versions,
    @Schema(nullable = true) EssayPromptResponse essayPrompt) {}
