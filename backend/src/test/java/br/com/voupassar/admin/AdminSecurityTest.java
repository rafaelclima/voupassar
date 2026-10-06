package br.com.voupassar.admin;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.admin.dto.AdminMetricsResponse;
import br.com.voupassar.admin.service.AdminService;
import br.com.voupassar.security.UserPrincipal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Autorização da área administrativa no contexto real.
 *
 * <p>Serviço mockado (sem banco): aqui só importa quem passa pelo
 * {@code @PreAuthorize}. Endpoints vivos: {@code /inconsistencies} e
 * {@code /metrics} (observabilidade, sem curadoria desde 2026-10-06).
 * Regras de negócio estão em {@code AdminServiceTest}.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminSecurityTest {

  @Autowired MockMvc mvc;

  @MockitoBean AdminService service;

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  private static void authenticate(String... roles) {
    UserPrincipal principal = new UserPrincipal(7L, "curador@exemplo.com", List.of(roles), 1);
    List<SimpleGrantedAuthority> authorities =
        List.of(roles).stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r)).toList();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, authorities));
  }

  @Test
  void anonymousIs401InEnvelope() throws Exception {
    mvc.perform(get("/api/v1/admin/metrics"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().exists("X-Trace-Id"))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void studentIs403() throws Exception {
    authenticate("STUDENT");

    mvc.perform(get("/api/v1/admin/metrics"))
        .andExpect(status().isForbidden())
        .andExpect(header().exists("X-Trace-Id"))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());

    mvc.perform(get("/api/v1/admin/inconsistencies"))
        .andExpect(status().isForbidden());
  }

  @Test
  void curatorReads() throws Exception {
    authenticate("CURATOR");
    when(service.metrics()).thenReturn(new AdminMetricsResponse(240, 5, 36, 240));
    when(service.inconsistencies()).thenReturn(List.of());

    mvc.perform(get("/api/v1/admin/inconsistencies")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/admin/metrics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questionsTotal").value(240));
  }

  @Test
  void adminReads() throws Exception {
    authenticate("ADMIN");
    when(service.metrics()).thenReturn(new AdminMetricsResponse(240, 5, 36, 240));
    when(service.inconsistencies()).thenReturn(List.of());

    mvc.perform(get("/api/v1/admin/inconsistencies")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/admin/metrics")).andExpect(status().isOk());
  }
}
