package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Discipline;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

  List<Discipline> findAllByOrderByCodeAsc();
}
