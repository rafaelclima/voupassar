package br.com.voupassar.profile.repository;

import br.com.voupassar.profile.entity.StudentTopicPerformance;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Leituras do agregado por conteúdo (TASK 3.6).
 *
 * <p>Escrito pelo job da Fase 4 (TASK 4.1), nunca pelo cliente. Enquanto a
 * 4.1 não existe, a tabela tende a estar vazia — o serviço sinaliza isso em
 * {@code notes} em vez de inventar progresso.
 */
public interface StudentTopicPerformanceRepository
    extends JpaRepository<StudentTopicPerformance, Long> {

  /**
   * Progresso do aluno com tópico (+ disciplina) e subassunto em fetch,
   * piores aproveitamentos primeiro (NULLs por último — sem tentativas).
   */
  @Query("""
      SELECT p FROM StudentTopicPerformance p
      LEFT JOIN FETCH p.topic t
      LEFT JOIN FETCH t.discipline d
      LEFT JOIN FETCH p.subtopic s
      WHERE p.user.id = :userId
      ORDER BY p.accuracy ASC NULLS LAST, p.attempts DESC, p.id ASC
      """)
  List<StudentTopicPerformance> findProgressByUserId(@Param("userId") Long userId);

  long countByUserId(Long userId);
}
