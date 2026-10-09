package br.com.voupassar.studyplan.repository;

import br.com.voupassar.studyplan.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
  Optional<StudyPlan> findByUserIdAndIsActiveTrue(Long userId);
  Optional<StudyPlan> findByUserId(Long userId);

  /**
   * Roteiro vigente de uma trilha (TASK E.2, V21).
   *
   * <p>Um ativo por {@code (user_id, institution)}: IFRN e EAJ coexistem sem
   * se desativarem. O método legado (só usuário) segue para compatibilidade
   * com planos pré-V21 (todos IFRN).
   */
  Optional<StudyPlan> findByUserIdAndInstitutionAndIsActiveTrue(Long userId, String institution);
}
