package br.com.voupassar.studyplan.dto;

import java.util.List;

/**
 * Resposta do motor de recomendação (TASK 4.3) — determinística e explicável.
 */
public record RecommendationResponse(
    Long userId,
    String algorithmVersion,
    String note,
    List<RecommendationItem> recommendations
) {

  public static record RecommendationItem(
      Long topicId,
      String topicCode,
      String topicName,
      Short priority,
      String reason,
      String evidenceJson,
      String status
  ) {}
}
