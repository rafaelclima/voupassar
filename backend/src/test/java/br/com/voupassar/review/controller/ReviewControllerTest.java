package br.com.voupassar.review.controller;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.review.dto.ReviewQueueResponse;
import br.com.voupassar.review.service.ReviewService;
import br.com.voupassar.security.UserPrincipal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Contrato JSON do /review/queue sem subir contexto (TASK 4.5).
 */
@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

  @Mock ReviewService service;

  @InjectMocks ReviewController controller;

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

  private static ReviewQueueResponse empty() {
    return new ReviewQueueResponse(
        0L, 0L, 0L, 0L, 0L, null, 0L, 0L, 0L, 20, false, null, null,
        List.of(), List.of("nota"));
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(get("/api/v1/review/queue"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void queueIs200() throws Exception {
    authenticate();
    when(service.getQueue(anyLong(), any(), any(), any(), anyBoolean(), isNull()))
        .thenReturn(empty());

    mvc().perform(get("/api/v1/review/queue"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.notes[0]").exists());
  }

  @Test
  void institutionEajReachesService() throws Exception {
    authenticate();
    when(service.getQueue(anyLong(), any(), any(), any(), anyBoolean(), eq("EAJ")))
        .thenReturn(empty());

    mvc().perform(get("/api/v1/review/queue").param("institution", "EAJ"))
        .andExpect(status().isOk());

    verify(service).getQueue(anyLong(), any(), any(), any(), anyBoolean(), eq("EAJ"));
  }
}
