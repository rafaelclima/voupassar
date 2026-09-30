package br.com.voupassar.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Papel RBAC (TASK 3.5). Linhas-semente: {@code STUDENT, CURATOR, ADMIN} (V2).
 */
@Entity
@Table(name = "roles")
public class Role {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "description", nullable = false)
  private String description;

  public Role() {}

  public Long getId() {
    return id;
  }

  public String getCode() {
    return code;
  }
}
