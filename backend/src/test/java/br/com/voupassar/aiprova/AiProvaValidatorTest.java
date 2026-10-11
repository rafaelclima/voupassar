package br.com.voupassar.aiprova;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Validador estrutural da Prova-IA (TASK A.1) — sem rede.
 *
 * <p>Lote válido passa; cada classe de defeito (N errado, alternativa
 * ausente/duplicada/vazia, resposta fora de A–D, menção a figura/passagem,
 * {@code explanation} presente, markdown, topic inventado, disciplina fora
 * do modelo) é rejeitada com {@code AI_GENERATION_INVALID}.
 */
class AiProvaValidatorTest {

  /** Tópico determinístico por disciplina (um código real do vocabulário vigente). */
  private static String sampleTopic(String discipline) {
    return switch (discipline) {
      case "LINGUA_PORTUGUESA" -> "INTERPRETACAO_TEXTUAL";
      case "MATEMATICA" -> "ALGEBRA";
      case "CIENCIAS_NATUREZA" -> "ECOLOGIA";
      case "CIENCIAS_HUMANAS" -> "HISTORIA_BRASIL";
      default -> "DESCONHECIDO";
    };
  }

  private static String validBatch(String editionModel) {
    AiProvaSpec.EditionSpec spec = AiProvaSpec.MODELS.get(editionModel);
    String format = spec.format();
    List<String> items = new ArrayList<>();
    int n = 0;
    for (var e : spec.byDiscipline().entrySet()) {
      String topic = sampleTopic(e.getKey());
      for (int i = 0; i < e.getValue(); i++) {
        n++;
        items.add("{\"n\": " + n + ", \"discipline\": \"" + e.getKey() + "\", \"topic\": \""
            + topic + "\", \"stem\": \"Enunciado auto-contido número " + n + " sobre "
            + topic.toLowerCase().replace('_', ' ') + "?\", "
            + "\"options\": {\"A\": \"Opção A da " + n + "\", \"B\": \"Opção B da " + n
            + "\", \"C\": \"Opção C da " + n + "\", \"D\": \"Opção D da " + n + "\"}, "
            + "\"answer\": \"A\"}");
      }
    }
    return "{\"format\": \"" + format + "\", \"editionModel\": \"" + editionModel
        + "\", \"questions\": [" + String.join(",", items) + "]}";
  }

  private static AiGenerationInvalidException rejects(String rawJson) {
    AiGenerationInvalidException ex =
        assertThrows(AiGenerationInvalidException.class, () -> AiProvaValidator.validate(rawJson));
    assertEquals("AI_GENERATION_INVALID", ex.getCode());
    return ex;
  }

  @Test
  void validBatchesPassForAllThreeShapes() {
    assertEquals(40, AiProvaValidator.validate(validBatch("IFRN-2026")).size());
    assertEquals(40, AiProvaValidator.validate(validBatch("IFRN-2020")).size());
    assertEquals(40, AiProvaValidator.validate(validBatch("EAJ-2022")).size());
    assertEquals(50, AiProvaValidator.validate(validBatch("EAJ-2021")).size());
    assertEquals(40, AiProvaValidator.validate(validBatch("EAJ-2025")).size());
  }

