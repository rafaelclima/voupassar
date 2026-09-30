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
 * Token de recuperação de senha (TASK 3.5, V3).
 *
 * <p>Uso único ({@code usedAt}) com expiração curta. Só o hash SHA-256 é
 * persistido. O uso válido incrementa {@code credential_version} do usuário
 * (invalida access + refreshes) e marca o token como usado na mesma
 * transação.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

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

  @Column(name = "used_at")
  private OffsetDateTime usedAt;

  @Column(name = "created_ip")
  private String createdIp;

  public PasswordResetToken() {}

  public PasswordResetToken(User user, String tokenHash, OffsetDateTime expiresAt) {
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

  public OffsetDateTime getUsedAt() {
    return usedAt;
  }

  public void setUsedAt(OffsetDateTime usedAt) {
    this.usedAt = usedAt;
  }

  public boolean isUsed() {
    return usedAt != null;
  }

  public boolean isExpired(OffsetDateTime now) {
    return !expiresAt.isAfter(now);
  }

  public void setCreatedIp(String createdIp) {
    this.createdIp = createdIp;
  }
}
