package br.com.voupassar.exams.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.exams.dto.DisciplineStatResponse;
import br.com.voupassar.exams.dto.EditionDetailResponse;
import br.com.voupassar.exams.dto.EditionStatsResponse;
import br.com.voupassar.exams.dto.EditionSummaryResponse;
import br.com.voupassar.exams.dto.ExamDocumentResponse;
import br.com.voupassar.exams.service.ExamService;
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
 * Controller de edições sem subir contexto (TASK 3.2).
 *
 * <p>Valida contrato JSON e envelope de erro. Autorização (401 sem token) é
 * coberta por {@code HealthControllerTest#protectedRouteWithoutAuthIsNotPublic},
 * que atinge {@code GET /api/v1/editions} no contexto real.
 */
@ExtendWith(MockitoExtension.class)
class ExamControllerTest {

  @Mock ExamService service;

  @InjectMocks ExamController controller;

  private MockMvc mvc() {
    return MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void listReturnsSummaries() throws Exception {
    when(service.listEditions(null))
        .thenReturn(
            List.of(
                new EditionSummaryResponse(
                    2025, "IFRN", "23/2024", 240, 40, 20, 20, 0, 0, true, null, 1, 2),
                new EditionSummaryResponse(
                    2026, "IFRN", "48/2025", 240, 40, 20, 20, 0, 0, true, null, 1, 2)));

    mvc().perform(get("/api/v1/editions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].year").value(2025))
        .andExpect(jsonPath("$[0].edital").value("23/2024"))
        .andExpect(jsonPath("$[0].objectiveCount").value(40))
        .andExpect(jsonPath("$[1].year").value(2026));
  }

  @Test
  void listWithInstitutionFilterForwards() throws Exception {
    when(service.listEditions("EAJ"))
        .thenReturn(
            List.of(
                new EditionSummaryResponse(
                    2021, "EAJ", "DESCONHECIDO", 180, 50, 15, 15, 12, 8, false, null, 1, 2)));

    mvc().perform(get("/api/v1/editions").param("institution", "EAJ"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].institution").value("EAJ"))
        .andExpect(jsonPath("$[0].year").value(2021))
        .andExpect(jsonPath("$[0].objectiveCount").value(50));
  }

  @Test
  void getReturnsDetail() throws Exception {
    when(service.getEdition(2026, null))
        .thenReturn(
            new EditionDetailResponse(
                2026, "IFRN", "48/2025", 240, 40, 20, 20, 0, 0, true, null, List.of(), null));

    mvc().perform(get("/api/v1/editions/2026"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.edital").value("48/2025"))
        .andExpect(jsonPath("$.versions").isArray());
  }

  @Test
  void get2021Returns404EnvelopeWithoutLeak() throws Exception {
    when(service.getEdition(2021, null))
        .thenThrow(
            new ResourceNotFoundException(
                "EDITION_NOT_FOUND", "Edição 2021 não encontrada: ausente do dataset inicial."));

    mvc().perform(get("/api/v1/editions/2021"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("EDITION_NOT_FOUND"))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void documentsReturnMetadataOnly() throws Exception {
    when(service.listDocuments(2026, null))
        .thenReturn(
            List.of(
                new ExamDocumentResponse(
                    11L,
                    "CADERNO",
                    "data/provas/2026/Caderno_de_Provas_-Técnico_Integrado_2026.pdf",
                    "a".repeat(64),
                    14,
                    "PDF24",
                    null,
                    "DEFINITIVO")));

    mvc().perform(get("/api/v1/editions/2026/documents"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].kind").value("CADERNO"))
        .andExpect(jsonPath("$[0].sha256").exists())
        .andExpect(jsonPath("$[0].statement").doesNotExist());
  }

  @Test
  void statsReturnsExpectedVsImported() throws Exception {
    when(service.getStats(2026, null))
        .thenReturn(
            new EditionStatsResponse(
                2026,
                "IFRN",
                "48/2025",
                40,
                40L,
                39L,
                1L,
                List.of(
                    new DisciplineStatResponse("LINGUA_PORTUGUESA", "Língua Portuguesa", 20, 20L, 0L),
                    new DisciplineStatResponse("MATEMATICA", "Matemática", 20, 20L, 1L)),
                2L,
                1L,
                true,
                false,
                List.of("scoring_rule DESCONHECIDA.")));

    mvc().perform(get("/api/v1/editions/2026/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.year").value(2026))
        .andExpect(jsonPath("$.questionsImported").value(40))
        .andExpect(jsonPath("$.annulled").value(1))
        .andExpect(jsonPath("$.perDiscipline.length()").value(2))
        .andExpect(jsonPath("$.scoringRuleKnown").value(false));
  }

  @Test
  void getWithInstitutionEajForwards() throws Exception {
    when(service.getEdition(2022, "EAJ"))
        .thenReturn(
            new EditionDetailResponse(
                2022, "EAJ", "DESCONHECIDO", 180, 40, 20, 20, 0, 0, false, null, List.of(), null));

    mvc().perform(get("/api/v1/editions/2022").param("institution", "EAJ"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.institution").value("EAJ"))
        .andExpect(jsonPath("$.year").value(2022))
        .andExpect(jsonPath("$.durationMinutes").value(180));
  }
}
