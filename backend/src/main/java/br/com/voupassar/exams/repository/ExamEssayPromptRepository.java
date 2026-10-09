package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.ExamEssayPrompt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExamEssayPromptRepository extends JpaRepository<ExamEssayPrompt, Long> {

  @Query("SELECT p FROM ExamEssayPrompt p WHERE p.exam.year = :year")
  Optional<ExamEssayPrompt> findByExamYear(@Param("year") Short year);

  /**
   * Prompt por edição composta (TASK E.1): {@code (institution, year)}.
   * EAJ não possui discursiva (has_essay FALSE) — retorna vazio.
   */
  @Query("SELECT p FROM ExamEssayPrompt p WHERE p.exam.institution = :institution AND p.exam.year = :year")
  Optional<ExamEssayPrompt> findByExamInstitutionAndYear(
      @Param("institution") String institution, @Param("year") Short year);
}
