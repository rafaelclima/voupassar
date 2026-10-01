package br.com.voupassar.simulations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.simulations.dto.CreateDisciplineSimulationRequest;
import br.com.voupassar.simulations.entity.Simulation;
import br.com.voupassar.simulations.entity.SimulationAttempt;
import br.com.voupassar.simulations.entity.SimulationQuestion;
import br.com.voupassar.simulations.repository.SimulationAttemptRepository;
import br.com.voupassar.simulations.repository.SimulationQuestionRepository;
import br.com.voupassar.simulations.repository.SimulationRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Regras do SimulationService (TASK 5.1) com mocks — sem banco.
 *
 * <p>Seleção sem reposição sobre candidatas não-anuladas, caderno congelado
 * com gabarito oculto em andamento, placar do servidor ao encerrar (última
 * tentativa por questão), posse escopada ao dono e ciclo
 * IN_PROGRESS → SUBMITTED/ABANDONED.
 */
@ExtendWith(MockitoExtension.class)
class SimulationServiceTest {

  @Mock UserRepository users;
  @Mock DisciplineRepository disciplines;
  @Mock QuestionRepository questions;
  @Mock SimulationRepository simulations;
  @Mock SimulationAttemptRepository attempts;
  @Mock SimulationQuestionRepository caderno;
  @Mock QuestionAttemptRepository responses;

  private SimulationService service;

