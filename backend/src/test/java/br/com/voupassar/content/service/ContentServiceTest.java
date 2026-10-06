package br.com.voupassar.content.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import br.com.voupassar.content.dto.ContentStatsResponse;
import br.com.voupassar.content.dto.DisciplineDetailResponse;
import br.com.voupassar.content.dto.DisciplineSummaryResponse;
import br.com.voupassar.content.dto.SubtopicDetailResponse;
import br.com.voupassar.content.dto.TopicDetailResponse;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.SubtopicRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Serviço de conteúdos sem subir contexto (TASK 3.3).
 *
 * <p>Valida regras de evidência: códigos inexistentes → 404 com código
 * explícito; banco vazio não inventa (zeros + notas); anuladas contam como
 * conteúdo; confiança BAIXA é sinalizada, nunca omitida.
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

  @Mock DisciplineRepository disciplines;
  @Mock TopicRepository topics;
  @Mock SubtopicRepository subtopics;
  @Mock QuestionRepository questions;
  @Mock QuestionClassificationRepository classifications;

  ContentService service;

  @BeforeEach
  void setup() {
    service = new ContentService(disciplines, topics, subtopics, questions, classifications);
  }

  private Discipline discipline(long id, String code, String name) {
    Discipline d = new Discipline(code, name);
    ReflectionTestUtils.setField(d, "id", id);
    return d;
  }

  private Topic topic(long id, Discipline d, String code, String name) {
    Topic t = new Topic();
    ReflectionTestUtils.setField(t, "id", id);
    ReflectionTestUtils.setField(t, "discipline", d);
    ReflectionTestUtils.setField(t, "code", code);
    ReflectionTestUtils.setField(t, "name", name);
    ReflectionTestUtils.setField(t, "active", true);
    return t;
  }

  private Subtopic subtopic(long id, Topic t, String code, String name) {
    Subtopic s = new Subtopic();
    ReflectionTestUtils.setField(s, "id", id);
    ReflectionTestUtils.setField(s, "topic", t);
    ReflectionTestUtils.setField(s, "code", code);
    ReflectionTestUtils.setField(s, "name", name);
    ReflectionTestUtils.setField(s, "active", true);
    return s;
  }

  @Test
  void listDisciplinesWithCounts() {
    Discipline lp = discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa");
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    when(disciplines.findAllByOrderByCodeAsc()).thenReturn(List.of(lp, mat));
    when(topics.countByDisciplineCode("LINGUA_PORTUGUESA")).thenReturn(2L);
    when(topics.countByDisciplineCode("MATEMATICA")).thenReturn(8L);
    when(questions.countByDisciplineCode("LINGUA_PORTUGUESA")).thenReturn(120L);
    when(questions.countByDisciplineCode("MATEMATICA")).thenReturn(120L);

    List<DisciplineSummaryResponse> out = service.listDisciplines();

    assertEquals(2, out.size());
    assertEquals("LINGUA_PORTUGUESA", out.get(0).code());
    assertEquals(2L, out.get(0).topicCount());
    assertEquals(120L, out.get(1).questionCount());
  }

  @Test
  void getDisciplineReturnsTopics() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    Topic algebra = topic(5L, mat, "ALGEBRA", "Álgebra");
    when(disciplines.findByCode("MATEMATICA")).thenReturn(Optional.of(mat));
    when(topics.findByDisciplineCodeOrdered("MATEMATICA")).thenReturn(List.of(algebra));
    when(classifications.countByTopic()).thenReturn(List.<Object[]>of(new Object[] {5L, 18L}));
    when(subtopics.countByTopicId(5L)).thenReturn(4L);
    when(questions.countByDisciplineCode("MATEMATICA")).thenReturn(120L);

    DisciplineDetailResponse out = service.getDiscipline("MATEMATICA");

    assertEquals("MATEMATICA", out.code());
    assertEquals(1, out.topics().size());
    assertEquals(18L, out.topics().get(0).questionCount());
    assertEquals(120L, out.questionCount());
  }

  @Test
  void getDisciplineIsCaseInsensitiveButUnknownIs404() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    when(disciplines.findByCode("MATEMATICA")).thenReturn(Optional.of(mat));
    when(topics.findByDisciplineCodeOrdered("MATEMATICA")).thenReturn(List.of());
    when(classifications.countByTopic()).thenReturn(List.of());
    when(questions.countByDisciplineCode("MATEMATICA")).thenReturn(0L);

    assertEquals("MATEMATICA", service.getDiscipline("matematica").code());

    when(disciplines.findByCode("FISICA")).thenReturn(Optional.empty());
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getDiscipline("FISICA"));
    assertEquals("DISCIPLINE_NOT_FOUND", ex.getCode());
  }

  @Test
  void getDisciplineBlankIsBadRequest() {
    assertThrows(BadRequestException.class, () -> service.getDiscipline(""));
    assertThrows(BadRequestException.class, () -> service.getDiscipline("mat-1"));
  }

  @Test
  void listTopicsFiltersByDiscipline() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    Topic algebra = topic(5L, mat, "ALGEBRA", "Álgebra");
    when(disciplines.findByCode("MATEMATICA")).thenReturn(Optional.of(mat));
    when(topics.findByDisciplineCodeOrdered("MATEMATICA")).thenReturn(List.of(algebra));
    when(classifications.countByTopic()).thenReturn(List.<Object[]>of(new Object[] {5L, 18L}));
    when(subtopics.countByTopicId(5L)).thenReturn(4L);

    assertEquals(1, service.listTopics("MATEMATICA").size());

    when(disciplines.findByCode("FISICA")).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.listTopics("FISICA"));
  }

  @Test
  void getTopicReturnsHistory() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    Topic algebra = topic(5L, mat, "ALGEBRA", "Álgebra");
    Subtopic eq = subtopic(25L, algebra, "EQUACOES", "Equações");
    when(topics.findByIdWithDiscipline(5L)).thenReturn(Optional.of(algebra));
    when(classifications.countByTopicId(5L)).thenReturn(18L);
    when(classifications.editionsByTopic(5L)).thenReturn(List.of((short) 2020, (short) 2026));
    when(classifications.countTopicByYear(5L))
        .thenReturn(
            List.<Object[]>of(
                new Object[] {(short) 2020, 3L, 0L}, new Object[] {(short) 2026, 4L, 0L}));
    when(classifications.confidenceByTopic(5L))
        .thenReturn(List.<Object[]>of(new Object[] {"ALTA", 14L}, new Object[] {"MEDIA", 4L}));
    when(subtopics.findByTopicIdOrdered(5L)).thenReturn(List.of(eq));
    when(classifications.countBySubtopic())
        .thenReturn(List.<Object[]>of(new Object[] {25L, 8L}));

    TopicDetailResponse out = service.getTopic(5L);

    assertEquals("ALGEBRA", out.code());
    assertEquals(18L, out.questionCount());
    assertEquals(List.of(2020, 2026), out.editions());
    assertEquals(2, out.perEdition().size());
    assertEquals(14L, out.confidence().alta());
    assertEquals(4L, out.confidence().media());
    assertEquals(1, out.subtopics().size());
    assertEquals(8L, out.subtopics().get(0).questionCount());
  }

  @Test
  void getTopicUnknownIs404AndInvalidIdIs400() {
    when(topics.findByIdWithDiscipline(999L)).thenReturn(Optional.empty());
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getTopic(999L));
    assertEquals("TOPIC_NOT_FOUND", ex.getCode());
    assertThrows(BadRequestException.class, () -> service.getTopic(0L));
  }

  @Test
  void listSubtopicsFiltersByTopic() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    Topic algebra = topic(5L, mat, "ALGEBRA", "Álgebra");
    Subtopic eq = subtopic(25L, algebra, "EQUACOES", "Equações");
    when(topics.findByIdWithDiscipline(5L)).thenReturn(Optional.of(algebra));
    when(subtopics.findByTopicIdOrdered(5L)).thenReturn(List.of(eq));
    when(classifications.countBySubtopic())
        .thenReturn(List.<Object[]>of(new Object[] {25L, 8L}));

    assertEquals(1, service.listSubtopics(5L).size());

    when(topics.findByIdWithDiscipline(999L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.listSubtopics(999L));
  }

  @Test
  void getSubtopicReturnsHistory() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    Topic algebra = topic(5L, mat, "ALGEBRA", "Álgebra");
    Subtopic eq = subtopic(25L, algebra, "EQUACOES", "Equações");
    when(subtopics.findByIdWithTopic(25L)).thenReturn(Optional.of(eq));
    when(classifications.countBySubtopicId(25L)).thenReturn(8L);
    when(classifications.editionsBySubtopic(25L)).thenReturn(List.of((short) 2026));
    when(classifications.countSubtopicByYear(25L))
        .thenReturn(List.<Object[]>of(new Object[] {(short) 2026, 2L, 0L}));
    when(classifications.confidenceBySubtopic(25L))
        .thenReturn(List.<Object[]>of(new Object[] {"ALTA", 8L}));

    SubtopicDetailResponse out = service.getSubtopic(25L);

    assertEquals("EQUACOES", out.code());
    assertEquals(8L, out.questionCount());
    assertEquals(List.of(2026), out.editions());
    assertEquals(8L, out.confidence().alta());
  }

  @Test
  void getSubtopicUnknownIs404() {
    when(subtopics.findByIdWithTopic(999L)).thenReturn(Optional.empty());
    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getSubtopic(999L));
    assertEquals("SUBTOPIC_NOT_FOUND", ex.getCode());
  }

  @Test
  void statsWithoutDataDoesNotInvent() {
    when(questions.count()).thenReturn(0L);
    when(classifications.countClassified()).thenReturn(0L);
    when(classifications.distinctTaxonomyVersions()).thenReturn(List.of());
    when(disciplines.findAllByOrderByCodeAsc()).thenReturn(List.of());
    when(topics.findAllOrdered()).thenReturn(List.of());
    when(classifications.statsByTopic()).thenReturn(List.of());

    ContentStatsResponse stats = service.getContentStats();

    assertEquals(0L, stats.totalQuestions());
    assertEquals(0L, stats.totalClassified());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("Nenhuma questão importada")));
  }

  @Test
  void statsComputesPercent() {
    Discipline mat = discipline(2L, "MATEMATICA", "Matemática");
    Topic algebra = topic(5L, mat, "ALGEBRA", "Álgebra");
    when(questions.count()).thenReturn(240L);
    when(classifications.countClassified()).thenReturn(240L);
    when(classifications.distinctTaxonomyVersions()).thenReturn(List.of("v1.1"));
    when(disciplines.findAllByOrderByCodeAsc()).thenReturn(List.of(mat));
    when(topics.countByDisciplineCode("MATEMATICA")).thenReturn(1L);
    when(questions.countByDisciplineCode("MATEMATICA")).thenReturn(120L);
    when(topics.findAllOrdered()).thenReturn(List.of(algebra));
    when(classifications.statsByTopic())
        .thenReturn(List.<Object[]>of(new Object[] {5L, 18L, 0L}));
    when(classifications.editionsByTopic(5L))
        .thenReturn(
            List.of((short) 2020, (short) 2022, (short) 2023, (short) 2024, (short) 2025, (short) 2026));

    ContentStatsResponse stats = service.getContentStats();

    assertEquals(240L, stats.totalQuestions());
    assertEquals(List.of("v1.1"), stats.taxonomyVersions());
    assertEquals(7.5, stats.perTopic().get(0).percentOfClassified(), 0.001);
    assertEquals(6, stats.perTopic().get(0).editionsCount());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("DESCONHECIDA")));
  }
}
