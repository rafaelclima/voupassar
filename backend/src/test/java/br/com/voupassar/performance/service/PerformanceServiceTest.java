package br.com.voupassar.performance.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.performance.dto.PerformanceEvolutionResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.entity.StudentTopicPerformance;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Regras do PerformanceService (TASK 4.1) com mocks — sem banco.
 *
 * <p>Determinístico sobre o fato imutável: geral/disciplinafactual +
 * assunto/subassunto pela vigente não-rejeitada; anuladas fora do
 * aproveitamento; evolução em baldes UTC sem interpolação; rebuild
 * idempotente só com pontuáveis.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceServiceTest {

  @Mock UserRepository users;
  @Mock QuestionAttemptRepository attempts;
  @Mock QuestionClassificationRepository classifications;
  @Mock StudentTopicPerformanceRepository performance;

  private PerformanceService service;

  @BeforeEach
  void setup() {
    service = new PerformanceService(users, attempts, classifications, performance);
  }

  private static User user(long id, boolean active) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha-segura-123"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(active);
    return u;
  }

  private static Discipline discipline(String code, String name) {
    Discipline d = new Discipline(code, name);
    ReflectionTestUtils.setField(d, "id", 1L);
    return d;
  }

  private static Topic topic(long id, String code, String name, Discipline d) {
    Topic t = new Topic();
    ReflectionTestUtils.setField(t, "id", id);
    ReflectionTestUtils.setField(t, "code", code);
    ReflectionTestUtils.setField(t, "name", name);
    ReflectionTestUtils.setField(t, "discipline", d);
    return t;
  }

  private static Subtopic subtopic(long id, String code, String name, Topic t) {
    Subtopic s = new Subtopic();
    ReflectionTestUtils.setField(s, "id", id);
    ReflectionTestUtils.setField(s, "code", code);
    ReflectionTestUtils.setField(s, "name", name);
    ReflectionTestUtils.setField(s, "topic", t);
    return s;
  }

  private static Question question(long id, Discipline d) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "discipline", d);
    return q;
  }

  private static QuestionAttempt attempt(
      long id, User u, Question q, boolean annulled, Boolean correct, String answeredAt) {
    QuestionAttempt a = new QuestionAttempt();
    ReflectionTestUtils.setField(a, "id", id);
    a.setUser(u);
    a.setQuestion(q);
    a.setAnnulled(annulled);
    a.setCorrect(correct);
    a.setMode("ESTUDO");
    a.setSelectedOption("A");
    a.setAnsweredAt(OffsetDateTime.parse(answeredAt));
    return a;
  }

  private static QuestionClassification classification(
      long id, Question q, Topic t, Subtopic s) {
    QuestionClassification c = new QuestionClassification();
    ReflectionTestUtils.setField(c, "id", id);
    ReflectionTestUtils.setField(c, "question", q);
    ReflectionTestUtils.setField(c, "topic", t);
    ReflectionTestUtils.setField(c, "subtopic", s);
    ReflectionTestUtils.setField(c, "confidence", "ALTA");
    ReflectionTestUtils.setField(c, "status", "PENDING");
    return c;
  }

  private void activeUser() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
  }

  // ---- overview ----

  @Test
  void overviewEmptyIsZeroWithNullAccuracy() {
    activeUser();
    when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of());

    PerformanceOverviewResponse out = service.getOverview(1L);

    assertEquals(0L, out.totalAttempts());
    assertEquals(0L, out.scoredAttempts());
    assertNull(out.accuracy());
    assertNull(out.lastAttemptAt());
    assertTrue(out.byDiscipline().isEmpty());
    assertTrue(out.byTopic().isEmpty());
    assertTrue(out.bySubtopic().isEmpty());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("Nenhuma tentativa")));
  }

  @Test
  void overviewAggregatesByDisciplineTopicSubtopic() {
    activeUser();
    Discipline mat = discipline("MATEMATICA", "Matemática");
    Topic porc = topic(5L, "PORCENTAGEM", "Porcentagem", mat);
    Subtopic calc = subtopic(37L, "CALCULO_DIRETO", "Cálculo direto", porc);
    Topic alg = topic(6L, "ALGEBRA", "Álgebra", mat);

    Question q12 = question(12L, mat);
    Question q13 = question(13L, mat);
    Question q14 = question(14L, mat);
    Question q37 = question(37L, mat);
    User u = user(1L, true);

    when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of(
        attempt(1L, u, q12, false, Boolean.TRUE, "2026-09-01T10:00:00Z"),
        attempt(2L, u, q13, false, Boolean.FALSE, "2026-09-02T10:00:00Z"),
        attempt(3L, u, q14, false, Boolean.TRUE, "2026-09-03T10:00:00Z"),
        attempt(4L, u, q37, true, null, "2026-09-04T10:00:00Z")));
    when(classifications.findActiveByQuestionIds(anyList())).thenReturn(List.of(
        classification(1L, q12, porc, calc),
        classification(2L, q13, porc, calc),
        classification(3L, q14, alg, null),
        classification(4L, q37, porc, calc)));

    PerformanceOverviewResponse out = service.getOverview(1L);

    assertEquals(4L, out.totalAttempts());
    assertEquals(3L, out.scoredAttempts());
    assertEquals(2L, out.correct());
    assertEquals(1L, out.incorrect());
    assertEquals(1L, out.annulled());
    assertEquals(2.0 / 3.0, out.accuracy());
    assertEquals(0L, out.unclassifiedAttempts());
    assertNotNull(out.lastAttemptAt());

    assertEquals(1, out.byDiscipline().size());
    assertEquals("MATEMATICA", out.byDiscipline().get(0).disciplineCode());
    assertEquals(4L, out.byDiscipline().get(0).attempts());
    assertEquals(3L, out.byDiscipline().get(0).scored());

    assertEquals(2, out.byTopic().size());
    // piores primeiro: PORCENTAGEM 0.5 antes de ALGEBRA 1.0
    assertEquals("PORCENTAGEM", out.byTopic().get(0).topicCode());
    assertEquals(3L, out.byTopic().get(0).attempts());
    assertEquals(2L, out.byTopic().get(0).scored());
    assertEquals(1L, out.byTopic().get(0).correct());
    assertEquals("ALGEBRA", out.byTopic().get(1).topicCode());

    assertEquals(1, out.bySubtopic().size());
    assertEquals("CALCULO_DIRETO", out.bySubtopic().get(0).subtopicCode());
    assertEquals(3L, out.bySubtopic().get(0).attempts());
    assertEquals(2L, out.bySubtopic().get(0).scored());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("Anuladas")));
  }

  @Test
  void overviewCountsUnclassifiedWithoutInventingTopic() {
    activeUser();
    Discipline mat = discipline("MATEMATICA", "Matemática");
    Question q = question(99L, mat);
    User u = user(1L, true);

    when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of(
        attempt(1L, u, q, false, Boolean.TRUE, "2026-09-01T10:00:00Z")));
    when(classifications.findActiveByQuestionIds(anyList())).thenReturn(List.of());

    PerformanceOverviewResponse out = service.getOverview(1L);

    assertEquals(1L, out.totalAttempts());
    assertEquals(1L, out.unclassifiedAttempts());
    assertTrue(out.byTopic().isEmpty());
    assertTrue(out.bySubtopic().isEmpty());
    assertEquals(1, out.byDiscipline().size());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("sem classificação vigente")));
  }

  @Test
  void overviewInactiveIs401() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, false)));

    assertThrows(UnauthorizedException.class, () -> service.getOverview(1L));
    assertThrows(UnauthorizedException.class, () -> service.getEvolution(1L, "WEEK"));
  }

  // ---- evolution ----

  @Test
  void evolutionBucketsDayWeekMonth() {
    activeUser();
    Discipline mat = discipline("MATEMATICA", "Matemática");
    Question q = question(12L, mat);
    User u = user(1L, true);

    when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of(
        attempt(1L, u, q, false, Boolean.TRUE, "2026-09-01T10:00:00Z"),
        attempt(2L, u, q, false, Boolean.FALSE, "2026-09-02T10:00:00Z"),
        attempt(3L, u, q, false, Boolean.TRUE, "2026-09-08T10:00:00Z")));

    PerformanceEvolutionResponse day = service.getEvolution(1L, "day");
    assertEquals("DAY", day.granularity());
    assertEquals(3, day.buckets().size());
    assertEquals(LocalDate.of(2026, 9, 1), day.buckets().get(0).bucketStart());
    assertEquals(1.0, day.buckets().get(0).accuracy());
    assertEquals(0.0, day.buckets().get(1).accuracy());

    PerformanceEvolutionResponse week = service.getEvolution(1L, "WEEK");
    assertEquals(2, week.buckets().size());
    // semanas ISO começando na segunda-feira
    assertEquals(LocalDate.of(2026, 8, 31), week.buckets().get(0).bucketStart());
    assertEquals(2L, week.buckets().get(0).attempts());
    assertEquals(0.5, week.buckets().get(0).accuracy());
    assertEquals(LocalDate.of(2026, 9, 7), week.buckets().get(1).bucketStart());

    PerformanceEvolutionResponse month = service.getEvolution(1L, "MONTH");
    assertEquals(1, month.buckets().size());
    assertEquals(LocalDate.of(2026, 9, 1), month.buckets().get(0).bucketStart());
    assertEquals(3L, month.buckets().get(0).attempts());
    assertEquals(2.0 / 3.0, month.buckets().get(0).accuracy());
  }

  @Test
  void evolutionRejectsInvalidGranularity() {
    activeUser();

    assertThrows(BadRequestException.class, () -> service.getEvolution(1L, "YEAR"));
    assertThrows(BadRequestException.class, () -> service.getEvolution(1L, ""));
  }

  // ---- rebuild ----

  @Test
  void rebuildWritesTopicAndSubtopicRowsSkippingAnnulled() {
    activeUser();
    Discipline mat = discipline("MATEMATICA", "Matemática");
    Topic porc = topic(5L, "PORCENTAGEM", "Porcentagem", mat);
    Subtopic calc = subtopic(37L, "CALCULO_DIRETO", "Cálculo direto", porc);
    Topic alg = topic(6L, "ALGEBRA", "Álgebra", mat);

    Question q12 = question(12L, mat);
    Question q13 = question(13L, mat);
    Question q14 = question(14L, mat);
    Question q37 = question(37L, mat);
    User u = user(1L, true);

    when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of(
        attempt(1L, u, q12, false, Boolean.TRUE, "2026-09-01T10:00:00Z"),
        attempt(2L, u, q13, false, Boolean.FALSE, "2026-09-02T10:00:00Z"),
        attempt(3L, u, q14, false, Boolean.TRUE, "2026-09-03T10:00:00Z"),
        attempt(4L, u, q37, true, null, "2026-09-04T10:00:00Z")));
    when(classifications.findActiveByQuestionIds(anyList())).thenReturn(List.of(
        classification(1L, q12, porc, calc),
        classification(2L, q13, porc, calc),
        classification(3L, q14, alg, null),
        classification(4L, q37, porc, calc)));
    when(performance.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var out = service.rebuildTopicPerformance(1L);

    assertEquals(3, out.rebuiltRows());
    assertNotNull(out.rebuiltAt());
    verify(performance).deleteByUserId(1L);

    ArgumentCaptor<List<StudentTopicPerformance>> cap = ArgumentCaptor.forClass(List.class);
    verify(performance).saveAll(cap.capture());
    List<StudentTopicPerformance> rows = cap.getValue();
    assertEquals(3, rows.size());

    StudentTopicPerformance porcTopic = rows.stream()
        .filter(r -> r.getTopic().getId().equals(5L) && r.getSubtopic() == null)
        .findFirst().orElseThrow();
    assertEquals(2, porcTopic.getAttempts());
    assertEquals(1, porcTopic.getHits());

    StudentTopicPerformance calcRow = rows.stream()
        .filter(r -> r.getSubtopic() != null && r.getSubtopic().getId().equals(37L))
        .findFirst().orElseThrow();
    assertEquals(2, calcRow.getAttempts());
    assertEquals(1, calcRow.getHits());

    StudentTopicPerformance algTopic = rows.stream()
        .filter(r -> r.getTopic().getId().equals(6L))
        .findFirst().orElseThrow();
    assertEquals(1, algTopic.getAttempts());
    assertEquals(1, algTopic.getHits());
  }

  @Test
  void rebuildWithOnlyAnnulledSavesNothing() {
    activeUser();
    Discipline mat = discipline("MATEMATICA", "Matemática");
    Topic porc = topic(5L, "PORCENTAGEM", "Porcentagem", mat);
    Subtopic calc = subtopic(37L, "CALCULO_DIRETO", "Cálculo direto", porc);
    Question q37 = question(37L, mat);
    User u = user(1L, true);

    when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of(
        attempt(4L, u, q37, true, null, "2026-09-04T10:00:00Z")));
    when(classifications.findActiveByQuestionIds(anyList())).thenReturn(List.of(
        classification(4L, q37, porc, calc)));
    when(performance.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

    var out = service.rebuildTopicPerformance(1L);

    assertEquals(0, out.rebuiltRows());
    verify(performance).deleteByUserId(1L);
  }
}
