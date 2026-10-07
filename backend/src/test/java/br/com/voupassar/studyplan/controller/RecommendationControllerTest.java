package br.com.voupassar.studyplan.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.security.UserPrincipal;
import br.com.voupassar.studyplan.dto.StudyPlanItemResponse;
import br.com.voupassar.studyplan.dto.StudyPlanResponse;
import br.com.voupassar.studyplan.service.RecommendationService;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Autorização do roteiro (TASK 16.1 — P1 IDOR).
 *
 * <p>Regras: sem token → 401; dono divergente → 403; dono → 200 com DTO
 * (sem entidade JPA vazada). Não existe mais {@code ?userId}.
 */
@ExtendWith(MockitoExtension.class)
class RecommendationControllerTest {

  @Mock RecommendationService service;

  @InjectMocks RecommendationController controller;

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

  private static void authenticate(long userId) {
    UserPrincipal principal = new UserPrincipal(userId, "a@b.c", List.of("STUDENT"), 1);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  private static StudyPlanResponse planOf(long userId) {
    StudyPlanItemResponse item = new StudyPlanItemResponse(
        11L, 5L, null, (short) 1, "motivo", "{\"topic_id\":5}", "TODO",
        OffsetDateTime.parse("2026-10-01T10:00:00Z"),
        OffsetDateTime.parse("2026-10-01T10:00:00Z"));
    return new StudyPlanResponse(
        3L, userId, true, "v2-deterministico", "PESSOAL",
        OffsetDateTime.parse("2026-10-01T10:00:00Z"),
        OffsetDateTime.parse("2026-10-01T10:00:00Z"),
        OffsetDateTime.parse("2026-10-01T10:00:00Z"),
        List.of(item));
  }

  @Test
  void withoutTokenIs401() throws Exception {
    mvc().perform(post("/api/v1/recommendations"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/recommendations/plan"))
        .andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/recommendations/items/11/status").param("status", "DONE"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void ownerGeneratesAndReadsOwnPlan() throws Exception {
    authenticate(1L);
    when(service.generatePlan(1L)).thenReturn(planOf(1L));
    when(service.getPlan(1L)).thenReturn(planOf(1L));

    mvc().perform(post("/api/v1/recommendations"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.items[0].topicId").value(5));

    mvc().perform(get("/api/v1/recommendations/plan"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.userId").value(1))
        .andExpect(jsonPath("$.algorithmVersion").value("v2-deterministico"));
  }

  @Test
  void updateOwnItemIs200() throws Exception {
    authenticate(1L);
    StudyPlanItemResponse done = new StudyPlanItemResponse(
        11L, 5L, null, (short) 1, "motivo", "{\"topic_id\":5}", "DONE", null, null);
    when(service.updateItemStatus(eq(11L), eq("DONE"), eq(1L))).thenReturn(done);

    mvc().perform(post("/api/v1/recommendations/items/11/status").param("status", "DONE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DONE"));
  }

  @Test
  void updateItemOfAnotherStudentIs403() throws Exception {
    authenticate(1L);
    when(service.updateItemStatus(eq(11L), anyString(), eq(1L)))
        .thenThrow(new AccessDeniedException("Item de roteiro de outro aluno."));

    mvc().perform(post("/api/v1/recommendations/items/11/status").param("status", "DONE"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.code").exists());
  }

  @Test
  void missingItemIs404() throws Exception {
    authenticate(1L);
    when(service.updateItemStatus(eq(404L), anyString(), anyLong()))
        .thenThrow(new ResourceNotFoundException("ITEM_NOT_FOUND", "Item do roteiro não encontrado."));

    mvc().perform(post("/api/v1/recommendations/items/404/status").param("status", "DONE"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ITEM_NOT_FOUND"));
  }
}
