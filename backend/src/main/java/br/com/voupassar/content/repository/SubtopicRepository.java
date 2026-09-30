package br.com.voupassar.content.repository;

import br.com.voupassar.content.entity.Subtopic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Consultas de subassuntos — sempre com tópico + disciplina (evita N+1). */
public interface SubtopicRepository extends JpaRepository<Subtopic, Long> {

  @Query("""
      SELECT s FROM Subtopic s
      JOIN FETCH s.topic t JOIN FETCH t.discipline d
      ORDER BY d.code ASC, t.code ASC, s.code ASC
      """)
  List<Subtopic> findAllOrdered();

  @Query("""
      SELECT s FROM Subtopic s
      JOIN FETCH s.topic t JOIN FETCH t.discipline d
      WHERE t.id = :topicId
      ORDER BY s.code ASC
      """)
  List<Subtopic> findByTopicIdOrdered(@Param("topicId") Long topicId);

  @Query("""
      SELECT s FROM Subtopic s
      JOIN FETCH s.topic t JOIN FETCH t.discipline d
      WHERE s.id = :id
      """)
  Optional<Subtopic> findByIdWithTopic(@Param("id") Long id);

  long countByTopicId(Long topicId);
}
