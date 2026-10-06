package br.com.voupassar.content.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.content.dto.ConfidenceBreakdownResponse;
import br.com.voupassar.content.dto.ContentDisciplineStatsResponse;
import br.com.voupassar.content.dto.ContentStatsResponse;
import br.com.voupassar.content.dto.ContentTopicStatsResponse;
import br.com.voupassar.content.dto.DisciplineDetailResponse;
import br.com.voupassar.content.dto.DisciplineSummaryResponse;
import br.com.voupassar.content.dto.EditionCountResponse;
import br.com.voupassar.content.dto.SubtopicDetailResponse;
import br.com.voupassar.content.dto.SubtopicSummaryResponse;
import br.com.voupassar.content.dto.TopicDetailResponse;
import br.com.voupassar.content.dto.TopicSummaryResponse;
import br.com.voupassar.content.service.ContentService;
import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Controller de conteúdos sem subir contexto (TASK 3.3).
 *
 * <p>Valida contrato JSON e envelope de erro. Autorização (401 sem token) é
 * coberta pelo contexto real (todas as rotas sob {@code /api/v1/**} exigem
 * autenticação até a TASK 3.5).
 */
@ExtendWith(MockitoExtension.class)
class ContentControllerTest {

  @Mock ContentService service;

  @InjectMocks ContentController controller;

  private MockMvc mvc() {
    return MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void listDisciplinesReturnsSummaries() throws Exception {
    when(service.listDisciplines())
        .thenReturn(
            List.of(
                new DisciplineSummaryResponse("LINGUA_PORTUGUESA", "Língua Portuguesa", 2, 120),
                new DisciplineSummaryResponse("MATEMATICA", "Matemática", 8, 120)));

    mvc().perform(get("/api/v1/disciplines"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].code").value("LINGUA_PORTUGUESA"))
        .andExpect(jsonPath("$[1].questionCount").value(120));
  }

  @Test
  void getDisciplineReturnsDetail() throws Exception {
    when(service.getDiscipline("MATEMATICA"))
        .thenReturn(
            new DisciplineDetailResponse(
                "MATEMATICA",
                "Matemática",
                1,
                120,
                List.of(
                    new TopicSummaryResponse(
                        5L, "ALGEBRA", "Álgebra", "MATEMATICA", "Matemática", true, 18, 4))));

    mvc().perform(get("/api/v1/disciplines/MATEMATICA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("MATEMATICA"))
        .andExpect(jsonPath("$.topics.length()").value(1))
        .andExpect(jsonPath("$.topics[0].code").value("ALGEBRA"));
  }

  @Test
  void getDisciplineUnknownReturns404Envelope() throws Exception {
    when(service.getDiscipline("FISICA"))
        .thenThrow(new ResourceNotFoundException("DISCIPLINE_NOT_FOUND", "Disciplina FISICA não encontrada."));

    mvc().perform(get("/api/v1/disciplines/FISICA"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("DISCIPLINE_NOT_FOUND"))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void listTopicsReturnsSummaries() throws Exception {
    when(service.listTopics(null))
        .thenReturn(
            List.of(
                new TopicSummaryResponse(
                    5L, "ALGEBRA", "Álgebra", "MATEMATICA", "Matemática", true, 18, 4)));

    mvc().perform(get("/api/v1/topics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].code").value("ALGEBRA"));
  }

  @Test
  void getTopicReturnsHistory() throws Exception {
    when(service.getTopic(5L))
        .thenReturn(
            new TopicDetailResponse(
                5L,
                "ALGEBRA",
                "Álgebra",
                "MATEMATICA",
                "Matemática",
                true,
                18,
                List.of(2020, 2026),
                List.of(new EditionCountResponse(2026, 4, 0)),
                new ConfidenceBreakdownResponse(14, 4, 0),
                List.of()));

    mvc().perform(get("/api/v1/topics/5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("ALGEBRA"))
        .andExpect(jsonPath("$.questionCount").value(18))
        .andExpect(jsonPath("$.perEdition").isArray())
        .andExpect(jsonPath("$.confidence.alta").value(14));
  }

  @Test
  void getTopicUnknownReturns404Envelope() throws Exception {
    when(service.getTopic(999L))
        .thenThrow(new ResourceNotFoundException("TOPIC_NOT_FOUND", "Assunto 999 não encontrado."));

    mvc().perform(get("/api/v1/topics/999"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("TOPIC_NOT_FOUND"));
  }

  @Test
  void listSubtopicsReturnsSummaries() throws Exception {
    when(service.listSubtopics(null))
        .thenReturn(
            List.of(
                new SubtopicSummaryResponse(
                    25L, "EQUACOES", "Equações", 5L, "ALGEBRA", "Álgebra",
                    "MATEMATICA", "Matemática", true, 8)));

    mvc().perform(get("/api/v1/subtopics"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].code").value("EQUACOES"));
  }

  @Test
  void getSubtopicReturnsHistory() throws Exception {
    when(service.getSubtopic(25L))
        .thenReturn(
            new SubtopicDetailResponse(
                25L,
                "EQUACOES",
                "Equações",
                5L,
                "ALGEBRA",
                "Álgebra",
                "MATEMATICA",
                "Matemática",
                true,
                8,
                List.of(2026),
                List.of(new EditionCountResponse(2026, 2, 0)),
                new ConfidenceBreakdownResponse(8, 0, 0)));

    mvc().perform(get("/api/v1/subtopics/25"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("EQUACOES"))
        .andExpect(jsonPath("$.questionCount").value(8));
  }

  @Test
  void contentStatsReturnsOverview() throws Exception {
    when(service.getContentStats())
        .thenReturn(
            new ContentStatsResponse(
                240L,
                240L,
                List.of("v1.1"),
                List.of(new ContentDisciplineStatsResponse("MATEMATICA", "Matemática", 8, 120)),
                List.of(
                    new ContentTopicStatsResponse(
                        5L, "ALGEBRA", "Álgebra", "MATEMATICA", "Matemática", 18, 7.5, 6, 0)),
                List.of("Anuladas contam como conteúdo que apareceu na prova.")));

    mvc().perform(get("/api/v1/content/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalQuestions").value(240))
        .andExpect(jsonPath("$.totalClassified").value(240))
        .andExpect(jsonPath("$.perTopic.length()").value(1))
        .andExpect(jsonPath("$.classificationReviewPending").doesNotExist());
  }
}
