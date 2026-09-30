package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Exam;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas de edições — ordenação canônica por ano crescente. */
public interface ExamRepository extends JpaRepository<Exam, Long> {

  List<Exam> findAllByOrderByYearAsc();

  Optional<Exam> findByYear(Short year);
}