  @Test
  void absentEditionNeverInvented() {
    // IFRN-2021 não existe no dataset — jamais preencher com inferência.
    String batch = validBatch("IFRN-2026").replace("IFRN-2026", "IFRN-2021");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("editionModel")));
  }

  @Test
  void subtopicOutsideV1ContractRejected() {
    // Reconciliação A.1 (DEV): v1 não tem subtopic (TASKS.md A.1/A.2).
    String batch = validBatch("IFRN-2026")
        .replaceFirst("\"answer\": \"A\"", "\"answer\": \"A\", \"subtopic\": \"INFERENCIA\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("subtopic")));
  }

  @Test
  void quadroAndTirinhaMentionRejected() {
    // Reconciliação A.1 (DEV): quadro/tirinha/foto incorporados do draft VPS.
    String batch = validBatch("EAJ-2021")
        .replaceFirst("Enunciado auto-contido número 31", "No quadro a seguir, questão 31");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("q31") && d.contains("figura")));
  }

  @Test
  void promptVersionedFileExistsWithProhibitions() {
    String prompt = AiProvaPrompts.load();
    assertEquals("ai-prova-v1", AiProvaPrompts.PROMPT_VERSION);
    assertTrue(prompt.contains("ai-prova-v1"));
    assertTrue(prompt.toLowerCase().contains("figura"));
    assertTrue(prompt.toLowerCase().contains("passage") || prompt.toLowerCase().contains("passagem"));
    assertTrue(prompt.toLowerCase().contains("explanation"));
    assertTrue(prompt.contains("JSON"));
  }

  @Test
  void wrongCountRejected() {
    // IFRN-2026 com 39 questões (redução silenciosa proibida).
    String full = validBatch("IFRN-2026");
    int cut = full.lastIndexOf(",{\"n\": 40,");
    String batch = full.substring(0, cut) + "]}";
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("N=40")));
  }

  @Test
  void numberingGapRejected() {
    String batch = validBatch("IFRN-2026").replace("\"n\": 7,", "\"n\": 8,");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("lacuna") || d.contains("duplicado")));
  }

  @Test
  void missingAlternativeRejected() {
    String batch = validBatch("IFRN-2026")
        .replaceFirst("\"D\": \"Opção D da 1\"", "\"D2\": \"Opção D da 1\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("q1") && d.contains("A,B,C,D")));
  }

  @Test
  void duplicateAlternativesRejected() {
    String batch = validBatch("IFRN-2026")
        .replaceFirst("\"B\": \"Opção B da 2\"", "\"B\": \"Opção A da 2\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("q2") && d.contains("duplicadas")));
  }

  @Test
  void emptyAlternativeRejected() {
    String batch = validBatch("IFRN-2026")
        .replaceFirst("\"C\": \"Opção C da 3\"", "\"C\": \"  \"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("q3") && d.contains("vazia")));
  }

  @Test
  void answerOutsideAlternativesRejected() {
    String batch = validBatch("IFRN-2026").replaceFirst("\"answer\": \"A\"", "\"answer\": \"E\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("answer")));
  }

  @Test
  void figureMentionRejected() {
    String batch = validBatch("IFRN-2026")
        .replaceFirst("Enunciado auto-contido número 5", "Observe a imagem abaixo e responda a 5");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("q5") && d.contains("figura")));
  }

  @Test
  void passageMentionRejected() {
    String batch = validBatch("IFRN-2026")
        .replaceFirst("Enunciado auto-contido número 6", "Segundo o Texto 1, responda a 6");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("q6") && d.contains("passagem")));
  }

  @Test
  void explanationPresentRejected() {
    String batch = validBatch("IFRN-2026")
        .replaceFirst("\"answer\": \"A\"", "\"answer\": \"A\", \"explanation\": \"Porque sim\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("proibido") && d.contains("explanation")));
  }

  @Test
  void markdownFencesRejected() {
    AiGenerationInvalidException ex = rejects("```json\n" + validBatch("IFRN-2026") + "\n```");
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("markdown")));
  }

  @Test
  void malformedJsonRejected() {
    AiGenerationInvalidException ex = rejects("{\"format\": \"IFRN\", ");
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("json invalido")));
  }

  @Test
  void inventedTopicRejected() {
    String batch = validBatch("IFRN-2026").replaceFirst(
        "\"topic\": \"INTERPRETACAO_TEXTUAL\"", "\"topic\": \"FOFOCA_LITERARIA\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("topic")));
  }

  @Test
  void disciplineOutsideModelRejected() {
    // Ciências da Natureza não existe no modelo IFRN.
    String batch = validBatch("IFRN-2026").replaceFirst(
        "\"discipline\": \"MATEMATICA\"", "\"discipline\": \"CIENCIAS_NATUREZA\"");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream()
        .anyMatch(d -> d.contains("discipline") || d.contains("CIENCIAS_NATUREZA")));
  }

  @Test
  void unknownEditionModelRejected() {
    String batch = validBatch("IFRN-2026").replace("IFRN-2026", "IFRN-2030");
    AiGenerationInvalidException ex = rejects(batch);
    assertTrue(ex.getDetails().stream().anyMatch(d -> d.contains("editionModel")));
  }

  @Test
  void nullBatchRejected() {
    rejects(null);
    rejects("   ");
  }
}
