package br.com.voupassar.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Beans de autenticação (TASK 3.5).
 *
 * <p>BCrypt com custo configurável ({@code app.security.bcrypt-strength},
 * padrão 10 — calibrado para a VPS de 2 GB; não subir sem medir).
 */
@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfig {

  @Bean
  PasswordEncoder passwordEncoder(AuthProperties props) {
    int strength = Math.min(Math.max(props.getBcryptStrength(), 4), 14);
    return new BCryptPasswordEncoder(strength);
  }
}
