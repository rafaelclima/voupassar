package br.com.voupassar.attempts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.attempts.dto.CreateAttemptRequest;
import br.com.voupassar.attempts.dto.CreateStudySessionRequest;
import br.com.voupassar.attempts.entity.SimulationAttemptRef;
import br.com.voupassar.attempts.entity.StudySession;
import br.com.voupassar.attempts.repository.SimulationAttemptRefRepository;
import br.com.voupassar.attempts.repository.StudySessionRepository;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.performance.service.PerformanceService;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
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
 * Regras do AttemptService (TASK 3.7) com mocks — sem banco.
 *
 * <p>Correção no servidor, vínculo obrigatório (sessão ou simulado), posse,
 * status e coerência de modo; anuladas com {@code isCorrect} NULL e nota
 * explícita; BLANK como erro; fato imutável (só POST/GET, sem PUT/DELETE).
 */
@ExtendWith(MockitoExtension.class)
class AttemptServiceTest {

  @Mock UserRepository users;
  @Mock QuestionRepository questions;
  @Mock QuestionAttemptRepository attempts;
  @Mock StudySessionRepository sessions;
  @Mock SimulationAttemptRefRepository simulations;
  @Mock PerformanceService performance;

  private AttemptService service;

  @BeforeEach
  void setup() {
    service = new AttemptService(users, questions, attempts, sessions, simulations, performance);
  }

