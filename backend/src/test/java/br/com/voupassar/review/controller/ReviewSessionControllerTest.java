package br.com.voupassar.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.review.dto.ReviewSessionResponse;
import br.com.voupassar.review.dto.ReviewSessionResultResponse;
import br.com.voupassar.review.service.ReviewSessionService;
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
 * Contrato JSON das sessões de revisão sem subir contexto (TASK 5.4).
 */
@ExtendWith(MockitoExtension.class)
class ReviewSessionControllerTest {

  @Mock ReviewSessionService service;

  @InjectMocks ReviewSessionController controller;

  private MockMvc mvc() {
    return MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new br.com.voupassar.exception.GlobalExceptionHandler())
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

  private static ReviewSessionResponse response() {
    return new ReviewSessionResponse(
        7L, "REVISAO", "IN_PROGRESS",
        OffsetDateTime.parse("2026-10-01T10:00:00Z"), null,
        2, 1L, 1L, List.of(), List.of("nota"));
  }

  private static ReviewSessionResultResponse result() {
    return new ReviewSessionResultResponse(
        7L, "REVISAO", "FINISHED",
        2L, 2L, 0L, 2L, 1L, 1L, 0L, 0.5,
        List.of(), List.of("nota"));
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(post("/api/v1/review/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/review/sessions/7")).andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/review/sessions/7/result")).andExpect(status().isUnauthorized());
  }

  @Test
  void createReturns201() throws Exception {
    authenticate();
    when(service.create(anyLong(), any())).thenReturn(response());

    mvc().perform(post("/api/v1/review/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"limit\":10,\"onlyErrors\":true}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sessionId").value(7))
        .andExpect(jsonPath("$.mode").value("REVISAO"))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  void createWithEmptyBodyReturns201WithDefaults() throws Exception {
    authenticate();
    when(service.create(anyLong(), any())).thenReturn(response());

    mvc().perform(post("/api/v1/review/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.sessionId").value(7));
  }

  @Test
  void createValidationFails400() throws Exception {
    authenticate();

    mvc().perform(post("/api/v1/review/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"limit\":0}"))
        .andExpect(status().isBadRequest());
    mvc().perform(post("/api/v1/review/sessions")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"limit\":101}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getAndResultAre200() throws Exception {
    authenticate();
    when(service.get(anyLong(), anyLong())).thenReturn(response());
    when(service.getResult(anyLong(), anyLong())).thenReturn(result());

    mvc().perform(get("/api/v1/review/sessions/7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionId").value(7))
        .andExpect(jsonPath("$.totalItems").value(2));
    mvc().perform(get("/api/v1/review/sessions/7/result"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accuracy").value(0.5));
  }
}
