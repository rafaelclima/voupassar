package br.com.voupassar.auth.repository;

import br.com.voupassar.auth.entity.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {}