  private static User user(long id, boolean active) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha-segura-123"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(active);
    return u;
  }

  private static Question question(long id, String answerKey, boolean annulled) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "discipline", new Discipline("MATEMATICA", "Matemática"));
    ReflectionTestUtils.setField(q, "answerKey", answerKey);
    ReflectionTestUtils.setField(q, "annulled", annulled);
    return q;
  }

  private static StudySession session(long id, long userId, String mode, String status) {
    StudySession s = new StudySession();
    ReflectionTestUtils.setField(s, "id", id);
    User u = user(userId, true);
    s.setUser(u);
    s.setMode(mode);
    s.setStatus(status);
    return s;
  }

  private static SimulationAttemptRef sim(long id, String mode, String status) {
    SimulationAttemptRef s = new SimulationAttemptRef();
    ReflectionTestUtils.setField(s, "id", id);
    ReflectionTestUtils.setField(s, "mode", mode);
    ReflectionTestUtils.setField(s, "status", status);
    return s;
  }

  private void activeUser() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
  }

  private void stubSessionSave(long id) {
    when(sessions.save(any(StudySession.class))).thenAnswer(inv -> {
      StudySession s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", id);
      return s;
    });
  }

  private void stubAttemptSave(long id) {
    when(attempts.save(any(QuestionAttempt.class))).thenAnswer(inv -> {
      QuestionAttempt a = inv.getArgument(0);
      ReflectionTestUtils.setField(a, "id", id);
      return a;
    });
  }

  // ---- sessões ----

  @Test
  void createSessionOpensInProgress() {
    activeUser();
    stubSessionSave(7L);

    var out = service.createSession(1L, new CreateStudySessionRequest("ESTUDO"));

    assertEquals("ESTUDO", out.mode());
    assertEquals("IN_PROGRESS", out.status());
    assertNotNull(out.startedAt());
  }

  @Test
  void finishSessionClosesOnce() {
    activeUser();
    when(sessions.findByIdAndUserId(7L, 1L))
        .thenReturn(Optional.of(session(7L, 1L, "ESTUDO", "IN_PROGRESS")));

    var out = service.finishSession(1L, 7L);

    assertEquals("FINISHED", out.status());
    assertNotNull(out.finishedAt());
    verify(sessions).save(any(StudySession.class));
  }

  @Test
  void finishClosedSessionIs409() {
    activeUser();
    when(sessions.findByIdAndUserId(7L, 1L))
        .thenReturn(Optional.of(session(7L, 1L, "ESTUDO", "FINISHED")));

    assertThrows(ConflictException.class, () -> service.finishSession(1L, 7L));
  }

  @Test
  void foreignSessionIs404() {
    activeUser();
    when(sessions.findByIdAndUserId(7L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.getSession(1L, 7L));
  }

  @Test
  void inactiveUserIs401() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, false)));

    assertThrows(UnauthorizedException.class,
        () -> service.createSession(1L, new CreateStudySessionRequest("ESTUDO")));
  }

  // ---- tentativas ----

  @Test
  void correctAnswerIsComputedServerSide() {
    activeUser();
    stubAttemptSave(101L);
    when(questions.findById(12L)).thenReturn(Optional.of(question(12L, "C", false)));
    when(sessions.findByIdAndUserId(7L, 1L))
        .thenReturn(Optional.of(session(7L, 1L, "ESTUDO", "IN_PROGRESS")));

    var out = service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "C", "ESTUDO", 95, 7L, null));

    assertEquals(12L, out.questionId());
    assertEquals("C", out.selectedOption());
    assertEquals(Boolean.TRUE, out.isCorrect());
    assertFalse(out.wasAnnulled());
    assertEquals(Long.valueOf(7L), out.studySessionId());
    assertNull(out.simulationAttemptId());

    ArgumentCaptor<QuestionAttempt> cap = ArgumentCaptor.forClass(QuestionAttempt.class);
    verify(attempts).save(cap.capture());
    assertEquals(Boolean.TRUE, cap.getValue().getCorrect());
    assertNotNull(cap.getValue().getAnsweredAt());
  }

  @Test
  void wrongAndBlankAreIncorrect() {
    activeUser();
    stubAttemptSave(101L);
    when(questions.findById(12L)).thenReturn(Optional.of(question(12L, "C", false)));
    when(sessions.findByIdAndUserId(7L, 1L))
        .thenReturn(Optional.of(session(7L, 1L, "ESTUDO", "IN_PROGRESS")));

    var wrong = service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "A", "ESTUDO", null, 7L, null));
    assertEquals(Boolean.FALSE, wrong.isCorrect());

    var blank = service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "BLANK", "ESTUDO", 0, 7L, null));
    assertEquals(Boolean.FALSE, blank.isCorrect());
    assertTrue(blank.notes().stream().anyMatch(n -> n.contains("branco")));
  }

  @Test
  void annulledHasNullCorrectAndNote() {
    activeUser();
    stubAttemptSave(101L);
    when(questions.findById(37L)).thenReturn(Optional.of(question(37L, "X", true)));
    when(sessions.findByIdAndUserId(7L, 1L))
        .thenReturn(Optional.of(session(7L, 1L, "ESTUDO", "IN_PROGRESS")));

    var out = service.submitAttempt(1L,
        new CreateAttemptRequest(37L, "A", "ESTUDO", 10, 7L, null));

    assertTrue(out.wasAnnulled());
    assertNull(out.isCorrect());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("anulada")));
  }

  @Test
  void orphanAttemptIs400AndUnknownQuestionIs404() {
    activeUser();
    when(questions.findById(12L)).thenReturn(Optional.of(question(12L, "C", false)));

    assertThrows(BadRequestException.class, () -> service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "C", "ESTUDO", null, null, null)));

    when(questions.findById(999L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.submitAttempt(1L,
        new CreateAttemptRequest(999L, "C", "ESTUDO", null, 7L, null)));
  }

  @Test
  void closedSessionAndModeMismatchAreRejected() {
    activeUser();
    when(questions.findById(12L)).thenReturn(Optional.of(question(12L, "C", false)));
    when(sessions.findByIdAndUserId(8L, 1L))
        .thenReturn(Optional.of(session(8L, 1L, "ESTUDO", "FINISHED")));
    when(sessions.findByIdAndUserId(9L, 1L))
        .thenReturn(Optional.of(session(9L, 1L, "PROVA", "IN_PROGRESS")));

    assertThrows(ConflictException.class, () -> service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "C", "ESTUDO", null, 8L, null)));
    assertThrows(BadRequestException.class, () -> service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "C", "ESTUDO", null, 9L, null)));
  }

  @Test
  void simulationLinkIsValidated() {
    activeUser();
    stubAttemptSave(101L);
    when(questions.findById(12L)).thenReturn(Optional.of(question(12L, "C", false)));
    when(simulations.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(sim(55L, "PROVA", "IN_PROGRESS")));

    var out = service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "c", "prova", 30, null, 55L));

    assertEquals(Boolean.TRUE, out.isCorrect());
    assertEquals("PROVA", out.mode());
    assertEquals(Long.valueOf(55L), out.simulationAttemptId());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("Fase 5")));
  }

  @Test
  void unknownSimulationIs404() {
    activeUser();
    when(questions.findById(12L)).thenReturn(Optional.of(question(12L, "C", false)));
    when(simulations.findByIdAndUserId(56L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.submitAttempt(1L,
        new CreateAttemptRequest(12L, "C", "PROVA", null, null, 56L)));
  }

  @Test
  void getAttemptIsScopedToOwner() {
    activeUser();
    Question q = question(12L, "C", false);
    QuestionAttempt a = new QuestionAttempt();
    ReflectionTestUtils.setField(a, "id", 101L);
    a.setQuestion(q);
    a.setSelectedOption("C");
    a.setCorrect(Boolean.TRUE);
    a.setAnnulled(false);
    a.setMode("ESTUDO");
    when(attempts.findByIdAndUserId(101L, 1L)).thenReturn(Optional.of(a));

    var out = service.getAttempt(1L, 101L);

    assertEquals(101L, out.id());
    assertEquals(Boolean.TRUE, out.isCorrect());

    when(attempts.findByIdAndUserId(102L, 1L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.getAttempt(1L, 102L));
  }
}
