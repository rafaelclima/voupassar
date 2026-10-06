package br.com.voupassar.questions.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.questions.dto.DisciplineRef;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.questions.dto.QuestionOptionResponse;
import br.com.voupassar.questions.dto.QuestionResponse;
import br.com.voupassar.questions.service.QuestionService;
import br.com.voupassar.security.UserPrincipal;
import static org.hamcrest.Matchers.containsString;
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
 * Controller de questões sem subir contexto (TASK 3.4 + ocultação TASK 5.3).
 *
 * <p>Valida contrato JSON e envelope de erro. Autorização (401 sem token) é
 * coberta pelo contexto real (todas as rotas sob {@code /api/v1/**} exigem
 * autenticação).
 */
@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

  @Mock QuestionService service;

  @InjectMocks QuestionController controller;

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

  private QuestionResponse item() {
    return new QuestionResponse(
        1L,
        "OFFICIAL",
        2026,
        21,
        new DisciplineRef("MATEMATICA", "Matemática"),
        "Quanto é 2 + 2?",
        List.of(
            new QuestionOptionResponse("A", "4"),
            new QuestionOptionResponse("B", "5"),
            new QuestionOptionResponse("C", "6"),
            new QuestionOptionResponse("D", "7")),
        "A",
        false,
        "FACIL",
        10,
        10,
        false,
        null,
        null,
        null,
        null,
        List.of("Dificuldade NÃO CONFIRMADA."),
        List.of(),
        List.of());
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(get("/api/v1/questions")).andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/questions/1")).andExpect(status().isUnauthorized());
  }

  @Test
  void listReturnsPage() throws Exception {
    authenticate();
    when(service.searchForUser(eq(1L), eq("MATEMATICA"), eq(null), eq(null), eq(null), eq(null), eq(null), eq(0), eq(20)))
        .thenReturn(new PageResponse<>(List.of(item()), 0, 20, 120, 6, true, false));

    mvc().perform(get("/api/v1/questions").param("disciplineCode", "MATEMATICA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content.length()").value(1))
        .andExpect(jsonPath("$.content[0].answerKey").value("A"))
        .andExpect(jsonPath("$.content[0].options.length()").value(4))
        .andExpect(jsonPath("$.totalElements").value(120))
        .andExpect(jsonPath("$.totalPages").value(6))
        .andExpect(jsonPath("$.first").value(true));
  }

  @Test
  void listForwardsDefaults() throws Exception {
    authenticate();
    when(service.searchForUser(eq(1L), eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), eq(0), eq(20)))
        .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));

    mvc().perform(get("/api/v1/questions")).andExpect(status().isOk());

    verify(service).searchForUser(1L, null, null, null, null, null, null, 0, 20);
  }

  @Test
  void listInvalidFilterReturns400Envelope() throws Exception {
    authenticate();
    when(service.searchForUser(eq(1L), eq(null), eq(null), eq(null), eq(null), eq("HARD"), eq(null), eq(0), eq(20)))
        .thenThrow(new BadRequestException("Dificuldade inválida: HARD."));

    mvc().perform(get("/api/v1/questions").param("difficulty", "HARD"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void listUnknownEditionReturns404Envelope() throws Exception {
    authenticate();
    when(service.searchForUser(eq(1L), eq(null), eq(null), eq(null), eq(2021), eq(null), eq(null), eq(0), eq(20)))
        .thenThrow(new ResourceNotFoundException("EDITION_NOT_FOUND", "Edição 2021 não encontrada."));

    mvc().perform(get("/api/v1/questions").param("year", "2021"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EDITION_NOT_FOUND"));
  }

  @Test
  void detailReturnsQuestion() throws Exception {
    authenticate();
    when(service.getByIdForUser(1L, 1L)).thenReturn(item());

    mvc().perform(get("/api/v1/questions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.statement").value("Quanto é 2 + 2?"))
        .andExpect(jsonPath("$.options.length()").value(4))
        .andExpect(jsonPath("$.notes").isArray());
  }

  @Test
  void detailUnknownReturns404Envelope() throws Exception {
    authenticate();
    when(service.getByIdForUser(1L, 999L))
        .thenThrow(new ResourceNotFoundException("QUESTION_NOT_FOUND", "Questão 999 não encontrada."));

    mvc().perform(get("/api/v1/questions/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("QUESTION_NOT_FOUND"))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void detailHiddenDuringProvaHasNullKey() throws Exception {
    authenticate();
    QuestionResponse hidden = new QuestionResponse(
        1L, "OFFICIAL", 2026, 21,
        new DisciplineRef("MATEMATICA", "Matemática"),
        "Quanto é 2 + 2?",
        List.of(
            new QuestionOptionResponse("A", "4"),
            new QuestionOptionResponse("B", "5"),
            new QuestionOptionResponse("C", "6"),
            new QuestionOptionResponse("D", "7")),
        null, false, "FACIL", 10, 10, false,
        null, null, null, null,
        List.of("Gabarito oculto durante a execução no Modo Prova (questão em simulado PROVA em andamento): conclua ou abandone para ver a correção."),
        List.of(),
        List.of());
    when(service.getByIdForUser(1L, 1L)).thenReturn(hidden);

    mvc().perform(get("/api/v1/questions/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.answerKey").doesNotExist())
        .andExpect(jsonPath("$.notes[0]", containsString("Gabarito oculto")));
  }
}
