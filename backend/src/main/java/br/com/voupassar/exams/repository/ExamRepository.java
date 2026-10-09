package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Exam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas de edições — ordenação canônica por ano crescente. */
public interface ExamRepository extends JpaRepository<Exam, Long> {

  List<Exam> findAllByOrderByYearAsc();

  List<Exam> findAllByOrderByInstitutionAscYearAsc();

  List<Exam> findByInstitutionOrderByYearAsc(String institution);

  Optional<Exam> findByYear(Short year);

  /**
   * Lookup por edição composta (TASK C.1; filtro {@code ?institution=} na E.1).
   *
   * <p>Ano sozinho nunca decide a edição (EAJ-2022 ≠ IFRN-2022). Os métodos
   * legados por ano seguem válidos somente no universo IFRN até a E.1
   * religar os chamadores.
   */
  Optional<Exam> findByInstitutionAndYear(String institution, Short year);
}
