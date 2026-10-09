package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.ExamVersion;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamVersionRepository extends JpaRepository<ExamVersion, Long> {

  List<ExamVersion> findByExamYearOrderByVersionCodeAsc(Short year);

  long countByExamYear(Short year);

  /**
   * Versões por edição composta (TASK E.1).
   *
   * <p>Ano sozinho nunca decide a edição (EAJ-2022 ≠ IFRN-2022). Os métodos
   * legados por ano seguem para compatibilidade (universo IFRN em anos sem
   * colisão); o caminho E.1 usa sempre {@code (institution, year)}.
   */
  List<ExamVersion> findByExamInstitutionAndExamYearOrderByVersionCodeAsc(
      String institution, Short year);

  long countByExamInstitutionAndExamYear(String institution, Short year);
}
