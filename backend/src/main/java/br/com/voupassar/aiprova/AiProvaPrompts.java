package br.com.voupassar.aiprova;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Prompt versionado {@code ai-prova-v1} (TASK A.1).
 *
 * <p>Arquivo canônico:
 * {@code backend/src/main/resources/ai/prompts/ai-prova-v1.txt}.
 * Mudança de instrução exige nova versão, nunca edição silenciosa.
 */
public final class AiProvaPrompts {

  /** Versão do prompt vigente (registrada na auditoria da geração). */
  public static final String PROMPT_VERSION = "ai-prova-v1";

  private static final String CLASSPATH = "ai/prompts/ai-prova-v1.txt";

  private AiProvaPrompts() {}

  /** Carrega o texto do prompt do classpath (falha rápido se ausente). */
  public static String load() {
    try (InputStream in = AiProvaPrompts.class.getClassLoader().getResourceAsStream(CLASSPATH)) {
      if (in == null) {
        throw new IllegalStateException("Prompt " + PROMPT_VERSION + " ausente no classpath: " + CLASSPATH);
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("Falha ao ler o prompt " + PROMPT_VERSION, e);
    }
  }
}
