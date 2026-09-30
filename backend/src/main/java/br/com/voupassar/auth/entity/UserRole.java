package br.com.voupassar.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Vínculo M:N {@code users ↔ roles} (TASK 3.5).
 *
 * <p>M:N (e não 1:N) para permitir que um curador também estude sem segunda
 * conta (docs/database-erd.md §2.1). Registro usa {@code STUDENT}.
 */
@Entity
@Table(name = "user_roles")
@IdClass(UserRole.UserRoleId.class)
public class UserRole {

  /** Chave composta (user, role) — nomes/valores espelham os atributos {@code @Id}. */
  public static class UserRoleId implements Serializable {
    private Long user;
    private Long role;

    public UserRoleId() {}

    public UserRoleId(Long user, Long role) {
      this.user = user;
      this.role = role;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof UserRoleId other)) {
        return false;
      }
      return Objects.equals(user, other.user) && Objects.equals(role, other.role);
    }

    @Override
    public int hashCode() {
      return Objects.hash(user, role);
    }
  }

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Id
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  @Column(name = "granted_at", nullable = false)
  private OffsetDateTime grantedAt = OffsetDateTime.now();

  public UserRole() {}

  public UserRole(User user, Role role) {
    this.user = user;
    this.role = role;
  }

  public User getUser() {
    return user;
  }

  public Role getRole() {
    return role;
  }
}
