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
 * Rate limiting de borda (TASK 3.5, estendido na TASK 22.1, AGENTS.md §15).
 *
 * <p>Janela fixa em memória por (IP + caminho):
 * <ul>
 *   <li>{@code /api/v1/auth/**} (todos os métodos): {@code max-requests}/janela;</li>
 *   <li>escrita em {@code /api/v1/attempts}, {@code /api/v1/simulations},
 *       {@code /api/v1/recommendations} (POST/PUT/PATCH/DELETE):
 *       {@code write-max-requests}/janela.</li>
 * </ul>
 * Leituras (GET) das rotas de escrita não são limitadas. Responde 429 no
 * envelope padrão com header {@code Retry-After} (segundos até resetar a
 * janela). O proxy reverso em produção aplica o limite real por IP público.
 * Desligável em teste ({@code app.security.rate-limit.enabled=false}).
 */
@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

  private record Window(long count, Instant windowStart) {}

  private final AuthProperties props;
  private final ClientIpResolver clientIp;
  private final ObjectMapper mapper;
  private final Map<String, Window> buckets = new ConcurrentHashMap<>();

  public AuthRateLimitFilter(AuthProperties props, ClientIpResolver clientIp, ObjectMapper mapper) {
    this.props = props;
    this.clientIp = clientIp;
    this.mapper = mapper;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    if (!props.getRateLimit().isEnabled()) {
      return true;
    }
    String uri = request.getRequestURI();
    if (uri.startsWith("/api/v1/auth/")) {
      return false;
    }
    return !isWrite(request) || !isWriteScope(uri);
  }

  private static boolean isWrite(HttpServletRequest request) {
    String method = request.getMethod();
    return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
        || "PATCH".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method);
  }

  private static boolean isWriteScope(String uri) {
    return uri.startsWith("/api/v1/attempts")
        || uri.startsWith("/api/v1/simulations")
        || uri.startsWith("/api/v1/recommendations");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    AuthProperties.RateLimit cfg = props.getRateLimit();
    int limit = request.getRequestURI().startsWith("/api/v1/auth/")
        ? cfg.getMaxRequests()
        : cfg.getWriteMaxRequests();
    String key = clientIp.resolve(request) + "|" + request.getRequestURI();
    Instant now = Instant.now();
    Window next =
        buckets.compute(key, (k, prev) -> {
          if (prev == null || now.isAfter(prev.windowStart().plusSeconds(cfg.getWindowSeconds()))) {
            return new Window(1, now);
          }
          return new Window(prev.count() + 1, prev.windowStart());
        });
    if (next.count() > limit) {
      long elapsed = next.windowStart().until(now, java.time.temporal.ChronoUnit.SECONDS);
      long retryAfter = Math.max(1L, cfg.getWindowSeconds() - elapsed);
      write429(request, response, retryAfter);
      return;
    }
    chain.doFilter(request, response);
  }

  private void write429(HttpServletRequest request, HttpServletResponse response, long retryAfter)
      throws IOException {
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
    response.setHeader("Retry-After", Long.toString(retryAfter));
    mapper.writeValue(response.getWriter(), body);
  }
}
