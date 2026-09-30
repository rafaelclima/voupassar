package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.ExamDocument;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Documentos por edição (atravessa {@code version → exam}).
 *
 * <p>Fetch join evita N+1 no detalhe da edição (6 edições, 12 documentos —
 * volume pequeno, mas sem lazy solto fora da transação).
 */
public interface ExamDocumentRepository extends JpaRepository<ExamDocument, Long> {

  @Query("""
      SELECT d FROM ExamDocument d
      JOIN FETCH d.version v JOIN FETCH v.exam e
      WHERE e.year = :year
      ORDER BY v.versionCode ASC, d.kind ASC, d.fileName ASC
      """)
  List<ExamDocument> findByExamYear(@Param("year") Short year);

  @Query("SELECT COUNT(d) FROM ExamDocument d WHERE d.version.exam.year = :year")
  long countByExamYear(@Param("year") Short year);
}
