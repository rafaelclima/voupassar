package br.com.voupassar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * VouPassar — ponto de entrada da API.
 *
 * <p>TASK 3.1 (bootstrap): sem regra de negócio aqui. Domínios futuros
 * ({@code exams}, {@code questions}, {@code attempts}, ...) entram como
 * pacotes próprios {@code controller → service → repository}
 * (ver docs/architecture.md §3).
 */
@SpringBootApplication
public class VoupassarApplication {

  public static void main(String[] args) {
    SpringApplication.run(VoupassarApplication.class, args);
  }
}
