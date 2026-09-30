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
    ReflectionTestUtils.setField(e, "year", (short) year);
    ReflectionTestUtils.setField(e, "edital", edital);
    ReflectionTestUtils.setField(e, "durationMinutes", (short) 240);
    ReflectionTestUtils.setField(e, "objectiveCount", (short) 40);
    ReflectionTestUtils.setField(e, "lpCount", (short) 20);
    ReflectionTestUtils.setField(e, "matCount", (short) 20);
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
    when(exams.findAllByOrderByYearAsc()).thenReturn(List.of(e2025, e2026));
    when(versions.countByExamYear((short) 2025)).thenReturn(1L);
    when(versions.countByExamYear((short) 2026)).thenReturn(1L);
    when(documents.countByExamYear((short) 2025)).thenReturn(2L);
    when(documents.countByExamYear((short) 2026)).thenReturn(2L);

    List<EditionSummaryResponse> out = service.listEditions();

    assertEquals(2, out.size());
    assertEquals(2025, out.get(0).year());
    assertEquals("23/2024", out.get(0).edital());
    assertEquals(40, out.get(0).objectiveCount());
    assertNull(out.get(0).scoringRule());
    assertEquals(2L, out.get(1).documentCount());
  }

  @Test
  void getEditionGroupsDocumentsByVersion() {
    Exam e = exam(2026, "48/2025");
    ExamVersion v = version(7L, e, "DEFINITIVO");
    ExamDocument caderno = document(11L, v, "CADERNO", "data/provas/2026/Caderno.pdf");
    ExamDocument gab = document(12L, v, "GABARITO_DEFINITIVO", "data/provas/2026/GAB.pdf");
    when(exams.findByYear((short) 2026)).thenReturn(Optional.of(e));
    when(versions.findByExamYearOrderByVersionCodeAsc((short) 2026)).thenReturn(List.of(v));
    when(documents.findByExamYear((short) 2026)).thenReturn(List.of(caderno, gab));
    when(essayPrompts.findByExamYear((short) 2026)).thenReturn(Optional.empty());

    EditionDetailResponse out = service.getEdition(2026);

    assertEquals(2026, out.year());
    assertEquals(1, out.versions().size());
    assertEquals(2, out.versions().get(0).documents().size());
    assertEquals("DEFINITIVO", out.versions().get(0).documents().get(0).versionCode());
    assertNull(out.essayPrompt());
  }

  @Test
  void getEdition2021IsNotFoundWithExplicitReason() {
    when(exams.findByYear((short) 2021)).thenReturn(Optional.empty());

    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getEdition(2021));

    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("2021"));
  }

  @Test
  void getEditionUnknownYearIsNotFound() {
    when(exams.findByYear((short) 2030)).thenReturn(Optional.empty());

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
    when(exams.findByYear((short) 2026)).thenReturn(Optional.of(e));
    when(questions.countByExamYear((short) 2026)).thenReturn(40L);
    when(questions.countByExamYearAndAnnulledTrue((short) 2026)).thenReturn(1L);
    when(questions.countByDiscipline((short) 2026))
        .thenReturn(
            List.<Object[]>of(
                new Object[] {"LINGUA_PORTUGUESA", "Língua Portuguesa", 20L, 0L},
                new Object[] {"MATEMATICA", "Matemática", 20L, 1L}));
    when(disciplines.findAllByOrderByCodeAsc())
        .thenReturn(
            List.of(
                discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa"),
                discipline(2L, "MATEMATICA", "Matemática")));
    when(documents.countByExamYear((short) 2026)).thenReturn(2L);
    when(versions.countByExamYear((short) 2026)).thenReturn(1L);

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
    when(exams.findByYear((short) 2026)).thenReturn(Optional.of(e));
    when(questions.countByExamYear((short) 2026)).thenReturn(0L);
    when(questions.countByExamYearAndAnnulledTrue((short) 2026)).thenReturn(0L);
    when(questions.countByDiscipline((short) 2026)).thenReturn(List.of());
    when(disciplines.findAllByOrderByCodeAsc())
        .thenReturn(
            List.of(
                discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa"),
                discipline(2L, "MATEMATICA", "Matemática")));
    when(documents.countByExamYear((short) 2026)).thenReturn(2L);
    when(versions.countByExamYear((short) 2026)).thenReturn(1L);

    EditionStatsResponse stats = service.getStats(2026);

    assertEquals(0L, stats.questionsImported());
    assertEquals(0L, stats.confirmed());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("Nenhuma questão importada")));
  }
}
