package br.com.voupassar.aiprova;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * Provedor mockado determinístico da Prova-IA (TASK A.3, Fase A: sem chamada
 * real à LLM).
 *
 * <p>Devolve prova válida no schema para os 3 formatos (IFRN-40, EAJ-40,
 * EAJ-2021-50 — os 9 {@code editionModel} vigentes): N exato, posições 1..N
 * na ordem canônica do mapa ({@code docs/ai-prova-schema.md §2}), 4
 * alternativas distintas, resposta girando em A–D (nunca a degeneração
 * 40×A), tópicos girando no vocabulário vigente ou determinísticos,
 * enunciados auto-contidos sem marcadores de figura/passagem. Mesmo {@code
 * editionModel} → mesmos bytes (sem relógio, sem aleatoriedade).
 *
 * <p>Conteúdo sintético, nunca questão oficial. A C.1 adiciona a
 * implementação OpenRouter desta interface + seleção; o serviço não muda.
 */
@Service
public class AiMockProvider implements AiProvaProvider {

  /** Identificador do modelo mockado (auditoria honesta: diz que é mock). */
  public static final String MODEL_ID = "mock-deterministico-v1";

  /** Ordem canônica de posições (schema §2: LP, MAT, CN, CH). */
  private static final List<String> CANONICAL_ORDER =
      List.of("LINGUA_PORTUGUESA", "MATEMATICA", "CIENCIAS_NATUREZA", "CIENCIAS_HUMANAS");

  private static final String ANSWERS = "ABCD";

  @Override
  public String modelId() {
    return MODEL_ID;
  }

  @Override
  public String generate(String editionModel) {
    AiProvaSpec.EditionSpec spec = AiProvaSpec.MODELS.get(editionModel);
    if (spec == null) {
      throw new IllegalArgumentException("editionModel desconhecido no mock: " + editionModel);
    }
    List<String> items = new ArrayList<>(spec.total());
    int n = 0;
    for (String discipline : CANONICAL_ORDER) {
      Integer count = spec.byDiscipline().get(discipline);
      if (count == null) {
        continue;
      }
      List<String> vocab = new ArrayList<>(AiProvaSpec.TOPICS.get(discipline));
      vocab.remove("DESCONHECIDO");
      vocab.sort(String::compareTo);
      for (int i = 0; i < count; i++) {
        n++;
        String topic = vocab.get((n - 1) % vocab.size());
        items.add(question(n, discipline, topic));
      }
    }
    return "{\"format\": \"" + spec.format() + "\", \"editionModel\": \"" + editionModel
        + "\", \"questions\": [" + String.join(",", items) + "]}";
  }

  private static String question(int n, String discipline, String topic) {
    String about = topic.toLowerCase().replace('_', ' ');
    String answer = String.valueOf(ANSWERS.charAt((n - 1) % ANSWERS.length()));
    return "{\"n\": " + n + ", \"discipline\": \"" + discipline + "\", \"topic\": \""
        + topic + "\", \"stem\": \"Enunciado sintético " + n + " (prova-IA experimental, "
        + about + "). Assinale a alternativa correta.\", "
        + "\"options\": {\"A\": \"Alternativa A da questão " + n + ".\", "
        + "\"B\": \"Alternativa B da questão " + n + ".\", "
        + "\"C\": \"Alternativa C da questão " + n + ".\", "
        + "\"D\": \"Alternativa D da questão " + n + ".\"}, "
        + "\"answer\": \"" + answer + "\"}";
  }
}
