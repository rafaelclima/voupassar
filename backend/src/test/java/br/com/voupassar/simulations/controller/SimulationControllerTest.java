package br.com.voupassar.simulations.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.security.UserPrincipal;
import br.com.voupassar.simulations.dto.SimulationAttemptResponse;
import br.com.voupassar.simulations.dto.SimulationAttemptSummary;
import br.com.voupassar.simulations.dto.SimulationResultResponse;
import br.com.voupassar.simulations.dto.StudyFeedbackResponse;
import br.com.voupassar.simulations.service.SimulationService;
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
 * Contrato JSON de /simulations sem subir contexto (TASK 5.1).
 */
@ExtendWith(MockitoExtension.class)
class SimulationControllerTest {

  @Mock SimulationService service;

  @InjectMocks SimulationController controller;

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

  private static SimulationAttemptResponse attempt() {
    return new SimulationAttemptResponse(55L, 7L, "BY_DISCIPLINE",
        "Simulado Matemática — 10 questões [PROVA]", "MATEMATICA", "Matemática",
        "PROVA", "IN_PROGRESS", 10, OffsetDateTime.parse("2026-10-01T10:00:00Z"), null,
        List.of(), null, List.of());
  }

  private static SimulationResultResponse result() {
    return new SimulationResultResponse(55L, 7L, "Simulado Matemática — 10 questões [PROVA]",
        "MATEMATICA", "PROVA", "SUBMITTED", 10L, 10L, 0L, 10L, 7L, 3L, 0L, 0.7,
        List.of(), List.of());
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(post("/api/v1/simulations/by-discipline")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"disciplineCode\":\"MATEMATICA\",\"questionCount\":10,\"mode\":\"PROVA\"}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/simulations/by-edition")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"editionYear\":2026,\"mode\":\"PROVA\"}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/attempts")).andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/attempts/55")).andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/simulations/attempts/55/submit"))
        .andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/simulations/attempts/55/abandon"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/attempts/55/result"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/attempts/55/feedback/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createReturns201() throws Exception {
    authenticate();
    when(service.createByDiscipline(anyLong(), any())).thenReturn(attempt());

    mvc().perform(post("/api/v1/simulations/by-discipline")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"disciplineCode\":\"MATEMATICA\",\"questionCount\":10,\"mode\":\"PROVA\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.attemptId").value(55))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  void createValidationFails400() throws Exception {
    authenticate();

    mvc().perform(post("/api/v1/simulations/by-discipline")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"disciplineCode\":\"MATEMATICA\",\"questionCount\":0,\"mode\":\"PROVA\"}"))
        .andExpect(status().isBadRequest());
    mvc().perform(post("/api/v1/simulations/by-discipline")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"disciplineCode\":\"MATEMATICA\",\"questionCount\":5,\"mode\":\"REVISAO\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void createByEditionReturns201() throws Exception {
    authenticate();
    when(service.createByEdition(anyLong(), any())).thenReturn(new SimulationAttemptResponse(
        56L, 8L, "REAL_EDITION", "Simulado Edição 2026 — 40 questões [PROVA]",
        null, null, "PROVA", "IN_PROGRESS", 40,
        OffsetDateTime.parse("2026-10-01T10:00:00Z"), null,
        List.of(), null, List.of()));

    mvc().perform(post("/api/v1/simulations/by-edition")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"editionYear\":2026,\"mode\":\"PROVA\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.attemptId").value(56))
        .andExpect(jsonPath("$.type").value("REAL_EDITION"))
        .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
  }

  @Test
  void createByEditionValidationFails400() throws Exception {
    authenticate();

    mvc().perform(post("/api/v1/simulations/by-edition")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"mode\":\"PROVA\"}"))
        .andExpect(status().isBadRequest());
    mvc().perform(post("/api/v1/simulations/by-edition")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"editionYear\":2026,\"mode\":\"REVISAO\"}"))
        .andExpect(status().isBadRequest());
    mvc().perform(post("/api/v1/simulations/by-edition")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"editionYear\":1999,\"mode\":\"PROVA\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getListSubmitAbandonAndResultReturn200() throws Exception {
    authenticate();
    when(service.getAttempt(1L, 55L)).thenReturn(attempt());
    when(service.listAttempts(anyLong(), anyInt(), anyInt())).thenReturn(new PageResponse<>(
        List.of(new SimulationAttemptSummary(55L, 7L, "Simulado", "MATEMATICA",
            "PROVA", "SUBMITTED", 10,
            OffsetDateTime.parse("2026-10-01T10:00:00Z"),
            OffsetDateTime.parse("2026-10-01T11:00:00Z"))),
        0, 20, 1, 1, true, true));
    when(service.submitAttempt(1L, 55L)).thenReturn(result());
    when(service.abandonAttempt(1L, 55L)).thenReturn(result());
    when(service.getResult(1L, 55L)).thenReturn(result());

    mvc().perform(get("/api/v1/simulations/attempts/55"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attemptId").value(55));
    mvc().perform(get("/api/v1/simulations/attempts"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalElements").value(1));
    mvc().perform(post("/api/v1/simulations/attempts/55/submit"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUBMITTED"))
        .andExpect(jsonPath("$.accuracy").value(0.7));
    mvc().perform(post("/api/v1/simulations/attempts/55/abandon"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUBMITTED"));
    mvc().perform(get("/api/v1/simulations/attempts/55/result"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(10));
  }

  @Test
  void feedbackReturns200() throws Exception {
    authenticate();
    when(service.getStudyFeedback(1L, 55L, 1)).thenReturn(new StudyFeedbackResponse(
        55L, 1, 21L, "MATEMATICA", "Matemática", 2026, 17,
        "C", true, false, "C",
        3L, "PORCENTAGEM", "Porcentagem", 11L, "CALCULO_PERCENTUAL", "Cálculo percentual",
        "ALTA", "v1.1", List.of("nota")));

    mvc().perform(get("/api/v1/simulations/attempts/55/feedback/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.position").value(1))
        .andExpect(jsonPath("$.isCorrect").value(true))
        .andExpect(jsonPath("$.correctAnswer").value("C"))
        .andExpect(jsonPath("$.topicCode").value("PORCENTAGEM"));
  }
}
