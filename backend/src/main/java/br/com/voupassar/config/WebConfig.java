package br.com.voupassar.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS restritivo (AGENTS.md §15).
 *
 * <p>Origens exatas via {@code app.cors.allowed-origins} (env
 * {@code CORS_ALLOWED_ORIGINS}, separadas por vírgula). Padrão: só
 * localhost dev. Produção deve fixar o domínio do GitHub Pages.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final List<String> allowedOrigins;

  public WebConfig(
      @Value("${app.cors.allowed-origins:http://localhost:8080,http://localhost:3000}")
          String origins) {
    this.allowedOrigins =
        Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins(allowedOrigins.toArray(String[]::new))
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("X-Trace-Id")
        .maxAge(3600);
  }
}
