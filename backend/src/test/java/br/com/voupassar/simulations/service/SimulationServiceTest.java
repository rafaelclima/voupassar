package br.com.voupassar.simulations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Exam;
import br.com.voupassar.exams.entity.ExamEssayPrompt;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.ExamEssayPromptRepository;
import br.com.voupassar.exams.repository.ExamRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.simulations.dto.CreateDisciplineSimulationRequest;
import br.com.voupassar.simulations.dto.CreateEditionSimulationRequest;
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
  @Mock QuestionClassificationRepository classifications;
  @Mock ExamRepository exams;
  @Mock ExamEssayPromptRepository essayPrompts;
  @Mock br.com.voupassar.admin.service.TechMetrics metrics;

  private SimulationService service;

  @BeforeEach
  void setup() {
    service = new SimulationService(
        users, disciplines, questions, simulations, attempts, caderno, responses,
        classifications, exams, essayPrompts, new Random(42), metrics);
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
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", null, "OFFICIAL"))
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
        new CreateDisciplineSimulationRequest("matematica", 3, null, "prova", null));

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
        new CreateDisciplineSimulationRequest("FISICA", 5, null, "PROVA", null)));
  }

  @Test
  void createBeyondAvailableIs400WithAvailable() {
    activeUser();
    stubMat(mat());
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", "DIFICIL", "OFFICIAL"))
        .thenReturn(List.of(21L, 22L));

    BadRequestException ex = assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 10, "DIFICIL", "ESTUDO", null)));
    assertEquals("INSUFFICIENT_QUESTIONS", ex.getCode());
    assertTrue(ex.getMessage().contains("2"));
  }

  @Test
  void createWithInvalidModeDifficultyAndCountIs400() {
    activeUser();
    stubMat(mat());

    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 5, null, "REVISAO", null)));
    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 5, "FACILIMA", "PROVA", null)));
    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 0, null, "PROVA", null)));
    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 101, null, "PROVA", null)));
  }

  // ---- origem (TASK 15.4) ----

  @Test
  void createWithoutSourceTypeDefaultsToOfficial() {
    activeUser();
    Discipline d = mat();
    stubMat(d);
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", null, "OFFICIAL"))
        .thenReturn(List.of(21L, 22L));
    when(questions.findAllById(any()))
        .thenReturn(List.of(question(21L, d, "A", false), question(22L, d, "B", false)));
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
        new CreateDisciplineSimulationRequest("MATEMATICA", 2, null, "PROVA", null));

    assertEquals(2, out.questionCount());
    verify(questions).findCandidateIdsByDiscipline("MATEMATICA", null, "OFFICIAL");
    ArgumentCaptor<Simulation> simCap = ArgumentCaptor.forClass(Simulation.class);
    verify(simulations).save(simCap.capture());
    assertTrue(simCap.getValue().getFilterJson().contains("\"sourceType\":\"OFFICIAL\""));
  }

  @Test
  void createWithAuthoralSourceTypeFiltersBank() {
    activeUser();
    Discipline d = mat();
    stubMat(d);
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", null, "AUTHORAL"))
        .thenReturn(List.of(31L));
    when(questions.findAllById(any())).thenReturn(List.of(question(31L, d, "C", false)));
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
        new CreateDisciplineSimulationRequest("MATEMATICA", 1, null, "PROVA", "authoral"));

    assertEquals(1, out.questionCount());
    verify(questions).findCandidateIdsByDiscipline("MATEMATICA", null, "AUTHORAL");
    ArgumentCaptor<Simulation> simCap = ArgumentCaptor.forClass(Simulation.class);
    verify(simulations, atLeastOnce()).save(simCap.capture());
    assertTrue(simCap.getValue().getFilterJson().contains("\"sourceType\":\"AUTHORAL\""));
  }

  @Test
  void createWithAllSourceTypeDisablesFilter() {
    activeUser();
    Discipline d = mat();
    stubMat(d);
    when(questions.findCandidateIdsByDiscipline("MATEMATICA", null, null))
        .thenReturn(List.of(21L));
    when(questions.findAllById(any())).thenReturn(List.of(question(21L, d, "A", false)));
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
        new CreateDisciplineSimulationRequest("MATEMATICA", 1, null, "PROVA", "ALL"));

    assertEquals(1, out.questionCount());
    verify(questions).findCandidateIdsByDiscipline("MATEMATICA", null, null);
  }

  @Test
  void createWithInvalidSourceTypeIs400() {
    activeUser();
    stubMat(mat());

    assertThrows(BadRequestException.class, () -> service.createByDiscipline(1L,
        new CreateDisciplineSimulationRequest("MATEMATICA", 5, null, "PROVA", "FALSIFICADA")));
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

  // ---- feedback imediato (TASK 5.2) ----

  private static Question explainedQuestion(
      long id, Discipline d, String answerKey, boolean annulled) {
    return question(id, d, answerKey, annulled);
  }

  private static QuestionClassification classification(
      long id, Question q, Topic topic, Subtopic subtopic) {
    QuestionClassification c = new QuestionClassification();
    ReflectionTestUtils.setField(c, "id", id);
    ReflectionTestUtils.setField(c, "question", q);
    ReflectionTestUtils.setField(c, "taxonomyVersion", "v1.1");
    ReflectionTestUtils.setField(c, "topic", topic);
    ReflectionTestUtils.setField(c, "subtopic", subtopic);
    ReflectionTestUtils.setField(c, "confidence", "ALTA");
    return c;
  }

  private static Topic topic(long id, String code, String name) {
    Topic t = new Topic();
    ReflectionTestUtils.setField(t, "id", id);
    ReflectionTestUtils.setField(t, "code", code);
    ReflectionTestUtils.setField(t, "name", name);
    return t;
  }

  private static Subtopic subtopic(long id, Topic topic, String code, String name) {
    Subtopic s = new Subtopic();
    ReflectionTestUtils.setField(s, "id", id);
    ReflectionTestUtils.setField(s, "topic", topic);
    ReflectionTestUtils.setField(s, "code", code);
    ReflectionTestUtils.setField(s, "name", name);
    return s;
  }

  private static QuestionAttempt estudoResponse(
      long id, Question q, User u, long simAttemptId,
      String selected, Boolean correct, boolean annulled, String answeredAt) {
    QuestionAttempt a = response(id, q, u, simAttemptId, selected, correct, annulled, answeredAt);
    a.setMode("ESTUDO");
    return a;
  }

  @Test
  void estudoFeedbackRevealsFrozenKeyAndTopic() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 2 questões [ESTUDO]");
    Question q1 = explainedQuestion(21L, d, "C", false);
    Question q2 = explainedQuestion(22L, d, "B", false);
    Topic t = topic(3L, "PORCENTAGEM", "Porcentagem");
    Subtopic st = subtopic(11L, t, "CALCULO_PERCENTUAL", "Cálculo percentual");
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "C"), row(55L, 2, q2, "B")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        estudoResponse(101L, q1, u, 55L, "C", true, false, "2026-10-01T10:05:00Z")));
    when(classifications.findActiveByQuestionId(21L))
        .thenReturn(List.of(classification(9L, q1, t, st)));

    var out = service.getStudyFeedback(1L, 55L, 1);

    assertEquals(55L, out.attemptId());
    assertEquals(1, out.position());
    assertEquals(21L, out.questionId());
    assertEquals("C", out.selectedOption());
    assertEquals(Boolean.TRUE, out.isCorrect());
    assertEquals("C", out.correctAnswer());
    assertEquals("PORCENTAGEM", out.topicCode());
    assertEquals("Porcentagem", out.topicName());
    assertEquals("CALCULO_PERCENTUAL", out.subtopicCode());
    assertEquals("ALTA", out.classificationConfidence());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("congelado")));
  }

  @Test
  void feedbackUsesFrozenKeyWhenCurrentKeyChanged() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 1 questão [ESTUDO]");
    // Gabarito "atual" mudou para D após a criação; o caderno congelou B.
    Question q1 = explainedQuestion(21L, d, "D", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "B")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        estudoResponse(101L, q1, u, 55L, "B", false, false, "2026-10-01T10:05:00Z")));
    when(classifications.findActiveByQuestionId(21L)).thenReturn(List.of());

    var out = service.getStudyFeedback(1L, 55L, 1);

    assertEquals(Boolean.TRUE, out.isCorrect());
    assertEquals("B", out.correctAnswer());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("congelado")));
  }

  @Test
  void feedbackUsesLatestAttemptPerQuestion() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 1 questão [ESTUDO]");
    Question q1 = explainedQuestion(21L, d, "C", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "C")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    // Errou e depois acertou: vale a última.
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        estudoResponse(101L, q1, u, 55L, "A", false, false, "2026-10-01T10:05:00Z"),
        estudoResponse(102L, q1, u, 55L, "C", true, false, "2026-10-01T10:06:00Z")));
    when(classifications.findActiveByQuestionId(21L)).thenReturn(List.of());

    var out = service.getStudyFeedback(1L, 55L, 1);

    assertEquals("C", out.selectedOption());
    assertEquals(Boolean.TRUE, out.isCorrect());
  }

  @Test
  void provaInProgressFeedbackIs409() {
    activeUser();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 2 questões [PROVA]");
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "PROVA", "IN_PROGRESS")));

    ConflictException ex =
        assertThrows(ConflictException.class, () -> service.getStudyFeedback(1L, 55L, 1));
    assertEquals("STUDY_FEEDBACK_UNAVAILABLE", ex.getCode());
  }

  @Test
  void provaFinishedFeedbackIs200() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 1 questão [PROVA]");
    Question q1 = explainedQuestion(21L, d, "B", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "PROVA", "SUBMITTED")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "B")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        response(101L, q1, u, 55L, "D", false, false, "2026-10-01T10:05:00Z")));
    when(classifications.findActiveByQuestionId(21L)).thenReturn(List.of());

    var out = service.getStudyFeedback(1L, 55L, 1);

    assertEquals("D", out.selectedOption());
    assertEquals(Boolean.FALSE, out.isCorrect());
    assertEquals("B", out.correctAnswer());
  }

  @Test
  void feedbackWithoutAnswerIs409() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 2 questões [ESTUDO]");
    Question q1 = explainedQuestion(21L, d, "A", false);
    Question q2 = explainedQuestion(22L, d, "B", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "A"), row(55L, 2, q2, "B")));
    when(questions.findAllById(any())).thenReturn(List.of(q1, q2));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of());

    ConflictException ex =
        assertThrows(ConflictException.class, () -> service.getStudyFeedback(1L, 55L, 2));
    assertEquals("FEEDBACK_NOT_AVAILABLE", ex.getCode());
  }

  @Test
  void unknownPositionIs404AndInvalidPositionIs400() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 1 questão [ESTUDO]");
    Question q1 = explainedQuestion(21L, d, "A", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "A")));

    assertThrows(ResourceNotFoundException.class, () -> service.getStudyFeedback(1L, 55L, 9));
    assertThrows(BadRequestException.class, () -> service.getStudyFeedback(1L, 55L, 0));
  }

  @Test
  void foreignFeedbackAttemptIs404() {
    activeUser();
    when(attempts.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> service.getStudyFeedback(1L, 99L, 1));
  }

  @Test
  void annulledFeedbackHasNullCorrect() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 1 questão [ESTUDO]");
    Question q1 = explainedQuestion(21L, d, "X", true);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "X")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    QuestionAttempt a =
        estudoResponse(101L, q1, u, 55L, "A", null, true, "2026-10-01T10:05:00Z");
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(a));
    when(classifications.findActiveByQuestionId(21L)).thenReturn(List.of());

    var out = service.getStudyFeedback(1L, 55L, 1);

    assertTrue(out.wasAnnulled());
    assertNull(out.isCorrect());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("anulada")));
  }

  // ---- simulado real por edição (TASK 5.5) ----

  private static Discipline lp() {
    return discipline(1L, "LINGUA_PORTUGUESA", "Língua Portuguesa");
  }

  private static Exam editionExam(
      long id, int year, String edital, int objective, int lpCount, int matCount) {
    Exam e = new Exam();
    ReflectionTestUtils.setField(e, "id", id);
    ReflectionTestUtils.setField(e, "year", (short) year);
    ReflectionTestUtils.setField(e, "edital", edital);
    ReflectionTestUtils.setField(e, "objectiveCount", (short) objective);
    ReflectionTestUtils.setField(e, "lpCount", (short) lpCount);
    ReflectionTestUtils.setField(e, "matCount", (short) matCount);
    ReflectionTestUtils.setField(e, "hasEssay", true);
    ReflectionTestUtils.setField(e, "scoringRule", null);
    return e;
  }

  private static Question editionQuestion(
      long id, Discipline d, int year, int number, String answerKey, boolean annulled) {
    Question q = question(id, d, answerKey, annulled);
    ReflectionTestUtils.setField(q, "sourceYear", (short) year);
    ReflectionTestUtils.setField(q, "sourceQuestionNumber", (short) number);
    return q;
  }

  private void stubEdition(Exam exam, List<Question> board) {
    when(exams.findByInstitutionAndYear("IFRN", exam.getYear())).thenReturn(Optional.of(exam));
    when(questions.findByEditionOrdered("IFRN", exam.getYear())).thenReturn(board);
    when(essayPrompts.findByExamInstitutionAndYear("IFRN", exam.getYear())).thenReturn(Optional.empty());
  }

  private void stubCreationIds(long simulationId, long attemptId) {
    when(simulations.save(any(Simulation.class))).thenAnswer(inv -> {
      Simulation s = inv.getArgument(0);
      ReflectionTestUtils.setField(s, "id", simulationId);
      return s;
    });
    when(attempts.save(any(SimulationAttempt.class))).thenAnswer(inv -> {
      SimulationAttempt a = inv.getArgument(0);
      ReflectionTestUtils.setField(a, "id", attemptId);
      return a;
    });
    when(caderno.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
  }

  @Test
  void createByEditionFreezesFullBoardInOriginalOrder() {
    activeUser();
    Discipline lp = lp();
    Discipline mat = mat();
    Exam exam = editionExam(9L, 2026, "48/2025", 4, 2, 2);
    List<Question> board = List.of(
        editionQuestion(101L, lp, 2026, 1, "A", false),
        editionQuestion(102L, lp, 2026, 2, "B", false),
        editionQuestion(103L, mat, 2026, 3, "C", false),
        editionQuestion(104L, mat, 2026, 4, "D", false));
    stubEdition(exam, board);
    stubCreationIds(8L, 56L);

    var out = service.createByEdition(1L, new CreateEditionSimulationRequest(2026, "prova"));

    assertEquals(56L, out.attemptId());
    assertEquals("REAL_EDITION", out.type());
    assertEquals("PROVA", out.mode());
    assertEquals("IN_PROGRESS", out.status());
    assertEquals(4, out.questionCount());
    assertEquals(List.of(1, 2, 3, 4),
        out.questions().stream().map(q -> q.sourceQuestionNumber()).toList());
    assertEquals(List.of(1, 2, 3, 4),
        out.questions().stream().map(q -> q.position()).toList());
    // Caderno misto: sem disciplina única no cabeçalho (só por posição).
    assertNull(out.disciplineCode());
    assertNull(out.disciplineName());
    assertEquals("LINGUA_PORTUGUESA", out.questions().get(0).disciplineCode());
    assertEquals("MATEMATICA", out.questions().get(3).disciplineCode());
    // Gabarito oculto em andamento.
    assertTrue(out.questions().stream().allMatch(q -> q.frozenAnswerKey() == null));
    assertNull(out.score());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("2026") && n.contains("ordem original")));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("48/2025")));

    ArgumentCaptor<Simulation> simCap = ArgumentCaptor.forClass(Simulation.class);
    verify(simulations).save(simCap.capture());
    assertEquals("REAL_EDITION", simCap.getValue().getType());
    assertEquals(9L, simCap.getValue().getExamId());

    ArgumentCaptor<List<SimulationQuestion>> rowsCap = ArgumentCaptor.forClass(List.class);
    verify(caderno).saveAll(rowsCap.capture());
    assertEquals(4, rowsCap.getValue().size());
    assertEquals(List.of("A", "B", "C", "D"),
        rowsCap.getValue().stream().map(SimulationQuestion::getFrozenAnswerKey).toList());
  }

  @Test
  void createByEditionKeepsAnnulledInOriginalPosition() {
    activeUser();
    Discipline lp = lp();
    Exam exam = editionExam(9L, 2020, "29/2019", 3, 2, 1);
    List<Question> board = List.of(
        editionQuestion(101L, lp, 2020, 1, "A", false),
        editionQuestion(102L, lp, 2020, 2, "X", true),
        editionQuestion(103L, mat(), 2020, 3, "C", false));
    stubEdition(exam, board);
    stubCreationIds(8L, 57L);

    var out = service.createByEdition(1L, new CreateEditionSimulationRequest(2020, "ESTUDO"));

    assertEquals(3, out.questionCount());
    assertEquals(3, out.questions().size());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("DESCONHECIDA")));

    ArgumentCaptor<List<SimulationQuestion>> rowsCap = ArgumentCaptor.forClass(List.class);
    verify(caderno).saveAll(rowsCap.capture());
    assertEquals(List.of("A", "X", "C"),
        rowsCap.getValue().stream().map(SimulationQuestion::getFrozenAnswerKey).toList());
  }

  @Test
  void createByEditionMentionsEssayPrompt() {
    activeUser();
    Exam exam = editionExam(9L, 2026, "48/2025", 2, 1, 1);
    List<Question> board = List.of(
        editionQuestion(101L, lp(), 2026, 1, "A", false),
        editionQuestion(102L, mat(), 2026, 2, "B", false));
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(exam));
    when(questions.findByEditionOrdered("IFRN", (short) 2026)).thenReturn(board);
    ExamEssayPrompt prompt = new ExamEssayPrompt();
    ReflectionTestUtils.setField(prompt, "genre", "artigo de opinião");
    ReflectionTestUtils.setField(prompt, "theme", "mudanças climáticas");
    ReflectionTestUtils.setField(prompt, "pseudonym", "Amazonino Belém");
    when(essayPrompts.findByExamInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(prompt));
    stubCreationIds(8L, 58L);

    var out = service.createByEdition(1L, new CreateEditionSimulationRequest(2026, "ESTUDO"));

    assertTrue(out.notes().stream().anyMatch(n -> n.contains("mudanças climáticas")));
  }

  @Test
  void createByEditionUnknownYearIs404() {
    activeUser();
    when(exams.findByInstitutionAndYear("IFRN", (short) 2030)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2030, "PROVA")));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
  }

  @Test
  void createByEditionMissing2021ExplainsAbsence() {
    activeUser();
    when(exams.findByInstitutionAndYear("IFRN", (short) 2021)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2021, "PROVA")));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("ausente"));
  }

  @Test
  void createByEditionWithIncompleteBoardIs409() {
    activeUser();
    Exam exam = editionExam(9L, 2026, "48/2025", 4, 2, 2);
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(exam));
    // Total divergente da capa.
    when(questions.findByEditionOrdered("IFRN", (short) 2026)).thenReturn(List.of(
        editionQuestion(101L, lp(), 2026, 1, "A", false),
        editionQuestion(102L, lp(), 2026, 2, "B", false),
        editionQuestion(103L, mat(), 2026, 3, "C", false)));

    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2026, "PROVA")));
    assertEquals("INCOMPLETE_EDITION", ex.getCode());
  }

  @Test
  void createByEditionWithNumberGapIs409() {
    activeUser();
    Exam exam = editionExam(9L, 2026, "48/2025", 4, 2, 2);
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(exam));
    // Falta a questão 3 (lacuna na numeração).
    when(questions.findByEditionOrdered("IFRN", (short) 2026)).thenReturn(List.of(
        editionQuestion(101L, lp(), 2026, 1, "A", false),
        editionQuestion(102L, lp(), 2026, 2, "B", false),
        editionQuestion(103L, mat(), 2026, 4, "C", false),
        editionQuestion(104L, mat(), 2026, 5, "D", false)));

    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2026, "PROVA")));
    assertEquals("INCOMPLETE_EDITION", ex.getCode());
  }

  @Test
  void createByEditionWithDisciplineMismatchIs409() {
    activeUser();
    Exam exam = editionExam(9L, 2026, "48/2025", 4, 2, 2);
    when(exams.findByInstitutionAndYear("IFRN", (short) 2026)).thenReturn(Optional.of(exam));
    // 3 LP + 1 MAT contra 2 + 2 da capa.
    when(questions.findByEditionOrdered("IFRN", (short) 2026)).thenReturn(List.of(
        editionQuestion(101L, lp(), 2026, 1, "A", false),
        editionQuestion(102L, lp(), 2026, 2, "B", false),
        editionQuestion(103L, lp(), 2026, 3, "C", false),
        editionQuestion(104L, mat(), 2026, 4, "D", false)));

    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2026, "PROVA")));
    assertEquals("INCOMPLETE_EDITION", ex.getCode());
  }

  @Test
  void createByEditionWithInvalidModeAndYearIs400() {
    activeUser();

    assertThrows(BadRequestException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2026, "REVISAO")));
    assertThrows(BadRequestException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(null, "PROVA")));
    assertThrows(BadRequestException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(1999, "PROVA")));
  }

  // ---- TASK E.1: simulado real por (institution, year) ----

  private static Exam eajEditionExam(
      long id, int year, int objective, int lp, int mat, int cn, int ch) {
    Exam e = new Exam();
    ReflectionTestUtils.setField(e, "id", id);
    ReflectionTestUtils.setField(e, "institution", "EAJ");
    ReflectionTestUtils.setField(e, "year", (short) year);
    ReflectionTestUtils.setField(e, "edital", "DESCONHECIDO");
    ReflectionTestUtils.setField(e, "objectiveCount", (short) objective);
    ReflectionTestUtils.setField(e, "lpCount", (short) lp);
    ReflectionTestUtils.setField(e, "matCount", (short) mat);
    ReflectionTestUtils.setField(e, "cnCount", (short) cn);
    ReflectionTestUtils.setField(e, "chCount", (short) ch);
    ReflectionTestUtils.setField(e, "durationMinutes", (short) 180);
    ReflectionTestUtils.setField(e, "hasEssay", false);
    ReflectionTestUtils.setField(e, "scoringRule", null);
    return e;
  }

  private static Discipline cn() {
    return discipline(3L, "CIENCIAS_NATUREZA", "Ciências da Natureza");
  }

  private static Discipline ch() {
    return discipline(4L, "CIENCIAS_HUMANAS", "Ciências Humanas");
  }

  @Test
  void createByEditionEaj2022DiffersFromIfrn2022() {
    activeUser();
    Exam fullExam = eajEditionExam(20L, 2022, 40, 20, 20, 0, 0);
    List<Question> full = new ArrayList<>();
    for (int n = 1; n <= 40; n++) {
      full.add(editionQuestion(200L + n, n <= 20 ? lp() : mat(), 2022, n, "A", false));
    }
    when(exams.findByInstitutionAndYear("EAJ", (short) 2022)).thenReturn(Optional.of(fullExam));
    when(questions.findByEditionOrdered("EAJ", (short) 2022)).thenReturn(full);
    stubCreationIds(8L, 60L);

    var out = service.createByEdition(1L, new CreateEditionSimulationRequest(2022, "PROVA", "EAJ"));

    assertEquals(40, out.questionCount());
    assertTrue(out.title().contains("EAJ 2022"));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("EAJ 2022")));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("TRANSCRIBED_FROM_MD")));
  }

  @Test
  void createByEditionEaj2021With50In4Areas() {
    activeUser();
    Exam eaj2021 = eajEditionExam(21L, 2021, 50, 15, 15, 12, 8);
    List<Question> full = new ArrayList<>();
    for (int n = 1; n <= 50; n++) {
      Discipline d = n <= 15 ? lp() : n <= 30 ? mat() : n <= 42 ? cn() : ch();
      full.add(editionQuestion(300L + n, d, 2021, n, "A", false));
    }
    when(exams.findByInstitutionAndYear("EAJ", (short) 2021)).thenReturn(Optional.of(eaj2021));
    when(questions.findByEditionOrdered("EAJ", (short) 2021)).thenReturn(full);
    stubCreationIds(8L, 61L);

    var out = service.createByEdition(1L, new CreateEditionSimulationRequest(2021, "ESTUDO", "EAJ"));

    assertEquals(50, out.questionCount());
    assertTrue(out.title().contains("EAJ 2021"));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("CN 12 + CH 8")));
  }

  @Test
  void createByEditionEaj2023IsNotFound() {
    activeUser();
    when(exams.findByInstitutionAndYear("EAJ", (short) 2023)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2023, "PROVA", "EAJ")));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("EAJ 2023"));
  }

  @Test
  void createByEditionIfrn2021PointsToEaj() {
    activeUser();
    when(exams.findByInstitutionAndYear("IFRN", (short) 2021)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2021, "PROVA", "IFRN")));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
    assertTrue(ex.getMessage().contains("EAJ-2021"));
  }

  @Test
  void createByEditionInvalidInstitutionIs400() {
    activeUser();

    assertThrows(BadRequestException.class,
        () -> service.createByEdition(1L, new CreateEditionSimulationRequest(2022, "PROVA", "XXX")));
  }

  @Test
  void realEditionAttemptHidesSingleDiscipline() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(8L, "Simulado Edição 2026 — 40 questões [PROVA]");
    s.setType("REAL_EDITION");
    Question q1 = question(101L, d, "A", false);
    when(attempts.findByIdAndUserId(56L, 1L))
        .thenReturn(Optional.of(attempt(56L, s, u, "PROVA", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(56L))
        .thenReturn(List.of(row(56L, 1, q1, "A")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    when(responses.findBySimulationAttemptIdAndUserId(56L, 1L)).thenReturn(List.of());

    var out = service.getAttempt(1L, 56L);

    assertEquals("REAL_EDITION", out.type());
    assertNull(out.disciplineCode());
    assertEquals("MATEMATICA", out.questions().get(0).disciplineCode());
  }

  @Test
  void estudoFeedbackWithoutClassificationMarksUnconfirmed() {
    activeUser();
    Discipline d = mat();
    User u = user(1L, true);
    Simulation s = simulation(7L, "Simulado Matemática — 1 questão [ESTUDO]");
    Question q1 = explainedQuestion(21L, d, "A", false);
    when(attempts.findByIdAndUserId(55L, 1L))
        .thenReturn(Optional.of(attempt(55L, s, u, "ESTUDO", "IN_PROGRESS")));
    when(caderno.findBySimulationAttemptIdOrderByPositionAsc(55L))
        .thenReturn(List.of(row(55L, 1, q1, "A")));
    when(questions.findAllById(any())).thenReturn(List.of(q1));
    when(responses.findBySimulationAttemptIdAndUserId(55L, 1L)).thenReturn(List.of(
        estudoResponse(101L, q1, u, 55L, "A", true, false, "2026-10-01T10:05:00Z")));
    when(classifications.findActiveByQuestionId(21L)).thenReturn(List.of());

    var out = service.getStudyFeedback(1L, 55L, 1);

    assertNull(out.topicCode());
    assertNull(out.classificationConfidence());
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("NÃO CONFIRMADO")));
  }
}
