package br.com.voupassar.review.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;

import br.com.voupassar.attempts.entity.StudySession;
import br.com.voupassar.attempts.repository.StudySessionRepository;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.review.dto.CreateReviewSessionRequest;
import br.com.voupassar.review.dto.ReviewQueueResponse;
import br.com.voupassar.review.dto.ReviewQueueResponse.ReviewItem;
import br.com.voupassar.review.dto.ReviewSessionResponse;
import br.com.voupassar.review.dto.ReviewSessionResultResponse;
import br.com.voupassar.review.entity.ReviewSessionQuestion;
import br.com.voupassar.review.repository.ReviewSessionQuestionRepository;
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

/**
 * Regras da sessão de revisão (TASK 5.4) com mocks — sem banco.
 *
 * <p>Caderno congelado do top-N da fila (TASK 4.5), progresso com feedback
 * imediato sempre revelado (REVISAO nunca oculta, ao contrário da PROVA na
 * TASK 5.3) e resultado só após encerrar.
 */
@ExtendWith(MockitoExtension.class)
class ReviewSessionServiceTest {

  @Mock UserRepository users;
  @Mock StudySessionRepository sessions;
  @Mock ReviewSessionQuestionRepository caderno;
  @Mock QuestionAttemptRepository responses;
  @Mock QuestionRepository questions;
  @Mock ReviewService review;

  private ReviewSessionService service;

  @BeforeEach
  void setup() {
    service = new ReviewSessionService(users, sessions, caderno, responses, questions, review);
  }

