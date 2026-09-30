package br.com.voupassar.auth.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hash SHA-256 (hex) para refresh/reset tokens (TASK 3.5).
 *
 * <p>Banco guarda só o hash; o valor opaco trafega uma única vez até o
 * cliente. Comparação por igualdade de strings de tamanho fixo (sem
 * short-circuit de conteúdo).
 */
final class TokenHash {

  private TokenHash() {}

  static String sha256Hex(String raw) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
      StringBuilder out = new StringBuilder(digest.length * 2);
      for (byte b : digest) {
        out.append(String.format("%02x", b));
      }
      return out.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 indisponível na JVM.", e);
    }
  }
}
