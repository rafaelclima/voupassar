package br.com.voupassar.security;

import br.com.voupassar.auth.config.AuthProperties;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * IP do cliente para rate limiting e auditoria (TASK 22.1).
 *
 * <p>{@code X-Forwarded-For} só é confiável atrás de proxy configurado
 * ({@code app.security.behind-proxy=true}, env {@code BEHIND_PROXY} — a VPS
 * roda atrás de proxy reverso com IP real no header). Sem proxy, o header é
 * ignorado (qualquer cliente direto poderia forjá-lo) e vale
 * {@code getRemoteAddr()}.
 */
@Component
public class ClientIpResolver {

  private final AuthProperties props;

  public ClientIpResolver(AuthProperties props) {
    this.props = props;
  }

  public String resolve(HttpServletRequest request) {
    if (props.isBehindProxy()) {
      String forwarded = request.getHeader("X-Forwarded-For");
      if (forwarded != null && !forwarded.isBlank()) {
        int comma = forwarded.indexOf(',');
        return (comma < 0 ? forwarded : forwarded.substring(0, comma)).trim();
      }
    }
    String remote = request.getRemoteAddr();
    return remote != null ? remote : "-";
  }
}
