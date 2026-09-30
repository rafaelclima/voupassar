package br.com.voupassar.auth.repository;

import br.com.voupassar.auth.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

  Optional<Role> findByCode(String code);
}
