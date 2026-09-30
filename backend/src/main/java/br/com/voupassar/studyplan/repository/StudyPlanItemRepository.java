package br.com.voupassar.studyplan.repository;

import br.com.voupassar.studyplan.entity.StudyPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyPlanItemRepository extends JpaRepository<StudyPlanItem, Long> {
  List<StudyPlanItem> findByStudyPlanIdOrderByPriorityAsc(Long studyPlanId);
}