  @BeforeEach
  void setup() {
    service = new SimulationService(
        users, disciplines, questions, simulations, attempts, caderno, responses, new Random(42));
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

  private static Question question(long id, Discipline d, String answerKey, boolean annulled) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "discipline", d);
    ReflectionTestUtils.setField(q, "answerKey", answerKey);
    ReflectionTestUtils.setField(q, "annulled", annulled);
    ReflectionTestUtils.setField(q, "sourceYear", (short) 2026);
    ReflectionTestUtils.setField(q, "sourceQuestionNumber", (short) id);
    return q;
  }

  private static Simulation simulation(long id, String title) {
    Simulation s = new Simulation();
    ReflectionTestUtils.setField(s, "id", id);
    s.setType("BY_DISCIPLINE");
    s.setTitle(title);
    return s;
  }

  private static SimulationAttempt attempt(long id, Simulation s, User u, String mode, String status) {
    SimulationAttempt a = new SimulationAttempt();
    ReflectionTestUtils.setField(a, "id", id);
    a.setSimulation(s);
    a.setUser(u);
    a.setMode(mode);
    a.setStatus(status);
    a.setStartedAt(OffsetDateTime.parse("2026-10-01T10:00:00Z"));
    return a;
  }

  private static SimulationQuestion row(long attemptId, int position, Question q, String frozen) {
    SimulationQuestion r = new SimulationQuestion();
    r.setSimulationAttemptId(attemptId);
    r.setPosition((short) position);
    r.setQuestion(q);
    r.setFrozenAnswerKey(frozen);
    return r;
  }

  private static QuestionAttempt response(
      long id, Question q, User u, long simAttemptId,
      String selected, Boolean correct, boolean annulled, String answeredAt) {
    QuestionAttempt a = new QuestionAttempt();
    ReflectionTestUtils.setField(a, "id", id);
    a.setUser(u);
    a.setQuestion(q);
    a.setSimulationAttemptId(simAttemptId);
    a.setSelectedOption(selected);
    a.setCorrect(correct);
    a.setAnnulled(annulled);
    a.setMode("PROVA");
    a.setAnsweredAt(OffsetDateTime.parse(answeredAt));
    return a;
  }

  private void activeUser() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
  }

  private Discipline mat() {
    return discipline(2L, "MATEMATICA", "Matemática");
  }

  private void stubMat(Discipline d) {
    when(disciplines.findByCode("MATEMATICA")).thenReturn(Optional.of(d));
  }

  // ---- criação ----

  @Test
  void createFreezesCadernoWithoutRevealingKey() {
    activeUser();
    Discipline d = mat();
    stubMat(d);
    List<Question> bank = List.of(
        question(21L, d, "A", false), question(22L, d, "B", false),
        question(23L, d, "C", false), question(24L, d, "D", false));
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", null))
        .thenReturn(List.of(21L, 22L, 23L, 24L));
    when(questions.findAllById(any())).thenReturn(bank);
    when(simulations.save(any(Simulation.class))).thenAnswer(inv -> {
      Simulation s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", 7L);
      return s;
    });
    when(attempts.save(any(SimulationAttempt.class))).thenAnswer(inv -> {
      SimulationAttempt a = inv.getArgument(0);
      ReflectionTestUtils.setField(a, "id", 55L);
      return a;
    });
    when(caderno.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

    var out = service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("matematica", 3, null, "prova"));

    assertEquals(55L, out.attemptId());
    assertEquals("BY_DISCIPLINE", out.type());
    assertEquals("PROVA", out.mode());
    assertEquals("IN_PROGRESS", out.status());
    assertEquals(3, out.questionCount());
    assertEquals(3, out.questions().size());
    assertEquals(List.of(1, 2, 3), out.questions().stream().map(q -> q.position()).toList());
    // Sem reposição: 3 ids distintos dentre os 4 candidatos.
    assertEquals(3, out.questions().stream().map(q -> q.questionId()).distinct().count());
    // Gabarito oculto em andamento.
    assertTrue(out.questions().stream().allMatch(q -> q.frozenAnswerKey() == null));
    assertTrue(out.questions().stream().allMatch(q -> q.isCorrect() == null));
    assertNull(out.score());

    ArgumentCaptor<Simulation> simCap = ArgumentCaptor.forClass(Simulation.class);
    verify(simulations).save(simCap.capture());
    assertEquals("BY_DISCIPLINE", simCap.getValue().getType());
    assertNull(simCap.getValue().getExamId());

    ArgumentCaptor<List<SimulationQuestion>> rowsCap = ArgumentCaptor.forClass(List.class);
    verify(caderno).saveAll(rowsCap.capture());
    assertEquals(3, rowsCap.getValue().size());
  }

  @Test
  void createWithUnknownDisciplineIs404() {
    activeUser();
    when(disciplines.findByCode("FISICA")).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("FISICA", 5, null, "PROVA")));
  }

  @Test
  void createBeyondAvailableIs400WithAvailable() {
    activeUser();
    stubMat(mat());
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", "DIFICIL"))
        .thenReturn(List.of(21L, 22L));

    BadRequestException ex = assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 10, "DIFICIL", "ESTUDO")));
    assertEquals("INSUFFICIENT_QUESTIONS", ex.getCode());
    assertTrue(ex.getMessage().contains("2"));
  }

  @Test
  void createWithInvalidModeDifficultyAndCountIs400() {
    activeUser();
    stubMat(mat());

    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 5, null, "REVISAO")));
    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 5, "FACILIMA", "PROVA")));
    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 0, null, "PROVA")));
    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 101, null, "PROVA")));
  }

  // ---- consulta ----

  @Test
  void getInProgressHidesKeyButShowsAnsweredFlags() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 2 questões [PROVA]");
    Question q1 = question(21L, d, "A", false);
    Question q2 = question(22L, d, "B", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "PROVA", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "A"), row(55L, 2, q2, "B")));
    when(questions.findAllById(any())).thenReturn(List.of(q1, q2));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L))
        .thenReturn(List.of(response(101L, q1, u, 55L, "A", true, false, "2026-10-01T10:05:00Z")));

    var out = service.getAttempt(1L, 55L);

    assertEquals(2, out.questions().size());
    assertTrue(out.questions().get(0).answered());
    assertTrue(!out.questions().get(1).answered());
    assertTrue(out.questions().stream().allMatch(q -> q.frozenAnswerKey() == null));
    assertNull(out.score());
  }

  @Test
  void foreignAttemptIs404() {
    activeUser();
    when(attempts.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.getAttempt(1L, 99L));
  }

  // ---- conclusão ----

  @Test
  void submitScoresLatestPerQuestionAndKeepsUnansweredOut() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 3 questões [PROVA]");
    Question q1 = question(21L, d, "A", false);
    Question q2 = question(22L, d, "B", false);
    Question q3 = question(23L, d, "C", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "PROVA", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "A"), row(55L, 2, q2, "B"), row(55L, 3, q3, "C")));
    when(questions.findAllById(any())).thenReturn(List.of(q1, q2, q3));
    // q1: errou e depois acertou (vale a última); q2: acertou; q3: sem resposta.
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        response(101L, q1, u, 55L, "B", false, false, "2026-10-01T10:05:00Z"),
        response(102L, q1, u, 55L, "A", true, false, "2026-10-01T10:06:00Z"),
        response(103L, q2, u, 55L, "B", true, false, "2026-10-01T10:07:00Z")));
    when(attempts.save(any(SimulationAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

    var out = service.submitAttempt(1L, 55L);

    assertEquals("SUBMITTED", out.status());
    assertEquals(3L, out.total());
    assertEquals(2L, out.answered());
    assertEquals(1L, out.unanswered());
    assertEquals(2L, out.scored());
    assertEquals(2L, out.correct());
    assertEquals(0L, out.incorrect());
    assertEquals(1.0, out.accuracy());
    assertEquals(3, out.items().size());
    assertEquals("A", out.items().get(0).selectedOption());
    assertEquals(Boolean.TRUE, out.items().get(0).isCorrect());
    assertTrue(out.items().get(2).unanswered());
    assertNull(out.items().get(2).selectedOption());

    ArgumentCaptor<SimulationAttempt> cap = ArgumentCaptor.forClass(SimulationAttempt.class);
    verify(attempts).save(cap.capture());
    assertEquals("SUBMITTED", cap.getValue().getStatus());
    assertNotNull(cap.getValue().getSubmittedAt());
    assertNotNull(cap.getValue().getScoreJson());
  }

  @Test
  void submitClosedIs409AndResultInProgressIs409() {
    activeUser();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado");
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "PROVA", "SUBMITTED")));

    assertThrows(ConflictException.class, () -> service.submitAttempt(1L, 55L));
    assertThrows(ConflictException.class, () -> service.abandonAttempt(1L, 55L));
  }

  @Test
  void resultInProgressIs409() {
    activeUser();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado");
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "PROVA", "IN_PROGRESS")));

    ConflictException ex =
        assertThrows(ConflictException.class, () -> service.getResult(1L, 55L));
    assertEquals("SIMULATION_NOT_FINISHED", ex.getCode());
  }

  @Test
  void abandonKeepsPartialScore() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 2 questões [ESTUDO]");
    Question q1 = question(21L, d, "A", false);
    Question q2 = question(22L, d, "B", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "A"), row(55L, 2, q2, "B")));
    when(questions.findAllById(any())).thenReturn(List.of(q1, q2));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        response(101L, q1, u, 55L, "D", false, false, "2026-10-01T10:05:00Z")));
    when(attempts.save(any(SimulationAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

    var out = service.abandonAttempt(1L, 55L);

    assertEquals("ABANDONED", out.status());
    assertEquals(2L, out.total());
    assertEquals(1L, out.answered());
    assertEquals(1L, out.unanswered());
    assertEquals(1L, out.scored());
    assertEquals(0L, out.correct());
    assertEquals(0.0, out.accuracy());
  }

  // ---- lista ----

  @Test
  void listReturnsNewestFirstWithFrozenCount() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 2 questões [PROVA]");
    Question q1 = question(21L, d, "A", false);
    Question q2 = question(22L, d, "B", false);
    SimulationAttempt a = attempt(55L, s, u, "PROVA", "SUBMITTED");
    when(attempts.findByUserIdOrderByStartedAtDescIdDesc(1L, PageRequest.of(0, 20)))
        .thenReturn(new PageImpl<>(List.of(a), PageRequest.of(0, 20), 1));
    when(caderno.findBySimulationAttemptIdInOrderBySimulationAttemptIdAscPositionAsc(List.of(55L)))
        .thenReturn(new ArrayList<>(List.of(row(55L, 1, q1, "A"), row(55L, 2, q2, "B"))));
    when(questions.findAllById(any())).thenReturn(List.of(q1, q2));
    when(simulations.findAllById(any())).thenReturn(List.of(s));

    var out = service.listAttempts(1L, 0, 20);

    assertEquals(1L, out.totalElements());
    assertEquals(55L, out.content().get(0).attemptId());
    assertEquals("MATEMATICA", out.content().get(0).disciplineCode());
    assertEquals(2, out.content().get(0).questionCount());
  }

  @Test
  void listWithBadPaginationIs400() {
    activeUser();

    assertThrows(BadRequestException.class, () -> service.listAttempts(1L, -1, 20));
    assertThrows(BadRequestException.class, () -> service.listAttempts(1L, 0, 101));
  }
}
