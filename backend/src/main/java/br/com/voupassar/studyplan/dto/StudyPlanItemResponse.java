package br.com.voupassar.studyplan.dto;

import java.time.OffsetDateTime;

/**
 * Item do roteiro de estudos (TASK 16.1) — DTO sem vazar entidade JPA.
 *
 * <p>Espelha os campos que o frontend já consome
 * ({@code id, topicId, subtopicId, priority, reason, evidenceJson, status});
 * {@code StudyPlanItem.studyPlan} nunca é serializado (era {@code @JsonIgnore}
 * na entidade para evitar recursão — agora nem chega ao JSON).
 */
public record StudyPlanItemResponse(
    Long id,
    Long topicId,
    Long subtopicId,
    Short priority,
    String reason,
    String evidenceJson,
    String status,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
