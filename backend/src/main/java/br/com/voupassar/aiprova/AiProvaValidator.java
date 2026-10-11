package br.com.voupassar.aiprova;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Validador estrutural do caderno-IA (TASK A.1, testável sem rede).
 *
 * <p>Regras (contrato em {@code docs/ai-prova-schema.md}): JSON parseável e
 * estrito (sem fences de markdown), N exato do formato-modelo, posições
 * 1..N sem lacuna, 4 alternativas não-vazias e distintas, {@code answer} em
 * A–D, disciplinas só as do modelo com contagem exata, {@code topic} no
 * vocabulário vigente ou {@code DESCONHECIDO}, sem marcadores de
 * figura/passagem, sem {@code explanation}.
 *
 * <p>Reprovação → {@link AiGenerationInvalidException} (mapeada para
 * {@code 422 AI_GENERATION_INVALID}) com motivo por posição. Nunca corrige
 * silenciosamente, nunca devolve caderno parcial.
 */
public final class AiProvaValidator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Set<String> ANSWER_KEYS = Set.of("A", "B", "C", "D");

  /** Chaves proibidas em qualquer objeto de questão (v1: sem explanation/passagem/figura; sem subtopic — fora do contrato TASKS.md A.1). */
  private static final Set<String> FORBIDDEN_KEYS = Set.of(
      "explanation", "passage", "figure", "figureUrl", "image", "texto_base", "textBase",
      "textbase", "subtopic", "markdown");

  /**
   * Menções a figura/imagem/gráfico/tabela/observe/charge-visual (case-insensitive).
   *
   * <p>Decisão DEV (reconciliação A.1): incorpora {@code quadro}, {@code tirinha} e
   * {@code foto} do draft VPS, mas NÃO {@code mapa} (questões auto-contidas de ESCALA
   * usam "mapa" legitimamente — ex. taxonomia v1.1 {@code RAZAO_PROPORCAO/ESCALA}) nem
   * {@code textos} sem número (gênero/tipo textual citam "textos" legitimamente).
   * Referências a mapa/texto externo via "observe"/"conforme"/"Texto N" continuam
   * capturadas pelos padrões abaixo.
   */
  private static final Pattern FIGURE_REF = Pattern.compile(
      "figura|imagem|gr[áa]fico|tabela|quadro|tirinha|foto|ilustra[çc][ãa]o|"
          + "\\bobserve\\b|\\bconforme\\b|\\bcharge\\b",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  /** Menções a passagem/texto-base compartilhado (case-insensitive). */
  private static final Pattern PASSAGE_REF = Pattern.compile(
      "texto\\s*[0-9ivx]+|\\btrecho\\b|texto-base|quest[õo]es?\\s+\\d+\\s*(a|e|-)",
      Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

  /** Resultado de um lote válido (caderno congelável). */
  public record ValidatedBatch(String format, String editionModel, int size) {}

  private AiProvaValidator() {}

  /**
   * Valida o JSON bruto do provedor.
   *
   * @param rawJson corpo devolvido pelo provedor (mock ou OpenRouter)
   * @return lote validado (formato + modelo + tamanho)
   * @throws AiGenerationInvalidException se qualquer regra falhar
   */
  public static ValidatedBatch validate(String rawJson) {
    if (rawJson == null || rawJson.isBlank()) {
      throw invalid("Lote de IA vazio.", List.of("envelope: corpo vazio"));
    }
    if (rawJson.contains("```")) {
      throw invalid("Lote de IA fora do contrato (markdown).",
          List.of("envelope: json estrito esperado, sem fences de markdown"));
    }
    JsonNode root;
    try {
      root = MAPPER.readTree(rawJson);
    } catch (Exception e) {
      throw invalid("Lote de IA fora do contrato (json invalido).",
          List.of("envelope: json invalido"));
    }
    if (!root.isObject()) {
      throw invalid("Lote de IA fora do contrato.", List.of("envelope: objeto esperado"));
    }

    List<String> problems = new ArrayList<>();

    String format = textField(root, "format");
    String editionModel = textField(root, "editionModel");
    JsonNode questions = root.get("questions");

    if (format == null || (!format.equals("IFRN") && !format.equals("EAJ"))) {
      problems.add("envelope: format deve ser IFRN ou EAJ");
    }
    AiProvaSpec.EditionSpec spec = editionModel == null
        ? null : AiProvaSpec.MODELS.get(editionModel);
    if (spec == null) {
      problems.add("envelope: editionModel desconhecido ou ausente"
          + " (esperado um de IFRN-2020, IFRN-2022, IFRN-2023, IFRN-2024,"
          + " IFRN-2025, IFRN-2026, EAJ-2021, EAJ-2022, EAJ-2025;"
          + " IFRN-2021 e EAJ-2023/2024/2026 não existem — nunca inventar)");
    }
    if (questions == null || !questions.isArray()) {
      problems.add("envelope: questions deve ser um array");
    }
    if (!problems.isEmpty()) {
      throw invalid("Lote de IA fora do contrato.", problems);
    }
    if (!editionModel.startsWith(format)) {
      problems.add("envelope: editionModel " + editionModel + " incompatível com format " + format);
    }

    List<JsonNode> items = new ArrayList<>();
    questions.forEach(items::add);
    if (items.size() != spec.total()) {
      problems.add("envelope: esperado N=" + spec.total() + " para " + editionModel
          + ", recebido " + items.size());
    }

    // Posições 1..N sem lacuna/duplicata.
    Set<Integer> seen = new HashSet<>();
    Map<String, Integer> counts = new HashMap<>();
    for (JsonNode q : items) {
      String tag = "q" + (q.has("n") && q.get("n").isInt() ? q.get("n").intValue() : "?");
      if (!q.isObject()) {
        problems.add(tag + ": objeto esperado");
        continue;
      }
      int n = q.has("n") && q.get("n").isInt() ? q.get("n").intValue() : -1;
      if (n < 1 || (spec != null && n > spec.total())) {
        problems.add(tag + ": n fora de 1.." + (spec == null ? "N" : spec.total()));
      } else if (!seen.add(n)) {
        problems.add("q" + n + ": n duplicado");
      }

      String discipline = textField(q, "discipline");
      if (discipline == null || !spec.byDiscipline().containsKey(discipline)) {
        problems.add("q" + n + ": discipline inválida ou fora do modelo " + editionModel
            + " (permitidas " + spec.byDiscipline().keySet() + ")");
      } else {
        counts.merge(discipline, 1, Integer::sum);
      }

      String topic = textField(q, "topic");
      Set<String> vocab = discipline == null
          ? null : AiProvaSpec.TOPICS.get(discipline);
      if (topic == null || vocab == null || !vocab.contains(topic)) {
        problems.add("q" + n + ": topic inválido (vocabulário da disciplina ou DESCONHECIDO)");
      }

      String stem = textField(q, "stem");
      if (stem == null || stem.strip().isEmpty()) {
        problems.add("q" + n + ": stem vazio");
      } else {
        checkRefs(problems, n, "stem", stem);
      }

      JsonNode options = q.get("options");
      if (options == null || !options.isObject()) {
        problems.add("q" + n + ": options deve ser objeto com A,B,C,D");
      } else {
        Set<String> keys = new HashSet<>();
        options.properties().forEach(e -> keys.add(e.getKey()));
        if (!keys.equals(ANSWER_KEYS)) {
          problems.add("q" + n + ": options deve conter exatamente A,B,C,D");
        } else {
          Set<String> normalized = new HashSet<>();
          for (String k : ANSWER_KEYS) {
            String v = options.get(k).isString() ? options.get(k).stringValue() : null;
            if (v == null || v.strip().isEmpty()) {
              problems.add("q" + n + ": alternativa " + k + " vazia");
            } else {
              checkRefs(problems, n, "alternativa " + k, v);
              String norm = v.strip().replaceAll("\\s+", " ");
              if (!normalized.add(norm)) {
                problems.add("q" + n + ": alternativas duplicadas");
                break;
              }
            }
          }
        }
      }

      String answer = textField(q, "answer");
      if (answer == null || !ANSWER_KEYS.contains(answer)) {
        problems.add("q" + n + ": answer deve ser A, B, C ou D");
      }

      for (String forbidden : FORBIDDEN_KEYS) {
        if (q.has(forbidden)) {
          problems.add("q" + n + ": campo proibido presente (" + forbidden + ")");
        }
      }
      // Envelope também não pode carregar explanation/passage/figure.
      for (String forbidden : FORBIDDEN_KEYS) {
        if (root.has(forbidden)) {
          problems.add("envelope: campo proibido presente (" + forbidden + ")");
          break;
        }
      }
    }

    // Lacunas de numeração (1..N).
    if (spec != null && problems.stream().noneMatch(p -> p.contains("duplicado"))) {
      for (int i = 1; i <= spec.total(); i++) {
        if (!seen.contains(i)) {
          problems.add("q" + i + ": posição ausente (lacuna de numeração)");
        }
      }
    }

    // Contagem exata por disciplina.
    for (Map.Entry<String, Integer> e : spec.byDiscipline().entrySet()) {
      int got = counts.getOrDefault(e.getKey(), 0);
      if (got != e.getValue()) {
        problems.add("envelope: " + e.getKey() + " esperado " + e.getValue()
            + ", recebido " + got);
      }
    }

    if (!problems.isEmpty()) {
      throw invalid("Lote de IA fora do contrato (" + problems.size() + " problema(s)).", problems);
    }
    return new ValidatedBatch(format, editionModel, items.size());
  }

  private static void checkRefs(List<String> problems, int n, String where, String text) {
    if (FIGURE_REF.matcher(text).find()) {
      problems.add("q" + n + ": menção a figura/imagem/gráfico/tabela em " + where
          + " (questão deve ser auto-contida)");
    } else if (PASSAGE_REF.matcher(text).find()) {
      problems.add("q" + n + ": menção a passagem/texto-base em " + where
          + " (questão deve ser auto-contida)");
    }
  }

  private static String textField(JsonNode node, String field) {
    JsonNode v = node.get(field);
    return v != null && v.isString() ? v.stringValue() : null;
  }

  private static AiGenerationInvalidException invalid(String message, List<String> details) {
    return new AiGenerationInvalidException(message, details);
  }
}
