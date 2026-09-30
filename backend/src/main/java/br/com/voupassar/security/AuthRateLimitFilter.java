package br.com.voupassar.security;

import br.com.voupassar.auth.config.AuthProperties;
import br.com.voupassar.common.dto.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

/**
 * Rate limiting básico do {@code /api/v1/auth/**} (TASK 3.5, AGENTS.md §15).
 *
 * <p>Janela fixa em memória por (IP + caminho): padrão 60 req/min.
 * Proteção de borda — o proxy reverso em produção aplica o limite real por
 * IP público. Responde 429 no envelope padrão. Desligável em teste
 * ({@code app.security.rate-limit.enabled=false}).
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

  private record Window(long count, Instant windowStart) {}

  private final AuthProperties props;
  private final ObjectMapper mapper;
  private final Map<String, Window> buckets = new ConcurrentHashMap<>();

  public AuthRateLimitFilter(AuthProperties props, ObjectMapper mapper) {
    this.props = props;
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !props.getRateLimit().isEnabled()
        || !request.getRequestURI().startsWith("/api/v1/auth/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    AuthProperties.RateLimit cfg = props.getRateLimit();
    String key = clientIp(request) + "|" + request.getRequestURI();
    Instant now = Instant.now();
    Window next =
        buckets.compute(key, (k, prev) -> {
          if (prev == null || now.isAfter(prev.windowStart().plusSeconds(cfg.getWindowSeconds()))) {
            return new Window(1, now);
          }
          return new Window(prev.count() + 1, prev.windowStart());
        });
    if (next.count() > cfg.getMaxRequests()) {
      write429(request, response);
      return;
    }
    chain.doFilter(request, response);
  }

  private static String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      int comma = forwarded.indexOf(',');
      return (comma < 0 ? forwarded : forwarded.substring(0, comma)).trim();
    }
    return request.getRemoteAddr();
  }

  private void write429(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String traceId = MDC.get("traceId");
    ApiError body =
        new ApiError(
            "RATE_LIMITED",
            "Muitas tentativas. Aguarde e tente novamente.",
            (List<String>) null,
            traceId != null ? traceId : "-",
            java.time.OffsetDateTime.now(),
            request.getRequestURI());
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    mapper.writeValue(response.getWriter(), body);
  }
}
