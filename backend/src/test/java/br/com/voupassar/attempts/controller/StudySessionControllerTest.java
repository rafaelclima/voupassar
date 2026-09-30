package br.com.voupassar.attempts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.attempts.dto.StudySessionResponse;
import br.com.voupassar.attempts.service.AttemptService;
import br.com.voupassar.exception.GlobalExceptionHandler;
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
 * Contrato JSON de /study-sessions sem subir contexto (TASK 3.7).
 */
@ExtendWith(MockitoExtension.class)
class StudySessionControllerTest {

  @Mock AttemptService service;

  @InjectMocks StudySessionController controller;

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

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(post("/api/v1/study-sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"ESTUDO\"}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/study-sessions/7")).andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/study-sessions/7/finish")).andExpect(status().isUnauthorized());
  }

  @Test
  void createReturns201() throws Exception {
    authenticate();
    when(service.createSession(anyLong(), any())).thenReturn(
        new StudySessionResponse(7L, "ESTUDO", "IN_PROGRESS",
            OffsetDateTime.parse("2026-09-30T10:00:00Z"), null));

    mvc().perform(post("/api/v1/study-sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"ESTUDO\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  void createValidationFails400() throws Exception {
    authenticate();

    mvc().perform(post("/api/v1/study-sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"JOGO\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void finishReturns200() throws Exception {
    authenticate();
    when(service.finishSession(1L, 7L)).thenReturn(
        new StudySessionResponse(7L, "ESTUDO", "FINISHED",
            OffsetDateTime.parse("2026-09-30T10:00:00Z"),
            OffsetDateTime.parse("2026-09-30T11:00:00Z")));

    mvc().perform(post("/api/v1/study-sessions/7/finish"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("FINISHED"));
  }
}
