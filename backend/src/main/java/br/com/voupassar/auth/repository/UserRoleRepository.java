package br.com.voupassar.auth.repository;

import br.com.voupassar.auth.entity.UserRole;
import br.com.voupassar.auth.entity.UserRole.UserRoleId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

  @Query("select r.code from UserRole ur join ur.role r where ur.user.id = :userId order by r.code")
  List<String> findRoleCodesByUserId(Long userId);
}
