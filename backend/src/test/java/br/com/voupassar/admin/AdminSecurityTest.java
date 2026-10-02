package br.com.voupassar.admin;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.admin.dto.AdminMetricsResponse;
import br.com.voupassar.admin.service.AdminService;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.security.UserPrincipal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Autorização da área administrativa no contexto real (TASK 12.1).
 *
 * <p>Serviço mockado (sem banco): aqui só importa quem passa pelo
 * {@code @PreAuthorize}. A autenticação usa {@link UserPrincipal} de verdade
 * (o filtro JWT produz esse tipo em produção; {@code @WithMockUser} entrega
 * {@code String} e o controller responderia 401 por principal nulo).
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

  private void stubQueue() {
    when(service.reviewQueue(anyString(), anyInt(), anyInt()))
        .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));
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

    mvc.perform(patch("/api/v1/admin/questions/1/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"validationStatus\":\"REVIEWED\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  void curatorReads() throws Exception {
    authenticate("CURATOR");
    stubQueue();
    when(service.metrics())
        .thenReturn(new AdminMetricsResponse(240, Map.of(), Map.of(), 5, 36, 240, Map.of()));

    mvc.perform(get("/api/v1/admin/review-queue")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/admin/inconsistencies")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/admin/metrics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questionsTotal").value(240));
  }

  @Test
  void adminReads() throws Exception {
    authenticate("ADMIN");
    stubQueue();

    mvc.perform(get("/api/v1/admin/review-queue")).andExpect(status().isOk());
    mvc.perform(get("/api/v1/admin/metrics")).andExpect(status().isOk());
  }
}
