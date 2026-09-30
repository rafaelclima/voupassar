package br.com.voupassar.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

/**
 * Refresh token opaco (TASK 3.5).
 *
 * <p>Armazena apenas o hash SHA-256 do token, nunca o valor (ERD §2.1).
 * Rotação: cada uso revoga o pai ({@code revokedAt}) e encadeia o filho
 * ({@code replacedBy}); reuso de um token já rotacionado = roubo suspeito →
 * revoga a cadeia inteira do usuário. {@code credentialVersion} do usuário
 * divergente do vigente também invalida.
 */
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column(name = "expires_at", nullable = false)
  private OffsetDateTime expiresAt;

  @Column(name = "revoked_at")
  private OffsetDateTime revokedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "replaced_by_id")
  private RefreshToken replacedBy;

  @Column(name = "created_ip")
  private String createdIp;

  @Column(name = "user_agent")
  private String userAgent;

  public RefreshToken() {}

  public RefreshToken(User user, String tokenHash, OffsetDateTime expiresAt) {
    this.user = user;
    this.tokenHash = tokenHash;
    this.expiresAt = expiresAt;
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public String getTokenHash() {
    return tokenHash;
  }

  public OffsetDateTime getExpiresAt() {
    return expiresAt;
  }

  public OffsetDateTime getRevokedAt() {
    return revokedAt;
  }

  public void setRevokedAt(OffsetDateTime revokedAt) {
    this.revokedAt = revokedAt;
  }

  public RefreshToken getReplacedBy() {
    return replacedBy;
  }

  public void setReplacedBy(RefreshToken replacedBy) {
    this.replacedBy = replacedBy;
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isExpired(OffsetDateTime now) {
    return !expiresAt.isAfter(now);
  }

  public void setCreatedIp(String createdIp) {
    this.createdIp = createdIp;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }
}
