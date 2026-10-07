package br.com.voupassar.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Parâmetros de autenticação (TASK 3.5) — todos via ambiente em produção,
 * nunca hardcoded (AGENTS.md §29).
 *
 * <p>Prefixo {@code app.security}: segredo JWT, TTLs, força do bcrypt e
 * rate limiting do {@code /auth}. Placeholders em {@code .env.example}.
 */
@ConfigurationProperties(prefix = "app.security")
public class AuthProperties {

  /** Segredo HMAC do JWT (env {@code JWT_SECRET}). Mínimo 32 bytes em UTF-8. */
  private String jwtSecret = "dev-only-secret-CHANGE-ME-min-32-chars-0123456789ab";

  private String jwtIssuer = "voupassar";

  /** Access curto por padrão (minutos). */
  private long accessTtlMinutes = 15;

  /** Refresh rotativo (dias). */
  private long refreshTtlDays = 7;

  /** Reset de senha, uso único (minutos). */
  private long resetTtlMinutes = 60;

  /** Custo do bcrypt (10 = padrão; VPS fraca — não subir sem calibrar). */
  private int bcryptStrength = 10;

  /**
   * Atrás de proxy reverso que injeta {@code X-Forwarded-For} real
   * (env {@code BEHIND_PROXY}; VPS prod = true). Quando false, o header é
   * ignorado no rate limiting e na auditoria (TASK 22.1).
   */
  private boolean behindProxy = false;

  private final RateLimit rateLimit = new RateLimit();

  public String getJwtSecret() {
    return jwtSecret;
  }

  public void setJwtSecret(String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public String getJwtIssuer() {
    return jwtIssuer;
  }

  public void setJwtIssuer(String jwtIssuer) {
    this.jwtIssuer = jwtIssuer;
  }

  public long getAccessTtlMinutes() {
    return accessTtlMinutes;
  }

  public void setAccessTtlMinutes(long accessTtlMinutes) {
    this.accessTtlMinutes = accessTtlMinutes;
  }

  public long getRefreshTtlDays() {
    return refreshTtlDays;
  }

  public void setRefreshTtlDays(long refreshTtlDays) {
    this.refreshTtlDays = refreshTtlDays;
  }

  public long getResetTtlMinutes() {
    return resetTtlMinutes;
  }

  public void setResetTtlMinutes(long resetTtlMinutes) {
    this.resetTtlMinutes = resetTtlMinutes;
  }

  public int getBcryptStrength() {
    return bcryptStrength;
  }

  public void setBcryptStrength(int bcryptStrength) {
    this.bcryptStrength = bcryptStrength;
  }

  public RateLimit getRateLimit() {
    return rateLimit;
  }

  public boolean isBehindProxy() {
    return behindProxy;
  }

  public void setBehindProxy(boolean behindProxy) {
    this.behindProxy = behindProxy;
  }

  /** Janela fixa em memória para {@code /api/v1/auth/**} (proteção básica; proxy faz o resto). */
  public static class RateLimit {
    private boolean enabled = true;
    private int maxRequests = 60;
    private long windowSeconds = 60;
    /**
     * Teto das rotas de escrita (POST/PUT/PATCH/DELETE em
     * {@code /attempts}, {@code /simulations}, {@code /recommendations} —
     * TASK 22.1). Maior que o do auth: escrita legítima em rajada curta
     * (simulado, roteiro) não pode travar uso normal.
     */
    private int writeMaxRequests = 120;

    public boolean isEnabled() {
      return enabled;
    }

    public void setEnabled(boolean enabled) {
      this.enabled = enabled;
    }

    public int getMaxRequests() {
      return maxRequests;
    }

    public void setMaxRequests(int maxRequests) {
      this.maxRequests = maxRequests;
    }

    public long getWindowSeconds() {
      return windowSeconds;
    }

    public void setWindowSeconds(long windowSeconds) {
      this.windowSeconds = windowSeconds;
    }

    public int getWriteMaxRequests() {
      return writeMaxRequests;
    }

    public void setWriteMaxRequests(int writeMaxRequests) {
      this.writeMaxRequests = writeMaxRequests;
    }
  }
}
