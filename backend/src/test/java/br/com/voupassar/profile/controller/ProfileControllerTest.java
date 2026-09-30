package br.com.voupassar.profile.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.profile.dto.AttemptHistoryResponse;
import br.com.voupassar.profile.dto.ProfileResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse;
import br.com.voupassar.profile.service.ProfileService;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.security.UserPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Contrato JSON do /profile sem subir contexto (TASK 3.6).
 *
 * <p>Rotas autenticadas recebem o principal via SecurityContext (mesma
 * thread) + {@code AuthenticationPrincipalArgumentResolver} explícito.
 */
@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

  @Mock ProfileService service;

  @InjectMocks ProfileController controller;

  private MockMvc mvc() {
    return MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  private static void authenticate() {
    UserPrincipal principal = new UserPrincipal(1L, "a@b.c", List.of("STUDENT"), 1);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  private static ProfileResponse profile() {
    return new ProfileResponse(1L, "a@b.c", "Aluno", "9º ano", 2027, "Passar", List.of("STUDENT"));
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(get("/api/v1/profile")).andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/profile/stats")).andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/profile/history")).andExpect(status().isUnauthorized());
    mvc().perform(put("/api/v1/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"displayName\":\"Aluno\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getProfileIs200() throws Exception {
    authenticate();
    when(service.getProfile(1L)).thenReturn(profile());

    mvc().perform(get("/api/v1/profile"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("a@b.c"))
        .andExpect(jsonPath("$.displayName").value("Aluno"))
        .andExpect(jsonPath("$.roles[0]").value("STUDENT"));
  }

  @Test
  void updateProfileValidationFails400() throws Exception {
    authenticate();

    mvc().perform(put("/api/v1/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"displayName\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void updateProfileIs200() throws Exception {
    authenticate();
    when(service.updateProfile(anyLong(), any())).thenReturn(profile());

    mvc().perform(put("/api/v1/profile")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"displayName\":\"Novo Nome\",\"targetYear\":2028}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.displayName").value("Aluno"));
  }

  @Test
  void statsIs200WithNotes() throws Exception {
    authenticate();
    when(service.getStats(1L)).thenReturn(new ProfileStatsResponse(
        0L, 0L, 0L, 0L, 0L, null, null, List.of(), List.of(), List.of(),
        List.of("Nenhuma tentativa registrada.")));

    mvc().perform(get("/api/v1/profile/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAttempts").value(0))
        .andExpect(jsonPath("$.notes[0]").exists());
  }

  @Test
  void historyIs200AndBadPaginationIs400() throws Exception {
    authenticate();
    when(service.getHistory(anyLong(), anyInt(), anyInt())).thenReturn(new PageResponse<>(
        List.of(new AttemptHistoryResponse(101L, 12L, 2026, 17, "MATEMATICA", "Matemática",
            "C", true, false, "ESTUDO", 95, null, null,
            OffsetDateTime.parse("2026-09-01T10:00:00Z"))),
        0, 20, 1, 1, true, true));

    mvc().perform(get("/api/v1/profile/history?page=0&size=20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].questionId").value(12))
        .andExpect(jsonPath("$.totalElements").value(1));

    when(service.getHistory(anyLong(), anyInt(), anyInt()))
        .thenThrow(new BadRequestException("Tamanho de página inválido: 101 (permitido 1–100)."));
    mvc().perform(get("/api/v1/profile/history?page=0&size=101"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").exists());
  }
}
