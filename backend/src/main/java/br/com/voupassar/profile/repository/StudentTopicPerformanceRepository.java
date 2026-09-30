package br.com.voupassar.profile.repository;

import br.com.voupassar.profile.entity.StudentTopicPerformance;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

  /**
   * Apaga o agregado do aluno antes do rebuild determinístico (TASK 4.1).
   *
   * <p>Bulk DELETE explícito (executa imediatamente no banco, sem carregar
   * entidades): o rebuild recalcula tudo a partir do fato imutável {@code
   * question_attempts} — delete + insert na mesma transação, sem UPDATE
   * parcial, reexecução segura e auditável.
   *
   * <p>NÃO usar `deleteBy` derivado aqui: ele carrega as linhas para a
   * memória e agenda `remove()` no contexto de persistência, e o flush do
   * Hibernate ordena INSERTs antes de DELETEs — o rebuild falharia com
   * violação da UNIQUE `(user, topic, subtopic)`. Sem
   * `clearAutomatically` de propósito: o rebuild roda na mesma transação do
   * `POST /attempts`, e limpar o contexto descartaria o INSERT da tentativa
   * recém-registrada (ainda pendente de flush).
   */
  @Modifying
  @Query("DELETE FROM StudentTopicPerformance p WHERE p.user.id = :userId")
  void deleteByUserId(@Param("userId") Long userId);
}
