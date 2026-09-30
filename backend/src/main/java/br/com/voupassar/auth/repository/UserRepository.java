package br.com.voupassar.auth.repository;

import br.com.voupassar.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  /** E-mail é CITEXT no banco: a comparação já é case-insensitive no Postgres. */
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
