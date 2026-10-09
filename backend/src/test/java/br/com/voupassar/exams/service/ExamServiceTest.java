package br.com.voupassar.exams.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import br.com.voupassar.exams.dto.EditionDetailResponse;
import br.com.voupassar.exams.dto.EditionStatsResponse;
import br.com.voupassar.exams.dto.EditionSummaryResponse;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Exam;
import br.com.voupassar.exams.entity.ExamDocument;
import br.com.voupassar.exams.entity.ExamEssayPrompt;
import br.com.voupassar.exams.entity.ExamVersion;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.ExamDocumentRepository;
import br.com.voupassar.exams.repository.ExamEssayPromptRepository;
import br.com.voupassar.exams.repository.ExamRepository;
import br.com.voupassar.exams.repository.ExamVersionRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Serviço de edições sem subir contexto (TASK 3.2).
 *
 * <p>Valida regras de evidência: 2021 ausente → 404 explícito; scoring NULL →
 * nota DESCONHECIDA; esperado × importado distintos.
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceTest {

  @Mock ExamRepository exams;
  @Mock ExamVersionRepository versions;
  @Mock ExamDocumentRepository documents;
  @Mock ExamEssayPromptRepository essayPrompts;
  @Mock DisciplineRepository disciplines;
  @Mock QuestionRepository questions;

  ExamService service;

  @BeforeEach
  void setup() {
    service =
        new ExamService(exams, versions, documents, essayPrompts, disciplines, questions);
  }

  private Exam exam(int year, String edital) {
    Exam e = new Exam();
    ReflectionTestUtils.setField(e, "id", (long) year);
    ReflectionTestUtils.setField(e, "institution", "IFRN");
    ReflectionTestUtils.setField(e, "year", (short) year);
    ReflectionTestUtils.setField(e, "edital", edital);
    ReflectionTestUtils.setField(e, "durationMinutes", (short) 240);
    ReflectionTestUtils.setField(e, "objectiveCount", (short) 40);
    ReflectionTestUtils.setField(e, "lpCount", (short) 20);
    ReflectionTestUtils.setField(e, "matCount", (short) 20);
    ReflectionTestUtils.setField(e, "cnCount", (short) 0);
    ReflectionTestUtils.setField(e, "chCount", (short) 0);
    ReflectionTestUtils.setField(e, "hasEssay", true);
    ReflectionTestUtils.setField(e, "scoringRule", null);
    return e;
  }

  private ExamVersion version(long id, Exam exam, String code) {
    ExamVersion v = new ExamVersion();
    ReflectionTestUtils.setField(v, "id", id);
    ReflectionTestUtils.setField(v, "exam", exam);
    ReflectionTestUtils.setField(v, "versionCode", code);
    ReflectionTestUtils.setField(v, "publishedAt", LocalDate.of(2025, 11, 4));
    ReflectionTestUtils.setField(v, "note", "nota");
    return v;
  }

  private ExamDocument document(long id, ExamVersion v, String kind, String file) {
    ExamDocument d = new ExamDocument();
    ReflectionTestUtils.setField(d, "id", id);
    ReflectionTestUtils.setField(d, "version", v);
    ReflectionTestUtils.setField(d, "kind", kind);
    ReflectionTestUtils.setField(d, "fileName", file);
    ReflectionTestUtils.setField(d, "sha256", "a".repeat(64));
    ReflectionTestUtils.setField(d, "pages", (short) 14);
    ReflectionTestUtils.setField(d, "generator", "PDF24");
    ReflectionTestUtils.setField(d, "note", null);
    return d;
  }

  private Discipline discipline(long id, String code, String name) {
    Discipline d = new Discipline(code, name);
    ReflectionTestUtils.setField(d, "id", id);
    return d;
  }

  @Test
  void listEditionsInOrderWithCounts() {
    Exam e2025 = exam(2025, "23/2024");
    Exam e2026 = exam(2026, "48/2025");
    when(exams.findAllByOrderByInstitutionAscYearAsc()).thenReturn(List.of(e2025, e2026));
    when(versions.countByExamInstitutionAndExamYear("IFRN", (short) 2025)).thenReturn(1L);
    when(versions.countByExamInstitutionAndExamYear("IFRN", (short) 2026)).thenReturn(1L);
    when(documents.countByExamInstitutionAndYear("IFRN", (short) 2025)).thenReturn(2L);
    when(documents.countByExamInstitutionAndYear("IFRN", (short) 2026)).thenReturn(2L);

    List<EditionSummaryResponse> out = service.listEditions();

    assertEquals(2, out.size());
    assertEquals(2025, out.get(0).year());
    assertEquals("IFRN", out.get(0).institution());
    assertEquals("23/2024", out.get(0).edital());
    assertEquals(40, out.get(0).objectiveCount());
    assertEquals(0, out.get(0).cnCount());
    assertEquals(0, out.get(0).chCount());
    assertNull(out.get(0).scoringRule());
    assertEquals(2L, out.get(1).documentCount());
  }

  @Test
  void getEditionGroupsDocumentsByVersion() {
    Exam e = exam(2026, "48/2025");
    ExamVersion v = version(7L, e, "DEFINITIVO");
    ExamDocument caderno = document(11L, v, "CADERNO", "data/provas/2026/Caderno.pdf");
    ExamDocument gab = document(12L, v, "GABARITO_DEFINITIVO", "data/provas/2026/GAB.pdf");
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(e));
    when(versions.findByExamInstitutionAndExamYearOrderByVersionCodeAsc("IFRN", (short) 2026)).thenReturn(List.of(v));
    when(documents.findByExamInstitutionAndYear("IFRN", (short) 2026)).thenReturn(List.of(caderno, gab));
    when(essayPrompts.findByExamInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.empty());

    EditionDetailResponse out = service.getEdition(2026);

    assertEquals(2026, out.year());
    assertEquals(1, out.versions().size());
    assertEquals(2, out.versions().get(0).documents().size());
    assertEquals("DEFINITIVO", out.versions().get(0).documents().get(0).versionCode());
    assertNull(out.essayPrompt());
  }

  @Test
  void getEdition2021IsNotFoundWithExplicitReason() {
    when(exams.findByInstitutionAndYear("IFRN", (short) 2021)).thenReturn(Optional.empty());

    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getEdition(2021));

    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("2021"));
  }

  @Test
  void getEditionUnknownYearIsNotFound() {
    when(exams.findByInstitutionAndYear("IFRN", (short) 2030)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.getEdition(2030));
  }

  @Test
  void yearOutOfRangeIsBadRequest() {
    assertThrows(BadRequestException.class, () -> service.getEdition(1999));
    assertThrows(BadRequestException.class, () -> service.listDocuments(999));
  }

  @Test
  void statsDistinguishesExpectedFromImported() {
    Exam e = exam(2026, "48/2025");
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(e));
    when(questions.countByExamInstitutionAndExamYear("IFRN", (short) 2026)).thenReturn(40L);
    when(questions.countByExamInstitutionAndExamYearAndAnnulledTrue("IFRN", (short) 2026)).thenReturn(1L);
    when(questions.countByDisciplineForInstitution("IFRN", (short) 2026))
        .thenReturn(
            List.<Object[]>of(
                new Object[] {"LINGUA_PORTUGUESA", "Língua Portuguesa", 20L, 0L},
                new Object[] {"MATEMATICA", "Matemática", 20L, 1L}));
    when(disciplines.findAllByOrderByCodeAsc())
        .thenReturn(
            List.of(
                discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa"),
                discipline(2L, "MATEMATICA", "Matemática")));
    when(documents.countByExamInstitutionAndYear("IFRN", (short) 2026)).thenReturn(2L);
    when(versions.countByExamInstitutionAndExamYear("IFRN", (short) 2026)).thenReturn(1L);

    EditionStatsResponse stats = service.getStats(2026);

    assertEquals(40L, stats.questionsImported());
    assertEquals(39L, stats.confirmed());
    assertEquals(1L, stats.annulled());
    assertEquals(2, stats.perDiscipline().size());
    assertEquals(20, stats.perDiscipline().get(1).expected());
    assertEquals(1L, stats.perDiscipline().get(1).annulled());
    assertFalse(stats.scoringRuleKnown());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("DESCONHECIDA")));
  }

  @Test
  void statsWithoutImportDoesNotInvent() {
    Exam e = exam(2026, "48/2025");
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(e));
    when(questions.countByExamInstitutionAndExamYear("IFRN", (short) 2026)).thenReturn(0L);
    when(questions.countByExamInstitutionAndExamYearAndAnnulledTrue("IFRN", (short) 2026)).thenReturn(0L);
    when(questions.countByDisciplineForInstitution("IFRN", (short) 2026)).thenReturn(List.of());
    when(disciplines.findAllByOrderByCodeAsc())
        .thenReturn(
            List.of(
                discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa"),
                discipline(2L, "MATEMATICA", "Matemática")));
    when(documents.countByExamInstitutionAndYear("IFRN", (short) 2026)).thenReturn(2L);
    when(versions.countByExamInstitutionAndExamYear("IFRN", (short) 2026)).thenReturn(1L);

    EditionStatsResponse stats = service.getStats(2026);

    assertEquals(0L, stats.questionsImported());
    assertEquals(0L, stats.confirmed());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("Nenhuma questão importada")));
  }

  // ---- TASK E.1: multi-processo (institution) ----

  private Exam eajExam(int year, int objective, int lp, int mat, int cn, int ch) {
    Exam e = new Exam();
    ReflectionTestUtils.setField(e, "id", (long) year + 1000L);
    ReflectionTestUtils.setField(e, "institution", "EAJ");
    ReflectionTestUtils.setField(e, "year", (short) year);
    ReflectionTestUtils.setField(e, "edital", "DESCONHECIDO");
    ReflectionTestUtils.setField(e, "durationMinutes", (short) 180);
    ReflectionTestUtils.setField(e, "objectiveCount", (short) objective);
    ReflectionTestUtils.setField(e, "lpCount", (short) lp);
    ReflectionTestUtils.setField(e, "matCount", (short) mat);
    ReflectionTestUtils.setField(e, "cnCount", (short) cn);
    ReflectionTestUtils.setField(e, "chCount", (short) ch);
    ReflectionTestUtils.setField(e, "hasEssay", false);
    ReflectionTestUtils.setField(e, "scoringRule", null);
    return e;
  }

  @Test
  void listEditionsWithoutFilterReturnsBothWithLabel() {
    Exam ifrn2022 = exam(2022, "41/2021");
    Exam eaj2022 = eajExam(2022, 40, 20, 20, 0, 0);
    when(exams.findAllByOrderByInstitutionAscYearAsc()).thenReturn(List.of(eaj2022, ifrn2022));
    when(versions.countByExamInstitutionAndExamYear("EAJ", (short) 2022)).thenReturn(1L);
    when(versions.countByExamInstitutionAndExamYear("IFRN", (short) 2022)).thenReturn(2L);
    when(documents.countByExamInstitutionAndYear("EAJ", (short) 2022)).thenReturn(2L);
    when(documents.countByExamInstitutionAndYear("IFRN", (short) 2022)).thenReturn(2L);

    List<EditionSummaryResponse> out = service.listEditions(null);

    assertEquals(2, out.size());
    assertEquals("EAJ", out.get(0).institution());
    assertEquals("IFRN", out.get(1).institution());
    assertEquals(2022, out.get(0).year());
    assertEquals(2022, out.get(1).year());
  }

  @Test
  void listEditionsWithInstitutionFilterReturnsOnlyThatProcess() {
    Exam eaj2021 = eajExam(2021, 50, 15, 15, 12, 8);
    when(exams.findByInstitutionOrderByYearAsc("EAJ")).thenReturn(List.of(eaj2021));
    when(versions.countByExamInstitutionAndExamYear("EAJ", (short) 2021)).thenReturn(1L);
    when(documents.countByExamInstitutionAndYear("EAJ", (short) 2021)).thenReturn(2L);

    List<EditionSummaryResponse> out = service.listEditions("eaj");

    assertEquals(1, out.size());
    assertEquals("EAJ", out.get(0).institution());
    assertEquals(2021, out.get(0).year());
    assertEquals(50, out.get(0).objectiveCount());
    assertEquals(12, out.get(0).cnCount());
    assertEquals(8, out.get(0).chCount());
  }

  @Test
  void listEditionsInvalidInstitutionIs400() {
    assertThrows(BadRequestException.class, () -> service.listEditions("XXX"));
  }

  @Test
  void eaj2022DiffersFromIfrn2022() {
    Exam ifrn2022 = exam(2022, "41/2021");
    Exam eaj2022 = eajExam(2022, 40, 20, 20, 0, 0);
    ExamVersion vIfrn = version(1L, ifrn2022, "DEFINITIVO");
    ExamVersion vEaj = version(2L, eaj2022, "UNICA");
    when(exams.findByInstitutionAndYear("IFRN", (short) 2022)).thenReturn(Optional.of(ifrn2022));
    when(exams.findByInstitutionAndYear("EAJ", (short) 2022)).thenReturn(Optional.of(eaj2022));
    when(versions.findByExamInstitutionAndExamYearOrderByVersionCodeAsc("IFRN", (short) 2022))
        .thenReturn(List.of(vIfrn));
    when(versions.findByExamInstitutionAndExamYearOrderByVersionCodeAsc("EAJ", (short) 2022))
        .thenReturn(List.of(vEaj));
    when(documents.findByExamInstitutionAndYear("IFRN", (short) 2022)).thenReturn(List.of());
    when(documents.findByExamInstitutionAndYear("EAJ", (short) 2022)).thenReturn(List.of());
    when(essayPrompts.findByExamInstitutionAndYear("IFRN", (short) 2022)).thenReturn(Optional.empty());
    when(essayPrompts.findByExamInstitutionAndYear("EAJ", (short) 2022)).thenReturn(Optional.empty());

    EditionDetailResponse ifrn = service.getEdition(2022, "IFRN");
    EditionDetailResponse eaj = service.getEdition(2022, "EAJ");

    assertEquals("IFRN", ifrn.institution());
    assertEquals("EAJ", eaj.institution());
    assertEquals("41/2021", ifrn.edital());
    assertEquals("DESCONHECIDO", eaj.edital());
    assertEquals(240, ifrn.durationMinutes());
    assertEquals(180, eaj.durationMinutes());
    assertEquals("DEFINITIVO", ifrn.versions().get(0).versionCode());
    assertEquals("UNICA", eaj.versions().get(0).versionCode());
  }

  @Test
  void eaj2021Returns50In4Areas() {
    Exam eaj2021 = eajExam(2021, 50, 15, 15, 12, 8);
    when(exams.findByInstitutionAndYear("EAJ", (short) 2021)).thenReturn(Optional.of(eaj2021));
    when(questions.countByExamInstitutionAndExamYear("EAJ", (short) 2021)).thenReturn(50L);
    when(questions.countByExamInstitutionAndExamYearAndAnnulledTrue("EAJ", (short) 2021)).thenReturn(1L);
    when(questions.countByDisciplineForInstitution("EAJ", (short) 2021))
        .thenReturn(
            List.<Object[]>of(
                new Object[] {"LINGUA_PORTUGUESA", "Língua Portuguesa", 15L, 0L},
                new Object[] {"MATEMATICA", "Matemática", 15L, 0L},
                new Object[] {"CIENCIAS_NATUREZA", "Ciências da Natureza", 12L, 1L},
                new Object[] {"CIENCIAS_HUMANAS", "Ciências Humanas", 8L, 0L}));
    when(disciplines.findAllByOrderByCodeAsc())
        .thenReturn(
            List.of(
                discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa"),
                discipline(2L, "MATEMATICA", "Matemática"),
                discipline(3L, "CIENCIAS_NATUREZA", "Ciências da Natureza"),
                discipline(4L, "CIENCIAS_HUMANAS", "Ciências Humanas")));
    when(documents.countByExamInstitutionAndYear("EAJ", (short) 2021)).thenReturn(2L);
    when(versions.countByExamInstitutionAndExamYear("EAJ", (short) 2021)).thenReturn(1L);

    EditionStatsResponse stats = service.getStats(2021, "EAJ");

    assertEquals("EAJ", stats.institution());
    assertEquals(50, stats.objectiveExpected());
    assertEquals(50L, stats.questionsImported());
    assertEquals(4, stats.perDiscipline().size());
  }

  @Test
  void eaj2023IsNotFoundWithHonestMessage() {
    when(exams.findByInstitutionAndYear("EAJ", (short) 2023)).thenReturn(Optional.empty());

    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getEdition(2023, "EAJ"));

    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("EAJ 2023"));
  }

  @Test
  void ifrn2021IsNotFoundPointingToEaj() {
    when(exams.findByInstitutionAndYear("IFRN", (short) 2021)).thenReturn(Optional.empty());

    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getEdition(2021, "IFRN"));

    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("IFRN 2021"));
    assertTrue(ex.getMessage().contains("EAJ-2021"));
  }
}
