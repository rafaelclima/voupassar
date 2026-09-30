package br.com.voupassar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/** Sobe o contexto com profile test (H2, sem Postgres). */
@SpringBootTest
@ActiveProfiles("test")
class VoupassarApplicationTests {

  @Test
  void contextLoads() {}
}
