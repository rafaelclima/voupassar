package br.com.voupassar.auth.service;

import br.com.voupassar.auth.config.AuthProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Access token JWT HS256 (TASK 3.5).
 *
 * <p>Claims: {@code sub=userId, email, roles, cv=credentialVersion, iss, iat,
 * exp}. Fail-fast no boot quando o segredo tem menos de 32 bytes. Tolerância
 * de relógio de 60s. Refresh/reset são opacos (nunca JWT) — ver {@link
 * br.com.voupassar.auth.service.AuthService}.
 */
@Service
public class JwtService {

  private static final long CLOCK_SKEW_SECONDS = 60;

  private final SecretKey key;
  private final String issuer;
  private final long accessTtlMinutes;

  public JwtService(AuthProperties props) {
    byte[] secret = props.getJwtSecret().getBytes(StandardCharsets.UTF_8);
    if (secret.length < 32) {
      throw new IllegalStateException(
          "app.security.jwt-secret precisa de ao menos 32 bytes (env JWT_SECRET).");
    }
    this.key = new SecretKeySpec(secret, "HmacSHA256");
    this.issuer = props.getJwtIssuer();
    this.accessTtlMinutes = props.getAccessTtlMinutes();
  }

  /** Emite um access token curto para o usuário com papéis e versão credencial. */
  public String generateAccessToken(long userId, String email, List<String> roles, int credentialVersion) {
    Instant now = Instant.now();
    return Jwts.builder()
        .issuer(issuer)
        .subject(Long.toString(userId))
        .claim("email", email)
        .claim("roles", roles)
        .claim("cv", credentialVersion)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessTtlMinutes * 60)))
        .signWith(key)
        .compact();
  }

  /** Valida assinatura, emissor e expiração; lança {@link JwtException} se inválido. */
  public ParsedAccess parseAccessToken(String token) {
    Jws<Claims> jws =
        Jwts.parser()
            .verifyWith(key)
            .requireIssuer(issuer)
            .clockSkewSeconds(CLOCK_SKEW_SECONDS)
            .build()
            .parseSignedClaims(token);
    Claims claims = jws.getPayload();
    long userId;
    try {
      userId = Long.parseLong(claims.getSubject());
    } catch (NumberFormatException e) {
      throw new JwtException("sub inválido no access token.");
    }
    @SuppressWarnings("unchecked")
    List<String> roles = claims.get("roles", List.class);
    Number cv = claims.get("cv", Number.class);
    return new ParsedAccess(
        userId,
        claims.get("email", String.class),
        roles != null ? List.copyOf(roles) : List.of(),
        cv != null ? cv.intValue() : 1);
  }

  /** Access token validado (sem acesso ao banco — o filtro confere ativo/versão). */
  public record ParsedAccess(long userId, String email, List<String> roles, int credentialVersion) {}
}
