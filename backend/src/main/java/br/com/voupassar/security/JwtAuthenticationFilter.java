package br.com.voupassar.security;

import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.auth.service.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Valida o access token JWT (TASK 3.5).
 *
 * <p>Sem {@code Authorization: Bearer ...} segue anônimo (o chain decide
 * 401 nas rotas protegidas). Token válido mas com usuário desativado ou
 * {@code credential_version} divergente (troca/reset de senha) é descartado
 * como anônimo — sem vazar o motivo.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtService jwt;
  private final UserRepository users;

  public JwtAuthenticationFilter(JwtService jwt, UserRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7).trim();
      if (!token.isEmpty()) {
        authenticate(token);
      }
    }
    chain.doFilter(request, response);
  }

  private void authenticate(String token) {
    JwtService.ParsedAccess parsed;
    try {
      parsed = jwt.parseAccessToken(token);
    } catch (JwtException | IllegalArgumentException e) {
      return;
    }
    // Confere ativo + versão vigente sem vazar o motivo na resposta.
    boolean valid =
        users
            .findById(parsed.userId())
            .filter(u -> u.isActive() && u.getCredentialVersion() == parsed.credentialVersion())
            .isPresent();
    if (!valid) {
      return;
    }
    List<SimpleGrantedAuthority> authorities =
        parsed.roles().stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    UserPrincipal principal =
        new UserPrincipal(parsed.userId(), parsed.email(), parsed.roles(), parsed.credentialVersion());
    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, authorities);
    SecurityContextHolder.getContext().setAuthentication(auth);
  }
}
