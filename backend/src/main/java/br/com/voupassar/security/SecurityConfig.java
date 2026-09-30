package br.com.voupassar.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Segurança base do bootstrap (TASK 3.1) — secure-by-default.
 *
 * <p>Público: {@code GET /api/v1/health} e {@code /actuator/health}.
 * Todo o resto exige autenticação (JWT completo na TASK 3.5; até lá,
 * sem emissor de token, as rotas protegidas respondem 401/403 — que é
 * o comportamento seguro correto).
 *
 * <p>API stateless: CSRF desabilitado (sem sessão/cookie), sessão NEVER.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final ApiAuthHandlers authHandlers;

  public SecurityConfig(ApiAuthHandlers authHandlers) {
    this.authHandlers = authHandlers;
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            eh ->
                eh.authenticationEntryPoint(authHandlers).accessDeniedHandler(authHandlers))
        .authorizeHttpRequests(
            auth ->
                auth
                    .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                    .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                    // OpenAPI (TASK 3.2): só schemas, sem PII e sem conteúdo
                    // de questão — seguro expor para leitura.
                    .requestMatchers(HttpMethod.GET, "/v3/api-docs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/swagger-ui.html").permitAll()
                    .anyRequest().authenticated())
        // Sem login form / httpBasic: API retorna 401 JSON em vez de página.
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
