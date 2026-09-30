package br.com.voupassar.attempts.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.attempts.dto.AttemptResponse;
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
 * Contrato JSON de /attempts sem subir contexto (TASK 3.7).
 */
@ExtendWith(MockitoExtension.class)
class AttemptControllerTest {

  @Mock AttemptService service;

  @InjectMocks AttemptController controller;

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

  private static AttemptResponse attempt() {
    return new AttemptResponse(101L, 12L, "C", Boolean.TRUE, false, "ESTUDO",
        95, 7L, null, OffsetDateTime.parse("2026-09-30T10:00:00Z"), List.of());
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(post("/api/v1/attempts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"questionId\":12,\"selectedOption\":\"C\",\"mode\":\"ESTUDO\",\"studySessionId\":7}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/attempts/101")).andExpect(status().isUnauthorized());
  }

  @Test
  void submitReturns201WithCorrection() throws Exception {
    authenticate();
    when(service.submitAttempt(anyLong(), any())).thenReturn(attempt());

    mvc().perform(post("/api/v1/attempts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"questionId\":12,\"selectedOption\":\"C\",\"mode\":\"ESTUDO\","
                + "\"timeSpentSeconds\":95,\"studySessionId\":7}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(101))
        .andExpect(jsonPath("$.questionId").value(12))
        .andExpect(jsonPath("$.isCorrect").value(true));
  }

  @Test
  void submitValidationFails400() throws Exception {
    authenticate();

    mvc().perform(post("/api/v1/attempts")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"questionId\":12,\"selectedOption\":\"E\",\"mode\":\"ESTUDO\",\"studySessionId\":7}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getReturns200() throws Exception {
    authenticate();
    when(service.getAttempt(1L, 101L)).thenReturn(attempt());

    mvc().perform(get("/api/v1/attempts/101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.questionId").value(12));
  }
}
