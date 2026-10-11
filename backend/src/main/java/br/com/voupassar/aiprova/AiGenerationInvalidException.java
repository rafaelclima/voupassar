package br.com.voupassar.aiprova;

import java.util.List;

/**
 * Falha estrutural do lote gerado pela LLM (TASK A.1).
 *
 * <p>Mapeada para {@code 422 AI_GENERATION_INVALID} com motivo por posição
 * (nunca correção silenciosa, nunca caderno parcial). O "sem garantia" da
 * decisão 4 vale para a correção pedagógica do gabarito, nunca para o
 * formato.
 */
public class AiGenerationInvalidException extends RuntimeException {

  private final String code = "AI_GENERATION_INVALID";
  private final List<String> details;

  public AiGenerationInvalidException(String message, List<String> details) {
    super(message);
    this.details = details == null ? List.of() : List.copyOf(details);
  }

  public String getCode() {
    return code;
  }

  public List<String> getDetails() {
    return details;
  }
}
