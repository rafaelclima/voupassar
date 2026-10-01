package br.com.voupassar.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.review.dto.ReviewQueueResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

  @Mock UserRepository users;
  @Mock QuestionAttemptRepository attempts;
  @Mock QuestionClassificationRepository classifications;
  @Mock TopicRepository topics;
  @Mock DisciplineRepository disciplines;

  private ReviewService service;

  @BeforeEach
  void setup() {
    service = new ReviewService(users, attempts, classifications, topics, disciplines);
  }

  private static User user(long id, boolean active) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(active);
    return u;
  }

  private static Discipline discipline(long id, String code, String name) {
    Discipline d = new Discipline(code, name);
    ReflectionTestUtils.setField(d, "id", id);
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

  private static Question question(long id, Discipline d, int year, int number) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "discipline", d);
    ReflectionTestUtils.setField(q, "sourceYear", (short) year);
    ReflectionTestUtils.setField(q, "sourceQuestionNumber", (short) number);
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

  private static QuestionClassification classification(long id, Question q, Topic t) {
    QuestionClassification c = new QuestionClassification();
    ReflectionTestUtils.setField(c, "id", id);
    ReflectionTestUtils.setField(c, "question", q);
    ReflectionTestUtils.setField(c, "topic", t);
    ReflectionTestUtils.setField(c, "subtopic", null);
    ReflectionTestUtils.setField(c, "confidence", "ALTA");
    ReflectionTestUtils.setField(c, "status", "PENDING");
    ReflectionTestUtils.setField(c, "taxonomyVersion", "v1.1");
    return c;
  }

  @Test
  void emptyQueueIsUnknown() {
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
    Mockito.when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of());

    ReviewQueueResponse out = service.getQueue(1L, null, null, null, false);

    assertEquals(0L, out.totalAttempts());
    assertEquals(0L, out.distinctQuestions());
    assertTrue(out.items().isEmpty());
    assertTrue(out.accuracy() == null);
    assertTrue(!out.notes().isEmpty());
  }

  @Test
  void errorsComeBeforeFragileThenConsolidated() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Topic t = topic(10L, "PORC", "Porcentagem", mat);
    Question q1 = question(1L, mat, 2026, 1);
    Question q2 = question(2L, mat, 2026, 2);
    Question q3 = question(3L, mat, 2026, 3);

    // Ordem cronológica (ASC): a última de cada questão é a mais recente.
    List<QuestionAttempt> all = List.of(
        attempt(1L, u, q1, false, false, "2026-09-01T10:00:00Z"),
        attempt(2L, u, q1, false, false, "2026-09-02T10:00:00Z"),
        attempt(3L, u, q2, false, true, "2026-09-01T11:00:00Z"),
        attempt(4L, u, q2, false, false, "2026-09-03T10:00:00Z"),
        attempt(5L, u, q3, false, true, "2026-09-04T10:00:00Z"));
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(all);
    Mockito.when(classifications.findActiveByQuestionIds(Mockito.anyList()))
        .thenReturn(List.of(
            classification(31L, q1, t),
            classification(32L, q2, t),
            classification(33L, q3, t)));

    ReviewQueueResponse out = service.getQueue(1L, null, null, null, false);

    assertEquals(3L, out.distinctQuestions());
    assertEquals(3, out.items().size());
    // Q1 nunca acertada (0/2) antes de Q2 regressão (1/2, última errada).
    assertEquals(1L, out.items().get(0).questionId());
    assertEquals("ERRO_SEM_ACERTO", out.items().get(0).reviewCategory());
    assertEquals(2L, out.items().get(1).questionId());
    assertEquals("ERRO_RECENTE", out.items().get(1).reviewCategory());
    // Q3 última correta, mas assunto 2/5 = FRÁGIL → TOPICO_FRAGIL.
    assertEquals(3L, out.items().get(2).questionId());
    assertEquals("TOPICO_FRAGIL", out.items().get(2).reviewCategory());
    assertEquals("FRAGIL", out.items().get(2).topicMastery());
    // Ranks 1-based e motivos auditáveis.
    assertEquals(1, out.items().get(0).rank());
    assertTrue(out.items().get(0).reason().contains("0/2"));
  }

  @Test
  void annulledStaysOutOfQueue() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Topic t = topic(10L, "PORC", "Porcentagem", mat);
    Question q1 = question(1L, mat, 2026, 1);
    Question q9 = question(9L, mat, 2026, 9);

    List<QuestionAttempt> all = List.of(
        attempt(1L, u, q1, false, false, "2026-09-01T10:00:00Z"),
        attempt(2L, u, q9, true, null, "2026-09-02T10:00:00Z"));
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(all);
    Mockito.when(classifications.findActiveByQuestionIds(Mockito.anyList()))
        .thenReturn(List.of(classification(31L, q1, t)));

    ReviewQueueResponse out = service.getQueue(1L, null, null, null, false);

    assertEquals(1L, out.annulled());
    assertEquals(1L, out.distinctQuestions());
    assertEquals(1, out.items().size());
    assertEquals(1L, out.items().get(0).questionId());
  }

  @Test
  void onlyErrorsAndLimitAreDeterministic() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Topic t = topic(10L, "PORC", "Porcentagem", mat);
    Question q1 = question(1L, mat, 2026, 1);
    Question q2 = question(2L, mat, 2026, 2);
    Question q3 = question(3L, mat, 2026, 3);

    List<QuestionAttempt> all = List.of(
        attempt(1L, u, q1, false, false, "2026-09-01T10:00:00Z"),
        attempt(2L, u, q2, false, true, "2026-09-01T11:00:00Z"),
        attempt(3L, u, q2, false, false, "2026-09-03T10:00:00Z"),
        attempt(4L, u, q3, false, true, "2026-09-04T10:00:00Z"));
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(all);
    Mockito.when(classifications.findActiveByQuestionIds(Mockito.anyList()))
        .thenReturn(List.of(
            classification(31L, q1, t),
            classification(32L, q2, t),
            classification(33L, q3, t)));

    ReviewQueueResponse only = service.getQueue(1L, null, null, null, true);
    assertEquals(2, only.items().size());
    assertTrue(only.items().stream().allMatch(i ->
        i.reviewCategory().equals("ERRO_SEM_ACERTO")
            || i.reviewCategory().equals("ERRO_RECENTE")));

    ReviewQueueResponse limited = service.getQueue(1L, 1, null, null, false);
    assertEquals(1, limited.items().size());
    assertEquals(1, limited.limit());
    assertEquals(1L, limited.items().get(0).questionId());
  }

  @Test
  void invalidLimitIs400() {
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
    assertThrows(BadRequestException.class, () -> service.getQueue(1L, 0, null, null, false));
    assertThrows(BadRequestException.class, () -> service.getQueue(1L, 101, null, null, false));
  }

  @Test
  void categoriesAreDeterministic() {
    assertEquals("ERRO_SEM_ACERTO", ReviewService.categorize(true, true, "FRAGIL", 2));
    assertEquals("ERRO_RECENTE", ReviewService.categorize(false, true, "DOMINADO", 3));
    assertEquals("TOPICO_FRAGIL", ReviewService.categorize(false, false, "FRAGIL", 3));
    assertEquals("REFORCO", ReviewService.categorize(false, false, "EM_DESENVOLVIMENTO", 3));
    assertEquals("REFORCO", ReviewService.categorize(false, false, "NAO_CLASSIFICADO", 1));
    assertEquals("MANUTENCAO", ReviewService.categorize(false, false, "DOMINADO", 1));
    assertEquals("CONSOLIDADO", ReviewService.categorize(false, false, "DOMINADO", 4));
  }
}
