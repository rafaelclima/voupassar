package br.com.voupassar.exams.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Detalhe de uma edição: versões + documentos + prompt da discursiva. */
@Schema(description = "Detalhe de uma edição. Estrutura pertence àquela edição (nunca regra universal).")
public record EditionDetailResponse(
    int year,
    @Schema(description = "Processo seletivo: IFRN ou EAJ (TASK C.1).", example = "IFRN")
        String institution,
    String edital,
    int durationMinutes,
    int objectiveCount,
    int lpCount,
    int matCount,
    @Schema(description = "Contagem de Ciências da Natureza daquela edição (V17; C.2 semeia CN/CH).")
        int cnCount,
    @Schema(description = "Contagem de Ciências Humanas daquela edição (V17; C.2 semeia CN/CH).")
        int chCount,
    boolean hasEssay,
    @Schema(description = "NULL = DESCONHECIDA.", nullable = true) String scoringRule,
    List<ExamVersionResponse> versions,
    @Schema(nullable = true) EssayPromptResponse essayPrompt) {}
