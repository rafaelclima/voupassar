package br.com.voupassar.content.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Detalhe {@code GET /api/v1/subtopics/{id}} com série histórica por edição. */
@Schema(description = "Subassunto com distribuição histórica por edição.")
public record SubtopicDetailResponse(
    @Schema(example = "25") long id,
    @Schema(example = "EQUACOES") String code,
    @Schema(example = "Equações") String name,
    @Schema(example = "5") long topicId,
    @Schema(example = "ALGEBRA") String topicCode,
    @Schema(example = "Álgebra") String topicName,
    @Schema(example = "MATEMATICA") String disciplineCode,
    @Schema(example = "Matemática") String disciplineName,
    boolean active,
    @Schema(description = "Questões classificadas neste subassunto (não-rejeitadas).", example = "8")
        long questionCount,
    @Schema(description = "Edições em que o subassunto aparece.") List<Integer> editions,
    List<EditionCountResponse> perEdition,
    ConfidenceBreakdownResponse confidence) {}
