package br.com.voupassar.aiprova;

/**
 * Fonte dos bytes do caderno-IA (TASK A.3/C.1).
 *
 * <p>Contrato mínimo: dado um {@code editionModel} vigente, devolve o JSON
 * bruto do lote no schema {@code ai-prova-v1} ({@code
 * docs/ai-prova-schema.md}) + o identificador do modelo efetivo para a
 * auditoria da geração. A validação estrutural acontece DEPOIS, no {@code
 * AiProvaValidator} — provedor nunca é confiável por si só. A troca
 * mock → real (C.1) não muda contrato, isolamento nem validação: só a
 * implementação desta interface.
 */
public interface AiProvaProvider {

  /** Modelo efetivo servido (ex.: {@code mock-deterministico-v1}); registrado na auditoria, sem PII. */
  String modelId();

  /**
   * Gera o JSON bruto do lote para o modelo de edição.
   *
   * @param editionModel um dos 9 modelos de {@link AiProvaSpec#MODELS}
   * @return corpo JSON no envelope {@code {format, editionModel, questions}}
   */
  String generate(String editionModel);
}
