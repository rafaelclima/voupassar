package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.ExamEssayPrompt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamEssayPromptRepository extends JpaRepository<ExamEssayPrompt, Long> {

  @Query("SELECT p FROM ExamEssayPrompt p WHERE p.exam.year = :year")
  Optional<ExamEssayPrompt> findByExamYear(@Param("year") Short year);
}
