package br.com.voupassar.content.repository;

import br.com.voupassar.content.entity.Topic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Consultas de assuntos — ordenação canônica por disciplina + código. */
public interface TopicRepository extends JpaRepository<Topic, Long> {

  @Query("""
      SELECT t FROM Topic t
      JOIN FETCH t.discipline d
      ORDER BY d.code ASC, t.code ASC
      """)
  List<Topic> findAllOrdered();

  @Query("""
      SELECT t FROM Topic t
      JOIN FETCH t.discipline d
      WHERE d.code = :disciplineCode
      ORDER BY t.code ASC
      """)
  List<Topic> findByDisciplineCodeOrdered(@Param("disciplineCode") String disciplineCode);

  @Query("""
      SELECT t FROM Topic t
      JOIN FETCH t.discipline d
      WHERE t.id = :id
      """)
  Optional<Topic> findByIdWithDiscipline(@Param("id") Long id);

  long countByDisciplineCode(String disciplineCode);
}
