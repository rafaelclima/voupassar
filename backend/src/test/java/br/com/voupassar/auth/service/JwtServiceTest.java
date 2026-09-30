package br.com.voupassar.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.voupassar.auth.config.AuthProperties;
import io.jsonwebtoken.JwtException;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * JWT de acesso (TASK 3.5): emissão HS256, validação e rejeições — sem Spring.
 */
class JwtServiceTest {

  private static AuthProperties props(String secret, String issuer, long ttlMinutes) {
    AuthProperties p = new AuthProperties();
    p.setJwtSecret(secret);
    p.setJwtIssuer(issuer);
    p.setAccessTtlMinutes(ttlMinutes);
    return p;
  }

  private static final String SECRET = "test-secret-min-32-chars-0123456789abcdef";

  @Test
  void roundtripKeepsClaims() {
    JwtService jwt = new JwtService(props(SECRET, "voupassar", 15));
    String token = jwt.generateAccessToken(42L, "a@b.c", List.of("STUDENT"), 3);

    JwtService.ParsedAccess parsed = jwt.parseAccessToken(token);

    assertEquals(42L, parsed.userId());
    assertEquals("a@b.c", parsed.email());
    assertEquals(List.of("STUDENT"), parsed.roles());
    assertEquals(3, parsed.credentialVersion());
    assertTrue(token.split("\\.").length == 3, "JWT compacto tem 3 partes");
  }

  @Test
  void expiredIsRejected() {
    JwtService jwt = new JwtService(props(SECRET, "voupassar", -5));
    String token = jwt.generateAccessToken(1L, "a@b.c", List.of("STUDENT"), 1);

    assertThrows(JwtException.class, () -> jwt.parseAccessToken(token));
  }

  @Test
  void wrongSecretIsRejected() {
    JwtService issuer = new JwtService(props(SECRET, "voupassar", 15));
    JwtService other =
        new JwtService(props("other-secret-min-32-chars-0123456789abcdef", "voupassar", 15));
    String token = issuer.generateAccessToken(1L, "a@b.c", List.of("STUDENT"), 1);

    assertThrows(JwtException.class, () -> other.parseAccessToken(token));
  }

  @Test
  void wrongIssuerIsRejected() {
    JwtService issuer = new JwtService(props(SECRET, "voupassar", 15));
    JwtService other = new JwtService(props(SECRET, "outro", 15));
    String token = issuer.generateAccessToken(1L, "a@b.c", List.of("STUDENT"), 1);

    assertThrows(JwtException.class, () -> other.parseAccessToken(token));
  }

  @Test
  void tamperedIsRejected() {
    JwtService jwt = new JwtService(props(SECRET, "voupassar", 15));
    String token = jwt.generateAccessToken(1L, "a@b.c", List.of("STUDENT"), 1);
    String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "B" : "A");

    assertThrows(JwtException.class, () -> jwt.parseAccessToken(tampered));
  }

  @Test
  void shortSecretFailsFast() {
    AuthProperties p = props("curto", "voupassar", 15);
    assertThrows(IllegalStateException.class, () -> new JwtService(p));
  }
}
