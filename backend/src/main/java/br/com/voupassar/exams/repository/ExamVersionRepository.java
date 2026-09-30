package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.ExamVersion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamVersionRepository extends JpaRepository<ExamVersion, Long> {

  List<ExamVersion> findByExamYearOrderByVersionCodeAsc(Short year);

  long countByExamYear(Short year);
}
