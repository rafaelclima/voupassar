package br.com.voupassar.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import br.com.voupassar.common.jpa.CitextJdbcType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.hibernate.annotations.JdbcType;

/**
 * Conta de acesso (TASK 3.5).
 *
 * <p>Leitura/escrita da tabela {@code users} (DDL TASK 2.2, V1). PII mínima:
 * e-mail (CITEXT UNIQUE) + hash de senha (bcrypt, nunca em log). {@code
 * credentialVersion} invalida access tokens e refreshes antigos quando
 * incrementada (troca/reset de senha). {@code isActive=false} revoga tudo.
 */
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** E-mail CITEXT (case-insensitive no Postgres) — ver {@link CitextJdbcType}. */
  @Column(name = "email", nullable = false, unique = true)
  @JdbcType(CitextJdbcType.class)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "is_active", nullable = false)
  private Boolean active = true;

  @Column(name = "credential_version", nullable = false)
  private Integer credentialVersion = 1;

  @Column(name = "last_login_at")
  private OffsetDateTime lastLoginAt;

  public User() {}

  public User(String email, String passwordHash) {
    this.email = email;
    this.passwordHash = passwordHash;
  }

  public Long getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public boolean isActive() {
    return Boolean.TRUE.equals(active);
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public int getCredentialVersion() {
    return credentialVersion != null ? credentialVersion : 1;
  }

  public void bumpCredentialVersion() {
    this.credentialVersion = getCredentialVersion() + 1;
  }

  public OffsetDateTime getLastLoginAt() {
    return lastLoginAt;
  }

  public void setLastLoginAt(OffsetDateTime lastLoginAt) {
    this.lastLoginAt = lastLoginAt;
  }
}
