package br.com.voupassar.aiprova;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Modelos de edição v1 da Prova-IA (TASK A.1, reconciliado com o draft VPS).
 *
 * <p>Estrutura dirigida pelos dados da edição-modelo — nunca quantidades
 * universais (TASKS.md decisão 1). Só edições presentes no banco podem ser
 * modelo: IFRN 2020/2022–2026 (40Q 20LP+20MAT; IFRN-2021 AUSENTE — nunca
 * inventar); EAJ-2021 (50Q 15/15/12/8); EAJ-2022/2025 (40Q 20/20).
 * EAJ-2023/2024/2026 AUSENTES.
 */
public final class AiProvaSpec {

  private AiProvaSpec() {}

  /** Especificação de um modelo: N exato + contagem por disciplina. */
  public record EditionSpec(String format, int total, Map<String, Integer> byDiscipline) {}

  private static EditionSpec ifrn40() {
    return new EditionSpec("IFRN", 40,
        Map.of("LINGUA_PORTUGUESA", 20, "MATEMATICA", 20));
  }

  private static EditionSpec eaj40() {
    return new EditionSpec("EAJ", 40,
        Map.of("LINGUA_PORTUGUESA", 20, "MATEMATICA", 20));
  }

  /** Modelos suportados na v1 (chave = {@code editionModel} do contrato). */
  public static final Map<String, EditionSpec> MODELS = Map.ofEntries(
      Map.entry("IFRN-2020", ifrn40()),
      Map.entry("IFRN-2022", ifrn40()),
      Map.entry("IFRN-2023", ifrn40()),
      Map.entry("IFRN-2024", ifrn40()),
      Map.entry("IFRN-2025", ifrn40()),
      Map.entry("IFRN-2026", ifrn40()),
      Map.entry("EAJ-2021", new EditionSpec("EAJ", 50,
          Map.of("LINGUA_PORTUGUESA", 15, "MATEMATICA", 15,
              "CIENCIAS_NATUREZA", 12, "CIENCIAS_HUMANAS", 8))),
      Map.entry("EAJ-2022", eaj40()),
      Map.entry("EAJ-2025", eaj40()));

  /** Vocabulário de {@code topic} por disciplina + {@code DESCONHECIDO}. */
  public static final Map<String, Set<String>> TOPICS = Map.of(
      "LINGUA_PORTUGUESA", Set.of(
          "GRAMATICA_NORMA", "INTERPRETACAO_TEXTUAL", "DESCONHECIDO"),
      "MATEMATICA", Set.of(
          "RAZAO_PROPORCAO", "ARITMETICA", "ALGEBRA", "GEOMETRIA",
          "ESTATISTICA_DADOS", "PORCENTAGEM", "MATEMATICA_FINANCEIRA",
          "GRANDEZAS_MEDIDAS", "DESCONHECIDO"),
      "CIENCIAS_NATUREZA", Set.of(
          "ECOLOGIA", "NUTRICAO_SAUDE", "AGROPECUARIA", "BIOLOGIA_CELULAR",
          "QUIMICA_GERAL", "FISICA_GERAL", "DESCONHECIDO"),
      "CIENCIAS_HUMANAS", Set.of(
          "HISTORIA_BRASIL", "CULTURA_SOCIEDADE", "CARTOGRAFIA",
          "GEOGRAFIA_BRASIL", "GEOPOLITICA", "DESCONHECIDO"));

  /** Cópia ordenada dos modelos (para documentação e mocks da Fase A). */
  public static Map<String, EditionSpec> models() {
    Map<String, EditionSpec> out = new LinkedHashMap<>();
    for (String k : new String[] {"IFRN-2020", "IFRN-2022", "IFRN-2023", "IFRN-2024",
        "IFRN-2025", "IFRN-2026", "EAJ-2021", "EAJ-2022", "EAJ-2025"}) {
      out.put(k, MODELS.get(k));
    }
    return out;
  }
}
