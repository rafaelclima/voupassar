package br.com.voupassar.simulations.repository;

import br.com.voupassar.simulations.entity.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Definições de simulado (TASK 5.1) — escrita na criação, leitura nas
 * consultas de execução.
 */
public interface SimulationRepository extends JpaRepository<Simulation, Long> {}
