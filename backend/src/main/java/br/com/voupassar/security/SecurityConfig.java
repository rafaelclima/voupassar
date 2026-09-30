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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Segurança (TASK 3.5) — JWT próprio + refresh opaco rotativo.
 *
 * <p>Público: health, OpenAPI e o fluxo público do {@code /api/v1/auth/**}
 * (registro, login, refresh, logout por refresh, forgot, reset). Todo o
 * resto — inclusive {@code /me}, troca de senha, logout global e as APIs de
 * provas/conteúdos/questões — exige Bearer válido (filtro JWT antes do
 * chain; 401/403 em JSON via {@link ApiAuthHandlers}).
 *
 * <p>API stateless: CSRF desabilitado (sem sessão/cookie), sessão NEVER.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final ApiAuthHandlers authHandlers;
  private final JwtAuthenticationFilter jwtFilter;
  private final AuthRateLimitFilter rateLimitFilter;

  public SecurityConfig(
      ApiAuthHandlers authHandlers,
      JwtAuthenticationFilter jwtFilter,
      AuthRateLimitFilter rateLimitFilter) {
    this.authHandlers = authHandlers;
    this.jwtFilter = jwtFilter;
    this.rateLimitFilter = rateLimitFilter;
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
                    // Auth público (TASK 3.5): o resto do /auth exige Bearer.
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/password/forgot").permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/password/reset").permitAll()
                    .anyRequest().authenticated())
        // Sem login form / httpBasic: API retorna 401 JSON em vez de página.
        .httpBasic(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        // Ordem: rate limiting de borda → JWT → resto do chain.
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class);
    return http.build();
  }
}
