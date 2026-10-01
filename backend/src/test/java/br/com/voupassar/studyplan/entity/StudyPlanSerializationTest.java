package br.com.voupassar.studyplan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Regressão TASK 6.4: {@code StudyPlan} era serializado com a referência
 * reversa {@code StudyPlanItem.studyPlan}, gerando recursão infinita
 * (plan.items[].studyPlan.items...) e 500 em GET/POST do roteiro.
 */
class StudyPlanSerializationTest {

  @Test
  void planWithItemsSerializesWithoutRecursion() throws Exception {
    StudyPlan plan = new StudyPlan(7L, "v1-deterministico");
    ReflectionTestUtils.setField(plan, "id", 1L);
    // OffsetDateTime exigiria o módulo jsr310 — irrelevante para a recursão.
    ReflectionTestUtils.setField(plan, "generatedAt", null);
    ReflectionTestUtils.setField(plan, "createdAt", null);
    ReflectionTestUtils.setField(plan, "updatedAt", null);

    StudyPlanItem item = new StudyPlanItem(plan, 5L, null, (short) 1,
        "motivo auditável", "{\"topic_id\":5}");
    ReflectionTestUtils.setField(item, "id", 11L);
    ReflectionTestUtils.setField(item, "createdAt", null);
    ReflectionTestUtils.setField(item, "updatedAt", null);
    plan.setItems(List.of(item));

    String json = new ObjectMapper().writeValueAsString(plan);

    assertFalse(json.contains("studyPlan"),
        "referência reversa não pode vazar no JSON: " + json);
    JsonNode root = new ObjectMapper().readTree(json);
    assertEquals(1, root.get("id").asInt());
    assertEquals(1, root.get("items").size());
    assertEquals(5, root.get("items").get(0).get("topicId").asInt());
    assertEquals("TODO",
        root.get("items").get(0).get("status").asString());
  }
}
