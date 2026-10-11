package br.com.voupassar.aiprova;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Bateria adversária executável da Prova-IA (TASK B.2, DEV).
 *
 * <p>Execução dos fixtures do draft VPS (`docs/ai-fixtures-b2.md`,
 * `draft/b2-vps-fixtures`): cada fixture = lote base válido + 1 mutação.
 * Todo caso é rejeitado com {@code AI_GENERATION_INVALID} (mapeado para
 * `422` com motivo por posição — mapeamento HTTP já provado em
 * `GlobalExceptionHandlerTest.aiGenerationInvalidMapsTo422WithDetails`);
 * nenhum caso gera caderno parcial (o validador só devolve lote válido ou
 * exceção — nunca meio-caderno). Lote válido passa (A.1).
 *
 * <p>Correção DEV contra o draft (dona do validador): B2-09b ("conforme o
 * trecho") cai na mensagem de **figura**, não de passagem — `conforme` é
 * marcador de figura (`docs/ai-prova-schema.md §6`) e a checagem de figura
 * precede a de passagem. A rejeição com posição é honesta nos dois casos.
 */
class AiProvaAdversarialTest {

  private static String question(int n, String discipline, String topic) {
    return "{\"n\": " + n + ", \"discipline\": \"" + discipline + "\", \"topic\": \""
        + topic + "\", \"stem\": \"Enunciado auto-contido número " + n + " sobre "
        + topic.toLowerCase().replace('_', ' ') + "?\", "
        + "\"options\": {\"A\": \"Opção A da " + n + "\", \"B\": \"Opção B da " + n
        + "\", \"C\": \"Opção C da " + n + "\", \"D\": \"Opção D da " + n + "\"}, "
        + "\"answer\": \"A\"}";
  }

  /** Base válida IFRN-2026 (40Q 20LP+20MAT), mesma do draft (via A.1). */
  private static String validBase() {
    List<String> items = new ArrayList<>();
    for (int n = 1; n <= 20; n++) {
      items.add(question(n, "LINGUA_PORTUGUESA", "INTERPRETACAO_TEXTUAL"));
    }
    for (int n = 21; n <= 40; n++) {
      items.add(question(n, "MATEMATICA", "ALGEBRA"));
    }
    return "{\"format\": \"IFRN\", \"editionModel\": \"IFRN-2026\", \"questions\": ["
        + String.join(",", items) + "]}";
  }

  private static AiGenerationInvalidException rejects(String rawJson) {
    AiGenerationInvalidException ex =
        assertThrows(AiGenerationInvalidException.class, () -> AiProvaValidator.validate(rawJson));
    assertEquals("AI_GENERATION_INVALID", ex.getCode());
    assertTrue(ex.getDetails() != null && !ex.getDetails().isEmpty(),
        "rejeição sem motivo por posição");
    return ex;
  }

  private static void assertReason(AiGenerationInvalidException ex, String... parts) {
    assertTrue(ex.getDetails().stream().anyMatch(
        d -> { for (String p : parts) { if (!d.contains(p)) { return false; } } return true; }),
        "motivo esperado " + String.join("+", parts) + " ausente em " + ex.getDetails());
  }

  @Test
  void b201_shortCount() {
    String full = validBase();
    int cut = full.lastIndexOf(",{\"n\": 40,");
    AiGenerationInvalidException ex = rejects(full.substring(0, cut) + "]}");
    assertReason(ex, "N=40");
  }

  @Test
  void b202_overCount() {
    String full = validBase();
    int cut = full.lastIndexOf("]}"); // índice do fecha-array (q40 já fechada)
    String q41 = ","
        + "{\"n\": 41, \"discipline\": \"MATEMATICA\", \"topic\": \"ALGEBRA\", "
        + "\"stem\": \"Enunciado auto-contido número 41 sobre algebra?\", "
        + "\"options\": {\"A\": \"Opção A da 41\", \"B\": \"Opção B da 41\", "
        + "\"C\": \"Opção C da 41\", \"D\": \"Opção D da 41\"}, \"answer\": \"A\"}";
    AiGenerationInvalidException ex = rejects(full.substring(0, cut) + q41 + "]}");
    assertReason(ex, "N=40");
    assertReason(ex, "q41", "fora de 1..40");
  }

  @Test
  void b203_numberingGap() {
    AiGenerationInvalidException ex = rejects(validBase().replace("\"n\": 7,", "\"n\": 8,"));
    assertReason(ex, "duplicado");
  }

  @Test
  void b204_alternativeKeyOutside() {
    AiGenerationInvalidException ex = rejects(
        validBase().replaceFirst("\"D\": \"Opção D da 1\"", "\"D2\": \"Opção D da 1\""));
    assertReason(ex, "q1", "A,B,C,D");
  }

  @Test
  void b205_emptyAlternative() {
    AiGenerationInvalidException ex = rejects(
        validBase().replaceFirst("\"C\": \"Opção C da 3\"", "\"C\": \"  \""));
    assertReason(ex, "q3", "vazia");
  }

  @Test
  void b206_duplicateAlternatives() {
    AiGenerationInvalidException ex = rejects(
        validBase().replaceFirst("\"B\": \"Opção B da 2\"", "\"B\": \"Opção A da 2\""));
    assertReason(ex, "q2", "duplicadas");
  }

  @Test
  void b207_answerOutside() {
    AiGenerationInvalidException ex = rejects(
        validBase().replaceFirst("\"answer\": \"A\"", "\"answer\": \"E\""));
    assertReason(ex, "answer");
  }

  @Test
  void b208a_observeImageInStem() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("Enunciado auto-contido número 5", "Observe a imagem abaixo e responda a 5"));
    assertReason(ex, "q5", "figura");
  }

  @Test
  void b208b_conformeGraphInStem() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("Enunciado auto-contido número 6", "Conforme o gráfico abaixo, responda a 6"));
    assertReason(ex, "q6", "figura");
  }

  @Test
  void b208c_tableInAlternative() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("\"A\": \"Opção A da 7\"", "\"A\": \"os dados da tabela acima\""));
    assertReason(ex, "q7", "figura");
  }

  @Test
  void b209a_texto1InStem() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("Enunciado auto-contido número 6", "Segundo o Texto 1, responda a 6"));
    assertReason(ex, "q6", "passagem");
  }

  @Test
  void b209b_conformeTrechoFallsInFigureMessage() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("número 7", "7 conforme o trecho apresentado"));
    assertReason(ex, "q7", "figura");
  }

  @Test
  void b210_explanationPresent() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("\"answer\": \"A\"", "\"answer\": \"A\", \"explanation\": \"Porque sim\""));
    assertReason(ex, "proibido", "explanation");
  }

  @Test
  void b211_markdownFences() {
    AiGenerationInvalidException ex = rejects("```json\n" + validBase() + "\n```");
    assertReason(ex, "markdown");
  }

  @Test
  void b212_malformedJson() {
    AiGenerationInvalidException ex = rejects("{\"format\": \"IFRN\", ");
    assertReason(ex, "json invalido");
  }

  @Test
  void b213_subtopicOutsideContract() {
    AiGenerationInvalidException ex = rejects(validBase()
        .replaceFirst("\"answer\": \"A\"", "\"answer\": \"A\", \"subtopic\": \"INFERENCIA\""));
    assertReason(ex, "proibido", "subtopic");
  }
}
