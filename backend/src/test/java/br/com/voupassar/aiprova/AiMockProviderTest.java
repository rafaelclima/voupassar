package br.com.voupassar.aiprova;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Provedor mockado determinístico (TASK A.3) — sem rede.
 *
 * <p>Prova válida no schema para as 3 formas (IFRN-40, EAJ-40, EAJ-2021-50 —
 * os 9 {@code editionModel} vigentes); mesmo modelo → mesmos bytes;
 * modelo desconhecido falha rápido (nunca lote inventado).
 */
class AiMockProviderTest {

  private final AiMockProvider provider = new AiMockProvider();

  @Test
  void allNineModelsValidate() {
    for (String model : AiProvaSpec.models().keySet()) {
      AiProvaValidator.ValidatedBatch batch =
          AiProvaValidator.validate(provider.generate(model));
      assertEquals(model, batch.editionModel());
    }
    assertEquals(40, AiProvaValidator.validate(provider.generate("IFRN-2026")).size());
    assertEquals(40, AiProvaValidator.validate(provider.generate("EAJ-2022")).size());
    assertEquals(40, AiProvaValidator.validate(provider.generate("EAJ-2025")).size());
    assertEquals(50, AiProvaValidator.validate(provider.generate("EAJ-2021")).size());
  }

  @Test
  void deterministicBytes() {
    assertEquals(provider.generate("IFRN-2026"), provider.generate("IFRN-2026"));
    assertEquals(provider.generate("EAJ-2021"), provider.generate("EAJ-2021"));
  }

  @Test
  void answersRotateWithoutDegeneracy() {
    // A auditoria EAJ flagou 40/40 A como degenerado: o mock gira A–D.
    String batch = provider.generate("IFRN-2026");
    Set<String> seen = new HashSet<>();
    for (String key : new String[] {"\"answer\": \"A\"", "\"answer\": \"B\"",
        "\"answer\": \"C\"", "\"answer\": \"D\""}) {
      if (batch.contains(key)) {
        seen.add(key);
      }
    }
    assertEquals(4, seen.size(), "mock deve girar as 4 respostas");
  }

  @Test
  void unknownModelFailsFast() {
    assertThrows(IllegalArgumentException.class, () -> provider.generate("IFRN-2021"));
  }

  @Test
  void modelIdDeclaresMock() {
    assertTrue(provider.modelId().contains("mock"),
        "id do modelo mockado deve dizer que é mock (nunca fallback silencioso)");
  }
}
