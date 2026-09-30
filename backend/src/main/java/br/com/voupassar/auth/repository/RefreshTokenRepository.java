package br.com.voupassar.auth.repository;

import br.com.voupassar.auth.entity.RefreshToken;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByTokenHash(String tokenHash);

  List<RefreshToken> findByUserIdAndRevokedAtIsNull(Long userId);

  /**
   * Revoga todos os refreshes ativos do usuário (logout global, troca/reset
   * de senha). Instante por parâmetro: {@code CURRENT_TIMESTAMP} do HQL é
   * {@code Timestamp} e o Hibernate 7 não o atribui a {@code OffsetDateTime}.
   */
  @Modifying
  @Query("update RefreshToken t set t.revokedAt = :now "
      + "where t.user.id = :userId and t.revokedAt is null")
  int revokeAllActiveByUserId(Long userId, OffsetDateTime now);
}
