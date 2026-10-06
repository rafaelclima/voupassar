package br.com.voupassar.questions.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.SubtopicRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Exam;
import br.com.voupassar.exams.entity.Passage;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.entity.QuestionPassage;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.ExamRepository;
import br.com.voupassar.exams.repository.QuestionPassageRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.questions.dto.QuestionResponse;
import br.com.voupassar.questions.entity.QuestionOption;
import br.com.voupassar.questions.repository.QuestionOptionRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Serviço de questões sem subir contexto (TASK 3.4).
 *
 * <p>Valida regras de evidência: filtros para referências inexistentes → 404
 * com código explícito (nunca vazio silencioso); 2021 registra ausência do
 * dataset; anuladas saem com nota (sem pontuar); dificuldade/
 * revisão pendente são sinalizadas em {@code notes}.
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

  @Mock QuestionRepository questions;
  @Mock QuestionOptionRepository options;
  @Mock QuestionClassificationRepository classifications;
  @Mock DisciplineRepository disciplines;
  @Mock TopicRepository topics;
  @Mock SubtopicRepository subtopics;
  @Mock ExamRepository exams;
  @Mock br.com.voupassar.simulations.repository.SimulationQuestionRepository caderno;
  @Mock QuestionPassageRepository questionPassages;

  QuestionService service;

  Discipline mat;
  Topic algebra;
  Subtopic equacoes;

  @BeforeEach
  void setup() {
    service =
        new QuestionService(
            questions, options, classifications, disciplines, topics, subtopics, exams);
    mat = new Discipline("MATEMATICA", "Matemática");
    ReflectionTestUtils.setField(mat, "id", 2L);
    algebra = new Topic();
    ReflectionTestUtils.setField(algebra, "id", 5L);
    ReflectionTestUtils.setField(algebra, "discipline", mat);
    ReflectionTestUtils.setField(algebra, "code", "ALGEBRA");
    ReflectionTestUtils.setField(algebra, "name", "Álgebra");
    ReflectionTestUtils.setField(algebra, "active", true);
    equacoes = new Subtopic();
    ReflectionTestUtils.setField(equacoes, "id", 25L);
    ReflectionTestUtils.setField(equacoes, "topic", algebra);
    ReflectionTestUtils.setField(equacoes, "code", "EQUACOES");
    ReflectionTestUtils.setField(equacoes, "name", "Equações");
    ReflectionTestUtils.setField(equacoes, "active", true);
  }

  private Question question(long id, int number, String answerKey, boolean annulled) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "sourceType", "OFFICIAL");
    ReflectionTestUtils.setField(q, "sourceYear", (short) 2026);
    ReflectionTestUtils.setField(q, "sourceQuestionNumber", (short) number);
    ReflectionTestUtils.setField(q, "statement", "Quanto é 2 + 2?");
    ReflectionTestUtils.setField(q, "kind", "OBJECTIVE");
    ReflectionTestUtils.setField(q, "discipline", mat);
    ReflectionTestUtils.setField(q, "pageStart", (short) 10);
    ReflectionTestUtils.setField(q, "pageEnd", (short) 10);
    ReflectionTestUtils.setField(q, "annulled", annulled);
    ReflectionTestUtils.setField(q, "answerKey", answerKey);
    ReflectionTestUtils.setField(q, "hasFigure", false);
    ReflectionTestUtils.setField(q, "difficultyEstimate", "FACIL");
    return q;
  }

  private QuestionOption option(Question q, String label, String text) {
    QuestionOption o = new QuestionOption();
    ReflectionTestUtils.setField(o, "id", (long) label.charAt(0));
    ReflectionTestUtils.setField(o, "question", q);
    ReflectionTestUtils.setField(o, "label", label);
    ReflectionTestUtils.setField(o, "optionText", text);
    return o;
  }

  private QuestionClassification classification(Question q) {
    QuestionClassification c = new QuestionClassification();
    ReflectionTestUtils.setField(c, "id", 7L);
    ReflectionTestUtils.setField(c, "question", q);
    ReflectionTestUtils.setField(c, "taxonomyVersion", "v1.1");
    ReflectionTestUtils.setField(c, "topic", algebra);
    ReflectionTestUtils.setField(c, "subtopic", equacoes);
    ReflectionTestUtils.setField(c, "confidence", "ALTA");
    return c;
  }

  @Test
  void searchWithoutFiltersAssemblesPage() {
    Question q = question(1L, 21, "A", false);
    when(questions.search(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any()))
        .thenReturn(new PageImpl<>(List.of(q), PageRequest.of(0, 20), 240));
    when(options.findByQuestionIdsOrdered(List.of(1L)))
        .thenReturn(
            List.of(
                option(q, "A", "4"),
                option(q, "B", "5"),
                option(q, "C", "6"),
                option(q, "D", "7")));
    when(classifications.findActiveByQuestionIds(List.of(1L)))
        .thenReturn(List.of(classification(q)));

    PageResponse<QuestionResponse> out =
        service.search(null, null, null, null, null, null, 0, 20);

    assertEquals(240, out.totalElements());
    assertEquals(12, out.totalPages());
    assertTrue(out.first());
    assertFalse(out.last());
    QuestionResponse item = out.content().get(0);
    assertEquals(1L, item.id());
    assertEquals("OFFICIAL", item.sourceType());
    assertEquals(2026, item.examYear());
    assertEquals(21, item.questionNumber());
    assertEquals("MATEMATICA", item.discipline().code());
    assertEquals(4, item.options().size());
    assertEquals("A", item.options().get(0).label());
    assertEquals("A", item.answerKey());
    assertEquals("ALGEBRA", item.topic().code());
    assertEquals("EQUACOES", item.subtopic().code());
    assertEquals("ALTA", item.classificationConfidence());
    assertTrue(item.notes().stream().anyMatch(n -> n.contains("Dificuldade FACIL estimada")));
  }

  @Test
  void searchNormalizesFilters() {
    Discipline lp = new Discipline("LINGUA_PORTUGUESA", "Língua Portuguesa");
    when(disciplines.findByCode("LINGUA_PORTUGUESA")).thenReturn(Optional.of(lp));
    when(topics.findByIdWithDiscipline(5L)).thenReturn(Optional.of(algebra));
    when(subtopics.findByIdWithTopic(25L)).thenReturn(Optional.of(equacoes));
    Exam exam2026 = new Exam();
    when(exams.findByYear((short) 2026)).thenReturn(Optional.of(exam2026));
    when(questions.search(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

    service.search("lingua_portuguesa", 5L, 25L, 2026, "media", "official", 0, 20);

    verify(questions)
        .search(
            eq("LINGUA_PORTUGUESA"),
            eq(5L),
            eq(25L),
            eq((short) 2026),
            eq("MEDIA"),
            eq("OFFICIAL"),
            eq(PageRequest.of(0, 20)));
  }

  @Test
  void searchInvalidPaginationIs400() {
    assertThrows(
        BadRequestException.class, () -> service.search(null, null, null, null, null, null, -1, 20));
    assertThrows(
        BadRequestException.class, () -> service.search(null, null, null, null, null, null, 0, 0));
    assertThrows(
        BadRequestException.class, () -> service.search(null, null, null, null, null, null, 0, 101));
  }

  @Test
  void searchInvalidEnumsAre400() {
    assertThrows(
        BadRequestException.class, () -> service.search(null, null, null, null, "HARD", null, 0, 20));
    assertThrows(
        BadRequestException.class, () -> service.search(null, null, null, null, null, "IFRN", 0, 20));
    assertThrows(
        BadRequestException.class, () -> service.search("mat-1", null, null, null, null, null, 0, 20));
    assertThrows(
        BadRequestException.class, () -> service.search(null, 0L, null, null, null, null, 0, 20));
    assertThrows(
        BadRequestException.class, () -> service.search(null, null, -3L, null, null, null, 0, 20));
    assertThrows(
        BadRequestException.class,
        () -> service.search(null, null, null, 1999, null, null, 0, 20));
  }

  @Test
  void searchUnknownReferencesAre404() {
    when(disciplines.findByCode("FISICA")).thenReturn(Optional.empty());
    ResourceNotFoundException ex =
        assertThrows(
            ResourceNotFoundException.class,
            () -> service.search("FISICA", null, null, null, null, null, 0, 20));
    assertEquals("DISCIPLINE_NOT_FOUND", ex.getCode());

    when(topics.findByIdWithDiscipline(999L)).thenReturn(Optional.empty());
    ex =
        assertThrows(
            ResourceNotFoundException.class,
            () -> service.search(null, 999L, null, null, null, null, 0, 20));
    assertEquals("TOPIC_NOT_FOUND", ex.getCode());

    when(subtopics.findByIdWithTopic(999L)).thenReturn(Optional.empty());
    ex =
        assertThrows(
            ResourceNotFoundException.class,
            () -> service.search(null, null, 999L, null, null, null, 0, 20));
    assertEquals("SUBTOPIC_NOT_FOUND", ex.getCode());

    when(exams.findByYear((short) 2030)).thenReturn(Optional.empty());
    ex =
        assertThrows(
            ResourceNotFoundException.class,
            () -> service.search(null, null, null, 2030, null, null, 0, 20));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
  }

  @Test
  void searchYear2021ReportsMissingDataset() {
    when(exams.findByYear((short) 2021)).thenReturn(Optional.empty());

    ResourceNotFoundException ex =
        assertThrows(
            ResourceNotFoundException.class,
            () -> service.search(null, null, null, 2021, null, null, 0, 20));

    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("2021"));
  }

  @Test
  void searchEmptyPageSkipsBatchLoads() {
    when(questions.search(any(), any(), any(), any(), any(), any(), any()))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

    PageResponse<QuestionResponse> out =
        service.search(null, null, null, null, null, null, 0, 20);

    assertEquals(0, out.totalElements());
    assertTrue(out.content().isEmpty());
    verify(options, never()).findByQuestionIdsOrdered(anyList());
    verify(classifications, never()).findActiveByQuestionIds(anyList());
  }

  @Test
  void getByIdReturnsDetail() {
    Question q = question(1L, 21, "A", false);
    when(questions.findById(1L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(1L))
        .thenReturn(
            List.of(
                option(q, "A", "4"),
                option(q, "B", "5"),
                option(q, "C", "6"),
                option(q, "D", "7")));
    when(classifications.findActiveByQuestionId(1L)).thenReturn(List.of(classification(q)));

    QuestionResponse out = service.getById(1L);

    assertEquals(1L, out.id());
    assertEquals("Quanto é 2 + 2?", out.statement());
    assertEquals(4, out.options().size());
    assertEquals("ALTA", out.classificationConfidence());
    assertNotNull(out.topic());
  }

  @Test
  void getByIdAnnulledHasNotes() {
    Question q = question(9L, 37, "X", true);
    when(questions.findById(9L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(9L))
        .thenReturn(
            List.of(
                option(q, "A", "4"),
                option(q, "B", "5"),
                option(q, "C", "6"),
                option(q, "D", "7")));
    when(classifications.findActiveByQuestionId(9L)).thenReturn(List.of(classification(q)));

    QuestionResponse out = service.getById(9L);

    assertTrue(out.annulled());
    assertEquals("X", out.answerKey());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("Anulada")));
  }

  @Test
  void getByIdUnclassifiedMarksTopicUnknown() {
    Question q = question(2L, 22, "B", false);
    when(questions.findById(2L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(2L))
        .thenReturn(
            List.of(
                option(q, "A", "4"),
                option(q, "B", "5"),
                option(q, "C", "6"),
                option(q, "D", "7")));
    when(classifications.findActiveByQuestionId(2L)).thenReturn(List.of());

    QuestionResponse out = service.getById(2L);

    assertNull(out.topic());
    assertNull(out.subtopic());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("NÃO CONFIRMADO")));
  }

  @Test
  void getByIdUnknownIs404AndInvalidIdIs400() {
    when(questions.findById(999L)).thenReturn(Optional.empty());
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getById(999L));
    assertEquals("QUESTION_NOT_FOUND", ex.getCode());
    assertThrows(BadRequestException.class, () -> service.getById(0L));
  }

  // ---- Modo Prova (TASK 5.3) ----

  private QuestionService serviceWithCaderno() {
    return new QuestionService(
        questions, options, classifications, disciplines, topics, subtopics, exams, caderno);
  }

  @Test
  void getByIdForUserHidesKeyWhenInProvaInProgress() {
    Question q = question(1L, 21, "A", false);
    when(questions.findById(1L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(1L))
        .thenReturn(
            List.of(
                option(q, "A", "4"),
                option(q, "B", "5"),
                option(q, "C", "6"),
                option(q, "D", "7")));
    when(classifications.findActiveByQuestionId(1L)).thenReturn(List.of(classification(q)));
    when(caderno.existsInProvaInProgress(1L, 1L)).thenReturn(true);

    QuestionResponse out = serviceWithCaderno().getByIdForUser(1L, 1L);

    assertNull(out.answerKey());
    assertEquals("Quanto é 2 + 2?", out.statement());
    assertEquals(4, out.options().size());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("Gabarito oculto")));
  }

  @Test
  void getByIdForUserRevealsWhenNotInProva() {
    Question q = question(1L, 21, "A", false);
    when(questions.findById(1L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(1L))
        .thenReturn(
            List.of(
                option(q, "A", "4"),
                option(q, "B", "5"),
                option(q, "C", "6"),
                option(q, "D", "7")));
    when(classifications.findActiveByQuestionId(1L)).thenReturn(List.of(classification(q)));
    when(caderno.existsInProvaInProgress(1L, 1L)).thenReturn(false);

    QuestionResponse out = serviceWithCaderno().getByIdForUser(1L, 1L);

    assertEquals("A", out.answerKey());
  }

  @Test
  void searchForUserMasksOnlyProvaQuestions() {
    Question q1 = question(1L, 21, "A", false);
    Question q2 = question(2L, 22, "B", false);
    when(questions.search(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any()))
        .thenReturn(new PageImpl<>(List.of(q1, q2), PageRequest.of(0, 20), 2));
    when(options.findByQuestionIdsOrdered(List.of(1L, 2L)))
        .thenReturn(
            List.of(
                option(q1, "A", "4"), option(q1, "B", "5"), option(q1, "C", "6"), option(q1, "D", "7"),
                option(q2, "A", "4"), option(q2, "B", "5"), option(q2, "C", "6"), option(q2, "D", "7")));
    when(classifications.findActiveByQuestionIds(List.of(1L, 2L)))
        .thenReturn(List.of(classification(q1), classification(q2)));
    when(caderno.findInProvaInProgress(1L, List.of(1L, 2L))).thenReturn(java.util.Set.of(2L));

    var out = serviceWithCaderno().searchForUser(1L, null, null, null, null, null, null, 0, 20);

    assertEquals(2, out.content().size());
    assertEquals("A", out.content().get(0).answerKey());
    assertNull(out.content().get(1).answerKey());
    assertTrue(out.content().get(1).notes().stream().anyMatch(n -> n.contains("Gabarito oculto")));
  }

  // ---- Textos-base (TASK 6.9) ----

  private QuestionService serviceWithPassages() {
    return new QuestionService(
        questions, options, classifications, disciplines, topics, subtopics, exams,
        caderno, questionPassages);
  }

  private QuestionPassage passageLink(long questionId, String label, String kind, String content) {
    Passage p = new Passage();
    ReflectionTestUtils.setField(p, "id", 10L);
    ReflectionTestUtils.setField(p, "label", label);
    ReflectionTestUtils.setField(p, "kind", kind);
    ReflectionTestUtils.setField(p, "content", content);
    ReflectionTestUtils.setField(p, "pageStart", (short) 5);
    ReflectionTestUtils.setField(p, "pageEnd", (short) 5);
    QuestionPassage qp = new QuestionPassage();
    ReflectionTestUtils.setField(qp, "id", 20L);
    ReflectionTestUtils.setField(qp, "questionId", questionId);
    ReflectionTestUtils.setField(qp, "passage", p);
    ReflectionTestUtils.setField(qp, "position", (short) 1);
    return qp;
  }

  @Test
  void getByIdIncludesPassages() {
    Question q = question(1L, 11, "A", false);
    when(questions.findById(1L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(1L)).thenReturn(List.of(option(q, "A", "4")));
    when(classifications.findActiveByQuestionId(1L)).thenReturn(List.of(classification(q)));
    when(questionPassages.findByQuestionIdsOrdered(List.of(1L)))
        .thenReturn(List.of(passageLink(1L, "Trecho — questões 11 a 15", "TRECHO", "Segundo [1] especialistas...")));

    QuestionResponse out = serviceWithPassages().getById(1L);

    assertEquals(1, out.passages().size());
    assertEquals("Trecho — questões 11 a 15", out.passages().get(0).label());
    assertEquals("TRECHO", out.passages().get(0).kind());
    assertEquals("Segundo [1] especialistas...", out.passages().get(0).content());
  }

  @Test
  void maskForProvaKeepsPassagesVisible() {
    Question q = question(1L, 11, "A", false);
    when(questions.findById(1L)).thenReturn(Optional.of(q));
    when(options.findByQuestionIdOrdered(1L)).thenReturn(List.of(option(q, "A", "4")));
    when(classifications.findActiveByQuestionId(1L)).thenReturn(List.of(classification(q)));
    when(questionPassages.findByQuestionIdsOrdered(List.of(1L)))
        .thenReturn(List.of(passageLink(1L, "Trecho — questões 11 a 15", "TRECHO", "Segundo [1] especialistas...")));
    when(caderno.existsInProvaInProgress(1L, 1L)).thenReturn(true);

    QuestionResponse out = serviceWithPassages().getByIdForUser(1L, 1L);

    assertNull(out.answerKey());
    assertEquals(1, out.passages().size());
    assertEquals("Segundo [1] especialistas...", out.passages().get(0).content());
  }

  @Test
  void searchWithoutPassageRepoReturnsEmpty() {
    // Construtor sem repositório (compat): nunca falha, só lista vazia.
    Question q = question(1L, 21, "A", false);
    when(questions.search(eq(null), eq(null), eq(null), eq(null), eq(null), eq(null), any()))
        .thenReturn(new PageImpl<>(List.of(q), PageRequest.of(0, 20), 1));
    when(options.findByQuestionIdsOrdered(List.of(1L))).thenReturn(List.of(option(q, "A", "4")));
    when(classifications.findActiveByQuestionIds(List.of(1L)))
        .thenReturn(List.of(classification(q)));

    PageResponse<QuestionResponse> out =
        service.search(null, null, null, null, null, null, 0, 20);

    assertNotNull(out.content().get(0).passages());
    assertTrue(out.content().get(0).passages().isEmpty());
  }
}