  private static User user(long id, boolean active) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha-segura-123"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(active);
    return u;
  }

  private static Discipline discipline(long id, String code, String name) {
    Discipline d = new Discipline(code, name);
    ReflectionTestUtils.setField(d, "id", id);
    return d;
  }

  private static Question question(long id, Discipline d, int year, int number) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "discipline", d);
    ReflectionTestUtils.setField(q, "sourceYear", (short) year);
    ReflectionTestUtils.setField(q, "sourceQuestionNumber", (short) number);
    ReflectionTestUtils.setField(q, "answerKey", "A");
    ReflectionTestUtils.setField(q, "annulled", false);
    return q;
  }

  private static ReviewItem item(long questionId, Discipline d, String category, String reason) {
    return new ReviewItem(
        1, questionId, d.getCode(), d.getName(), 2026, (int) questionId,
        10L, "PORC", "Porcentagem",
        null, null, null,
        "ALTA", "v1.1",
        2L, 0L, 2L, false,
        OffsetDateTime.parse("2026-09-02T10:00:00Z"), 5L,
        "EM_OBSERVACAO", 2L, 0.0,
        category, reason);
  }

  private static ReviewQueueResponse queue(List<ReviewItem> items) {
    return new ReviewQueueResponse(
        4L, 4L, 1L, 3L, 0L, 0.25, 0L, items.size(), items.size(), 20, false, null, null,
        items, List.of("nota"));
  }

  private static StudySession session(long id, String mode, String status) {
    StudySession s = new StudySession();
    ReflectionTestUtils.setField(s, "id", id);
    s.setMode(mode);
    s.setStatus(status);
    s.setStartedAt(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
    return s;
  }

  private static ReviewSessionQuestion row(long sessionId, int position, Question q) {
    ReviewSessionQuestion r = new ReviewSessionQuestion();
    r.setStudySessionId(sessionId);
    r.setPosition((short) position);
    r.setQuestion(q);
    return r;
  }

  private static QuestionAttempt response(
      long id, User u, Question q, long sessionId, String selected, Boolean correct) {
    QuestionAttempt a = new QuestionAttempt();
    ReflectionTestUtils.setField(a, "id", id);
    a.setUser(u);
    a.setQuestion(q);
    a.setStudySessionId(sessionId);
    a.setSelectedOption(selected);
    a.setCorrect(correct);
    a.setAnnulled(false);
    a.setMode("REVISAO");
    a.setAnsweredAt(OffsetDateTime.parse("2026-10-01T11:00:00Z"));
    return a;
  }

  private static CreateReviewSessionRequest createReq() {
    return new CreateReviewSessionRequest(null, null, null, false);
  }

  @Test
  void createFreezesTopNAsCaderno() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Question q1 = question(1L, mat, 2026, 1);
    Question q2 = question(2L, mat, 2026, 2);
    List<ReviewItem> items = List.of(
        item(1L, mat, "ERRO_SEM_ACERTO", "0/2 na questão 1; prioridade máxima."),
        item(2L, mat, "ERRO_RECENTE", "1/2 na questão 2; regressão."));

    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(review.getQueue(1L, null, null, null, false)).thenReturn(queue(items));
    Mockito.when(questions.findAllById(List.of(1L, 2L))).thenReturn(List.of(q1, q2));
    Mockito.when(sessions.save(any(StudySession.class))).thenAnswer(inv -> {
      StudySession s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", 7L);
      return s;
    });

    ReviewSessionResponse out = service.create(1L, createReq());

    assertEquals(7L, out.sessionId());
    assertEquals("REVISAO", out.mode());
    assertEquals("IN_PROGRESS", out.status());
    assertEquals(2, out.totalItems());
    assertEquals(0L, out.answered());
    assertEquals(1L, out.items().get(0).questionId());
    assertEquals("ERRO_SEM_ACERTO", out.items().get(0).reviewCategory());
    assertFalse(out.items().get(0).answered());
    assertNull(out.items().get(0).selectedOption());
    Mockito.verify(caderno).saveAll(anyList());
  }

  @Test
  void createWithEmptyQueueIs400() {
    User u = user(1L, true);
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(review.getQueue(1L, null, null, null, false))
        .thenReturn(queue(List.of()));

    assertThrows(BadRequestException.class, () -> service.create(1L, createReq()));
  }

  @Test
  void getShowsProgressWithImmediateFeedback() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Question q1 = question(1L, mat, 2026, 1);
    Question q2 = question(2L, mat, 2026, 2);
    StudySession s = session(7L, "REVISAO", "IN_PROGRESS");
    List<ReviewSessionQuestion> rows = List.of(row(7L, 1, q1), row(7L, 2, q2));
    List<ReviewItem> items = List.of(
        item(1L, mat, "ERRO_SEM_ACERTO", "0/2 na questão 1; prioridade máxima."),
        item(2L, mat, "ERRO_RECENTE", "1/2 na questão 2; regressão."));

    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(sessions.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(s));
    Mockito.when(caderno.findByStudySessionIdOrderByPositionAsc(7L)).thenReturn(rows);
    Mockito.when(responses.findByStudySessionIdAndUserId(7L, 1L))
        .thenReturn(List.of(response(100L, u, q1, 7L, "A", true)));
    Mockito.when(questions.findAllById(anyList())).thenReturn(List.of(q1, q2));
    Mockito.when(review.getQueue(1L, 100, null, null, false)).thenReturn(queue(items));

    ReviewSessionResponse out = service.get(1L, 7L);

    assertEquals(2, out.totalItems());
    assertEquals(1L, out.answered());
    assertEquals(1L, out.unanswered());
    // Feedback imediato: revelado mesmo em andamento (REVISAO nunca oculta).
    assertTrue(out.items().get(0).answered());
    assertEquals("A", out.items().get(0).selectedOption());
    assertEquals(Boolean.TRUE, out.items().get(0).isCorrect());
    assertFalse(out.items().get(1).answered());
    assertNull(out.items().get(1).isCorrect());
  }

  @Test
  void getOnNonReviewSessionIs404() {
    User u = user(1L, true);
    // Sessão ESTUDO com o mesmo id: existe, mas não é revisão.
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(sessions.findByIdAndUserId(7L, 1L))
        .thenReturn(Optional.of(session(7L, "ESTUDO", "IN_PROGRESS")));
    Mockito.when(caderno.findByStudySessionIdOrderByPositionAsc(7L)).thenReturn(List.of());

    assertThrows(ResourceNotFoundException.class, () -> service.get(1L, 7L));
  }

  @Test
  void getOnOtherUsersSessionIs404() {
    User u = user(1L, true);
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(sessions.findByIdAndUserId(9L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.get(1L, 9L));
  }

  @Test
  void resultWhileInProgressIs409() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Question q1 = question(1L, mat, 2026, 1);
    StudySession s = session(7L, "REVISAO", "IN_PROGRESS");

    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(sessions.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(s));
    Mockito.when(caderno.findByStudySessionIdOrderByPositionAsc(7L))
        .thenReturn(List.of(row(7L, 1, q1)));

    assertThrows(ConflictException.class, () -> service.getResult(1L, 7L));
  }

  @Test
  void resultAfterFinishScoresLastAttemptPerQuestion() {
    User u = user(1L, true);
    Discipline mat = discipline(1L, "MATEMATICA", "Matemática");
    Question q1 = question(1L, mat, 2026, 1);
    Question q2 = question(2L, mat, 2026, 2);
    StudySession s = session(7L, "REVISAO", "FINISHED");
    s.setFinishedAt(OffsetDateTime.parse("2026-10-01T12:00:00Z"));

    Mockito.when(users.findById(1L)).thenReturn(Optional.of(u));
    Mockito.when(sessions.findByIdAndUserId(7L, 1L)).thenReturn(Optional.of(s));
    Mockito.when(caderno.findByStudySessionIdOrderByPositionAsc(7L))
        .thenReturn(List.of(row(7L, 1, q1), row(7L, 2, q2)));
    Mockito.when(responses.findByStudySessionIdAndUserId(7L, 1L))
        .thenReturn(List.of(response(100L, u, q1, 7L, "A", true)));
    Mockito.when(questions.findAllById(anyList())).thenReturn(List.of(q1, q2));

    ReviewSessionResultResponse out = service.getResult(1L, 7L);

    assertEquals(2L, out.total());
    assertEquals(1L, out.answered());
    assertEquals(1L, out.unanswered());
    assertEquals(1L, out.scored());
    assertEquals(1L, out.correct());
    assertEquals(1.0, out.accuracy());
    assertEquals(Boolean.TRUE, out.items().get(0).isCorrect());
    assertTrue(out.items().get(1).unanswered());
    assertNull(out.items().get(1).isCorrect());
  }
}
