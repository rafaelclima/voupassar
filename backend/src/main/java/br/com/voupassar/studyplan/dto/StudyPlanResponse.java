package br.com.voupassar.studyplan.dto;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Roteiro vigente do aluno (TASK 16.1) — DTO sem vazar entidade JPA.
 *
 * <p>Substitui o retorno direto de {@code StudyPlan} no
 * {@code RecommendationController}: mesmos campos que o frontend já consome
 * ({@code id, userId, isActive, algorithmVersion, generatedAt, items}),
 * sem proxy Hibernate nem referência reversa.
 */
public record StudyPlanResponse(
    Long id,
    Long userId,
    Boolean isActive,
    String algorithmVersion,
    String status,
    OffsetDateTime generatedAt,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    List<StudyPlanItemResponse> items
) {}
