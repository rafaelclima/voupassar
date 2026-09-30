package br.com.voupassar.security;

import br.com.voupassar.common.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * Erros 401/403 em JSON no mesmo envelope {@link ApiError} — sem redirect
 * para login e sem corpo vazio (API stateless).
 */
@Component
public class ApiAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

  private final ObjectMapper mapper;

  public ApiAuthHandlers(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  private String traceId(HttpServletRequest req) {
    String mdc = MDC.get("traceId");
    if (mdc != null) {
      return mdc;
    }
    String header = req.getHeader("X-Trace-Id");
    return header != null ? header : "-";
  }

  private void write(
      HttpServletRequest req, HttpServletResponse res, int status, String code, String message)
      throws IOException {
    ApiError body =
        new ApiError(code, message, (List<String>) null, traceId(req), OffsetDateTime.now(),
            req.getRequestURI());
    res.setStatus(status);
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    res.setCharacterEncoding("UTF-8");
    mapper.writeValue(res.getWriter(), body);
  }

  @Override
  public void commence(
      HttpServletRequest req, HttpServletResponse res, AuthenticationException ex)
      throws IOException {
    write(req, res, HttpServletResponse.SC_UNAUTHORIZED, "UNAUTHORIZED", "Autenticação necessária.");
  }

  @Override
  public void handle(
      HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex)
      throws IOException {
    write(req, res, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN", "Acesso negado.");
  }
}
