package br.com.voupassar.health;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health público mínimo (TASK 3.1).
 *
 * <p>Responde sem depender do banco: distingue "app UP" de "DB DOWN"
 * (o detalhe do banco fica em {@code /actuator/health}, interno).
 * Rota propositalmente pública — ver SecurityConfig.
 */
@RestController
@RequestMapping(path = "/api/v1/health", produces = MediaType.APPLICATION_JSON_VALUE)
public class HealthController {

  private final String service;
  private final String version;

  public HealthController(
      @Value("${app.service-name:voupassar-backend}") String service,
      @Value("${app.version:0.1.0-SNAPSHOT}") String version) {
    this.service = service;
    this.version = version;
  }

  @GetMapping
  public Map<String, Object> health() {
    return Map.of(
        "status", "UP",
        "service", service,
        "version", version,
        "timestamp", OffsetDateTime.now().toString());
  }
}
