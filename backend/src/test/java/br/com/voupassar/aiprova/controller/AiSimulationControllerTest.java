package br.com.voupassar.aiprova.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.aiprova.AiSimulationService;
import br.com.voupassar.aiprova.dto.AiAnswerResponse;
import br.com.voupassar.aiprova.dto.AiAttemptResponse;
import br.com.voupassar.aiprova.dto.AiAttemptResponse.AiCadernoItem;
import br.com.voupassar.aiprova.dto.AiFeedbackResponse;
import br.com.voupassar.aiprova.dto.AiResultResponse;
import br.com.voupassar.aiprova.AiGenerationInvalidException;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.security.UserPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
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
 * Contrato JSON da prova-IA sem subir contexto (TASK A.3).
 *
 * <p>Rotas, status (201 na criação), selo experimental no corpo e envelopes
 * de erro honestos (401 sem token; 400/404/409/422 do serviço).
 */
@ExtendWith(MockitoExtension.class)
class AiSimulationControllerTest {

  @Mock AiSimulationService service;

  @InjectMocks AiSimulationController controller;

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

  private static AiAttemptResponse attempt() {
    return new AiAttemptResponse(55L, 7L, 3L, "AI_GENERATED_EDITION",
        "Prova-IA IFRN-2026 (experimental) [PROVA]", "IFRN", "IFRN-2026",
        "PROVA", "IN_PROGRESS", 40, OffsetDateTime.parse("2026-10-01T10:00:00Z"), null,
        List.of(new AiCadernoItem(1, "MATEMATICA", "ALGEBRA", "Enunciado 1?",
            Map.of("A", "a", "B", "b", "C", "c", "D", "d"), false, null, null, null,
            true, "Questão gerada por IA — não é questão oficial IFRN/EAJ.")),
        null, List.of("Placar sobre gabarito gerado por IA — sem validade de prova real."));
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(post("/api/v1/simulations/ai-generated")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"institution\":\"IFRN\",\"mode\":\"PROVA\"}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/ai/attempts/55")).andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/simulations/ai/attempts/55/answers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"position\":1,\"selectedOption\":\"A\"}"))
        .andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/simulations/ai/attempts/55/submit"))
        .andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/simulations/ai/attempts/55/abandon"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/ai/attempts/55/result"))
        .andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/simulations/ai/attempts/55/feedback/1"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void createReturns201WithSeal() throws Exception {
    authenticate();
    when(service.createAiSimulation(anyLong(), any())).thenReturn(attempt());

    mvc().perform(post("/api/v1/simulations/ai-generated")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"institution\":\"IFRN\",\"mode\":\"PROVA\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.attemptId").value(55))
        .andExpect(jsonPath("$.type").value("AI_GENERATED_EDITION"))
        .andExpect(jsonPath("$.questions[0].aiGenerated").value(true))
        .andExpect(jsonPath("$.questions[0].aiNote").value("Questão gerada por IA — não é questão oficial IFRN/EAJ."))
        .andExpect(jsonPath("$.questions[0].stem").value("Enunciado 1?"))
        .andExpect(jsonPath("$.questions[0].options.D").value("d"));
  }

  @Test
  void answerRoundTrip() throws Exception {
    authenticate();
    when(service.answerPosition(anyLong(), anyLong(), any())).thenReturn(
        new AiAnswerResponse(9L, 55L, 1, "B", true, "B", true, List.of("nota")));

    mvc().perform(post("/api/v1/simulations/ai/attempts/55/answers")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"position\":1,\"selectedOption\":\"B\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.isCorrect").value(true))
        .andExpect(jsonPath("$.aiGenerated").value(true));
  }

  @Test
  void errorEnvelopes() throws Exception {
    authenticate();
    when(service.createAiSimulation(anyLong(), any()))
        .thenThrow(new BadRequestException("INVALID_INSTITUTION", "Processo seletivo inválido."));
    mvc().perform(post("/api/v1/simulations/ai-generated")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"institution\":\"XXX\",\"mode\":\"PROVA\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INSTITUTION"))
        .andExpect(jsonPath("$.traceId").exists());

    when(service.getAiAttempt(anyLong(), anyLong()))
        .thenThrow(new ResourceNotFoundException("SIMULATION_ATTEMPT_NOT_FOUND", "Não encontrada."));
    mvc().perform(get("/api/v1/simulations/ai/attempts/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("SIMULATION_ATTEMPT_NOT_FOUND"));

    when(service.getAiResult(anyLong(), anyLong()))
        .thenThrow(new ConflictException("SIMULATION_NOT_FINISHED", "Em andamento."));
    mvc().perform(get("/api/v1/simulations/ai/attempts/55/result"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("SIMULATION_NOT_FINISHED"));

    when(service.getAiFeedback(anyLong(), anyLong(), anyInt()))
        .thenReturn(new AiFeedbackResponse(55L, 1, "MATEMATICA", "ALGEBRA",
            "A", false, "B", true, List.of()));
    mvc().perform(get("/api/v1/simulations/ai/attempts/55/feedback/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.aiGenerated").value(true));
  }

  @Test
  void invalidBatchIs422() throws Exception {
    authenticate();
    when(service.createAiSimulation(anyLong(), any()))
        .thenThrow(new AiGenerationInvalidException("Lote fora do contrato.", List.of("q1: motivo")));
    mvc().perform(post("/api/v1/simulations/ai-generated")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"institution\":\"IFRN\",\"mode\":\"PROVA\"}"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.code").value("AI_GENERATION_INVALID"))
        .andExpect(jsonPath("$.details[0]").value("q1: motivo"));
  }
}
