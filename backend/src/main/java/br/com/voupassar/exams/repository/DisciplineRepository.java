package br.com.voupassar.exams.repository;

import br.com.voupassar.exams.entity.Discipline;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisciplineRepository extends JpaRepository<Discipline, Long> {

  List<Discipline> findAllByOrderByCodeAsc();

  Optional<Discipline> findByCode(String code);
}
