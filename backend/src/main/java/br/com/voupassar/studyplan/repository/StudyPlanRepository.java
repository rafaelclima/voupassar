package br.com.voupassar.studyplan.repository;

import br.com.voupassar.studyplan.entity.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
  Optional<StudyPlan> findByUserIdAndIsActiveTrue(Long userId);
  Optional<StudyPlan> findByUserId(Long userId);
}
