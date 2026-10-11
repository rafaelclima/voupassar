package br.com.voupassar.aiprova;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.aiprova.dto.AiAnswerRequest;
import br.com.voupassar.aiprova.dto.AiAnswerResponse;
import br.com.voupassar.aiprova.dto.AiAttemptResponse;
import br.com.voupassar.aiprova.dto.AiFeedbackResponse;
import br.com.voupassar.aiprova.dto.AiGenerateRequest;
import br.com.voupassar.aiprova.dto.AiResultResponse;
import br.com.voupassar.aiprova.entity.AiGeneration;
import br.com.voupassar.aiprova.entity.AiGenerationAnswer;
import br.com.voupassar.aiprova.entity.AiGenerationQuestion;
import br.com.voupassar.aiprova.repository.AiGenerationAnswerRepository;
import br.com.voupassar.aiprova.repository.AiGenerationQuestionRepository;
import br.com.voupassar.aiprova.repository.AiGenerationRepository;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exams.entity.Exam;
import br.com.voupassar.exams.repository.ExamRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.simulations.entity.Simulation;
import br.com.voupassar.simulations.entity.SimulationAttempt;
import br.com.voupassar.simulations.repository.SimulationAttemptRepository;
import br.com.voupassar.simulations.repository.SimulationRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Regras do AiSimulationService (TASK A.3) com mocks — sem banco, sem rede
 * (provedor mockado real, determinístico).
 *
 * <p>Criação com caderno congelado e gabarito oculto, edição-modelo default
 * (mais recente da trilha) e explícita, erros honestos (400/404/409/422),
 * resposta com ocultação em PROVA, placar do servidor contra o congelado,
 * selo experimental em questão e resultado, e isolamento (rota-IA 404 para
 * execução oficial; nada-IA em {@code question_attempts}).
 */
@ExtendWith(MockitoExtension.class)
class AiSimulationServiceTest {

  @Mock UserRepository users;
  @Mock ExamRepository exams;
  @Mock SimulationRepository simulations;
  @Mock SimulationAttemptRepository attempts;
  @Mock AiGenerationRepository generations;
  @Mock AiGenerationQuestionRepository cadernoIa;
  @Mock AiGenerationAnswerRepository answersIa;

  private AiSimulationService service;
  private final AiMockProvider mockProvider = new AiMockProvider();

  @BeforeEach
  void setup() {
    service = new AiSimulationService(users, exams, simulations, attempts,
        generations, cadernoIa, answersIa, mockProvider);
  }

  private static User user(long id) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha-segura-123"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(true);
    return u;
  }

  private static Exam exam(long id, String institution, int year) {
    Exam e = new Exam();
    ReflectionTestUtils.setField(e, "id", id);
    ReflectionTestUtils.setField(e, "institution", institution);
    ReflectionTestUtils.setField(e, "year", (short) year);
    return e;
  }

  private static List<Exam> ifrnTrack() {
    List<Exam> out = new ArrayList<>();
    long id = 1;
    for (int year : new int[] {2020, 2022, 2023, 2024, 2025, 2026}) {
      out.add(exam(id++, "IFRN", year));
    }
    return out;
  }

  private void activeUser() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L)));
  }

  private void stubSaves(long generationId, long simulationId, long attemptId) {
    when(generations.save(any(AiGeneration.class))).thenAnswer(inv -> {
      AiGeneration g = inv.getArgument(0);
      ReflectionTestUtils.setField(g, "id", generationId);
      return g;
    });
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
  }

  private static AiGenerationQuestion frozenRow(long generationId, int position,
      String discipline, String answer) {
    AiGenerationQuestion row = new AiGenerationQuestion();
    row.setGenerationId(generationId);
    row.setPosition((short) position);
    row.setDiscipline(discipline);
    row.setTopic("DESCONHECIDO");
    row.setStem("Enunciado " + position + "?");
    row.setOptionsJson("{\"A\":\"a" + position + "\",\"B\":\"b" + position
        + "\",\"C\":\"c" + position + "\",\"D\":\"d" + position + "\"}");
    row.setFrozenAnswerKey(answer);
    return row;
  }

  private LoadedExecution stubExecution(String mode, String status, List<AiGenerationQuestion> rows) {
    User u = user(1L);
    AiGeneration generation = new AiGeneration();
    ReflectionTestUtils.setField(generation, "id", 3L);
    generation.setUser(u);
    generation.setInstitution("IFRN");
    generation.setEditionModel("IFRN-2026");
    generation.setMode(mode);

    Simulation simulation = new Simulation();
    ReflectionTestUtils.setField(simulation, "id", 7L);
    simulation.setOwner(u);
    simulation.setType("AI_GENERATED_EDITION");
    simulation.setAiGenerationId(3L);
    simulation.setTitle("Prova-IA IFRN-2026 (experimental) [" + mode + "]");

    SimulationAttempt attempt = new SimulationAttempt();
    ReflectionTestUtils.setField(attempt, "id", 55L);
    attempt.setSimulation(simulation);
    attempt.setUser(u);
    attempt.setMode(mode);
    attempt.setStatus(status);
    attempt.setStartedAt(OffsetDateTime.now());

    when(attempts.findByIdAndUserId(55L, 1L)).thenReturn(Optional.of(attempt));
    when(generations.findById(3L)).thenReturn(Optional.of(generation));
    when(cadernoIa.findByGenerationIdOrderByPositionAsc(3L)).thenReturn(rows);
    return new LoadedExecution(attempt, rows);
  }

  private record LoadedExecution(SimulationAttempt attempt, List<AiGenerationQuestion> rows) {}

  private static AiGenerationAnswer iaAnswer(long id, long attemptId, int position,
      String selected, boolean correct, String mode) {
    AiGenerationAnswer a = new AiGenerationAnswer();
    ReflectionTestUtils.setField(a, "id", id);
    a.setUser(user(1L));
    a.setGenerationId(3L);
    a.setPosition((short) position);
    a.setSelectedOption(selected);
    a.setCorrect(correct);
    a.setMode(mode);
    a.setAnsweredAt(OffsetDateTime.now());
    SimulationAttempt attempt = new SimulationAttempt();
    ReflectionTestUtils.setField(attempt, "id", attemptId);
    a.setSimulationAttempt(attempt);
    return a;
  }

  // ---- criação ----

  @Test
  void createDefaultsToLatestOfTrack() {
    activeUser();
    when(exams.findByInstitutionOrderByYearAsc("IFRN")).thenReturn(ifrnTrack());
    stubSaves(3L, 7L, 55L);

    AiAttemptResponse out = service.createAiSimulation(1L,
        new AiGenerateRequest("IFRN", null, "PROVA"));

    assertEquals(55L, out.attemptId());
    assertEquals("AI_GENERATED_EDITION", out.type());
    assertEquals("IFRN-2026", out.editionModel());
    assertEquals(40, out.questionCount());
    assertEquals("IN_PROGRESS", out.status());
    assertNull(out.score());
    assertEquals(40, out.questions().size());
    assertTrue(out.questions().stream().allMatch(q -> q.aiGenerated()));
    assertTrue(out.questions().stream()
        .allMatch(q -> "Questão gerada por IA — não é questão oficial IFRN/EAJ.".equals(q.aiNote())));
    // Gabarito oculto em andamento, caderno visível.
    assertTrue(out.questions().stream().allMatch(q -> q.frozenAnswerKey() == null));
    assertTrue(out.questions().stream().allMatch(q -> q.stem() != null && q.options().size() == 4));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("sem validade de prova real")));
  }

  @Test
  void createEaj2021Has50WithSplit() {
    activeUser();
    stubSaves(3L, 7L, 55L);

    AiAttemptResponse out = service.createAiSimulation(1L,
        new AiGenerateRequest("EAJ", "EAJ-2021", "ESTUDO"));

    assertEquals(50, out.questionCount());
    assertEquals(15, out.questions().stream()
        .filter(q -> "LINGUA_PORTUGUESA".equals(q.discipline())).count());
    assertEquals(15, out.questions().stream()
        .filter(q -> "MATEMATICA".equals(q.discipline())).count());
    assertEquals(12, out.questions().stream()
        .filter(q -> "CIENCIAS_NATUREZA".equals(q.discipline())).count());
    assertEquals(8, out.questions().stream()
        .filter(q -> "CIENCIAS_HUMANAS".equals(q.discipline())).count());
  }

  @Test
  void createPersistsFrozenNotebookAndExperimentalFilter() {
    activeUser();
    when(exams.findByInstitutionOrderByYearAsc("EAJ"))
        .thenReturn(List.of(exam(11L, "EAJ", 2021), exam(12L, "EAJ", 2022), exam(13L, "EAJ", 2025)));
    stubSaves(3L, 7L, 55L);

    service.createAiSimulation(1L, new AiGenerateRequest("EAJ", null, "ESTUDO"));

    var genCaptor = org.mockito.ArgumentCaptor.forClass(AiGeneration.class);
    verify(generations).save(genCaptor.capture());
    assertEquals("EAJ-2025", genCaptor.getValue().getEditionModel());
    assertEquals("READY", genCaptor.getValue().getStatus());
    assertNotNull(genCaptor.getValue().getFrozenAt());
    assertEquals("ai-prova-v1", genCaptor.getValue().getPromptVersion());

    var simCaptor = org.mockito.ArgumentCaptor.forClass(Simulation.class);
    verify(simulations).save(simCaptor.capture());
    assertEquals("AI_GENERATED_EDITION", simCaptor.getValue().getType());
    assertEquals(Long.valueOf(3L), simCaptor.getValue().getAiGenerationId());
    assertNull(simCaptor.getValue().getExamId());
    String filter = simCaptor.getValue().getFilterJson();
    assertTrue(filter.contains("\"experimental\":true"));
    assertTrue(filter.contains("\"editionModel\":\"EAJ-2025\""));
    assertTrue(filter.contains("\"promptVersion\":\"ai-prova-v1\""));
  }

  @Test
  void invalidInstitution400() {
    activeUser();
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> service.createAiSimulation(1L, new AiGenerateRequest("ENEM", null, "PROVA")));
    assertEquals("INVALID_INSTITUTION", ex.getCode());
  }

  @Test
  void missingInstitution400() {
    activeUser();
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> service.createAiSimulation(1L, new AiGenerateRequest(null, null, "PROVA")));
    assertEquals("INVALID_INSTITUTION", ex.getCode());
  }

  @Test
  void invalidMode400() {
    activeUser();
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> service.createAiSimulation(1L, new AiGenerateRequest("IFRN", "IFRN-2026", "TREINO")));
    assertEquals("INVALID_MODE", ex.getCode());
  }

  @Test
  void unknownEditionModel404() {
    activeUser();
    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
        () -> service.createAiSimulation(1L, new AiGenerateRequest("IFRN", "IFRN-2021", "PROVA")));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
  }

  @Test
  void crossTrackModel404() {
    activeUser();
    ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
        () -> service.createAiSimulation(1L, new AiGenerateRequest("IFRN", "EAJ-2021", "PROVA")));
    assertEquals("EDITION_NOT_FOUND", ex.getCode());
  }

  @Test
  void providerGarbage422WithoutPartialNotebook() {
    AiSimulationService broken = new AiSimulationService(users, exams, simulations, attempts,
        generations, cadernoIa, answersIa, new AiProvaProvider() {
          @Override
          public String modelId() {
            return "stub-quebrado-v1";
          }

          @Override
          public String generate(String editionModel) {
            return "{\"format\": \"IFRN\", ";
          }
        });
    activeUser();
    when(exams.findByInstitutionOrderByYearAsc("IFRN")).thenReturn(ifrnTrack());

    assertThrows(AiGenerationInvalidException.class,
        () -> broken.createAiSimulation(1L, new AiGenerateRequest("IFRN", null, "PROVA")));
    verify(generations, never()).save(any());
    verify(simulations, never()).save(any());
    verify(attempts, never()).save(any());
  }

  // ---- resposta ----

  @Test
  void answerEstudoRevealsImmediately() {
    activeUser();
    List<AiGenerationQuestion> rows = List.of(frozenRow(3L, 1, "MATEMATICA", "B"));
    stubExecution("ESTUDO", "IN_PROGRESS", rows);
    when(answersIa.save(any(AiGenerationAnswer.class))).thenAnswer(inv -> {
      AiGenerationAnswer a = inv.getArgument(0);
      ReflectionTestUtils.setField(a, "id", 9L);
      return a;
    });

    AiAnswerResponse out = service.answerPosition(1L, 55L, new AiAnswerRequest(1, "B", 12));

    assertEquals(Boolean.TRUE, out.isCorrect());
    assertEquals("B", out.frozenAnswerKey());
    assertTrue(out.aiGenerated());
  }

  @Test
  void answerProvaHidesUntilClose() {
    activeUser();
    List<AiGenerationQuestion> rows = List.of(frozenRow(3L, 1, "MATEMATICA", "B"));
    stubExecution("PROVA", "IN_PROGRESS", rows);
    when(answersIa.save(any(AiGenerationAnswer.class))).thenAnswer(inv -> {
      AiGenerationAnswer a = inv.getArgument(0);
      ReflectionTestUtils.setField(a, "id", 9L);
      return a;
    });

    AiAnswerResponse out = service.answerPosition(1L, 55L, new AiAnswerRequest(1, "A", null));

    assertNull(out.isCorrect());
    assertNull(out.frozenAnswerKey());
    assertEquals("A", out.selectedOption());
    // Persistido corretamente (reaparece após encerrar).
    var captor = org.mockito.ArgumentCaptor.forClass(AiGenerationAnswer.class);
    verify(answersIa).save(captor.capture());
    assertFalse(captor.getValue().isCorrect());
  }

  @Test
  void answerClosed409() {
    activeUser();
    stubExecution("ESTUDO", "SUBMITTED",
        List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.answerPosition(1L, 55L, new AiAnswerRequest(1, "B", null)));
    assertEquals("SIMULATION_CLOSED", ex.getCode());
  }

  @Test
  void answerBadPosition400And404() {
    activeUser();
    stubExecution("ESTUDO", "IN_PROGRESS",
        List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    BadRequestException bad = assertThrows(BadRequestException.class,
        () -> service.answerPosition(1L, 55L, new AiAnswerRequest(0, "B", null)));
    assertEquals("INVALID_POSITION", bad.getCode());
    ResourceNotFoundException missing = assertThrows(ResourceNotFoundException.class,
        () -> service.answerPosition(1L, 55L, new AiAnswerRequest(9, "B", null)));
    assertEquals("SIMULATION_QUESTION_NOT_FOUND", missing.getCode());
  }

  @Test
  void answerBadOption400() {
    activeUser();
    stubExecution("ESTUDO", "IN_PROGRESS",
        List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    BadRequestException ex = assertThrows(BadRequestException.class,
        () -> service.answerPosition(1L, 55L, new AiAnswerRequest(1, "E", null)));
    assertEquals("INVALID_OPTION", ex.getCode());
  }

  // ---- encerrar / resultado / feedback ----

  @Test
  void submitScoresAgainstFrozen() {
    activeUser();
    List<AiGenerationQuestion> rows = List.of(
        frozenRow(3L, 1, "MATEMATICA", "B"),
        frozenRow(3L, 2, "MATEMATICA", "A"));
    stubExecution("PROVA", "IN_PROGRESS", rows);
    when(answersIa.findBySimulationAttemptIdAndUserIdOrderByAnsweredAtAscIdAsc(55L, 1L))
        .thenReturn(List.of(
            iaAnswer(10L, 55L, 1, "B", true, "PROVA"),
            iaAnswer(11L, 55L, 2, "C", false, "PROVA")));

    AiResultResponse out = service.submitAiAttempt(1L, 55L);

    assertEquals("SUBMITTED", out.status());
    assertEquals(2, out.total());
    assertEquals(2, out.answered());
    assertEquals(1, out.correct());
    assertEquals(1, out.incorrect());
    assertEquals(0.5, out.accuracy());
    assertEquals(Boolean.TRUE, out.items().get(0).isCorrect());
    assertEquals("B", out.items().get(0).frozenAnswerKey());
    assertTrue(out.items().stream().allMatch(i -> i.aiGenerated()));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("sem validade de prova real")));
    assertTrue(out.notes().stream().anyMatch(n -> n.contains("não alimenta diagnóstico")));
  }

  @Test
  void resultHiddenWhileInProgress() {
    activeUser();
    stubExecution("PROVA", "IN_PROGRESS", List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.getAiResult(1L, 55L));
    assertEquals("SIMULATION_NOT_FINISHED", ex.getCode());
  }

  @Test
  void getExecutionAfterCloseReveals() {
    activeUser();
    List<AiGenerationQuestion> rows = List.of(frozenRow(3L, 1, "MATEMATICA", "B"));
    stubExecution("PROVA", "SUBMITTED", rows);
    when(answersIa.findBySimulationAttemptIdAndUserIdOrderByAnsweredAtAscIdAsc(55L, 1L))
        .thenReturn(List.of(iaAnswer(10L, 55L, 1, "B", true, "PROVA")));

    AiAttemptResponse out = service.getAiAttempt(1L, 55L);

    assertEquals("B", out.questions().get(0).frozenAnswerKey());
    assertEquals(Boolean.TRUE, out.questions().get(0).isCorrect());
    assertNotNull(out.score());
  }

  @Test
  void feedbackEstudoImmediate() {
    activeUser();
    stubExecution("ESTUDO", "IN_PROGRESS", List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    when(answersIa.findBySimulationAttemptIdAndUserIdOrderByAnsweredAtAscIdAsc(55L, 1L))
        .thenReturn(List.of(iaAnswer(10L, 55L, 1, "A", false, "ESTUDO")));

    AiFeedbackResponse out = service.getAiFeedback(1L, 55L, 1);

    assertEquals(Boolean.FALSE, out.isCorrect());
    assertEquals("B", out.correctAnswer());
    assertTrue(out.aiGenerated());
  }

  @Test
  void feedbackProvaInProgress409() {
    activeUser();
    stubExecution("PROVA", "IN_PROGRESS", List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.getAiFeedback(1L, 55L, 1));
    assertEquals("STUDY_FEEDBACK_UNAVAILABLE", ex.getCode());
  }

  @Test
  void feedbackWithoutAnswer409() {
    activeUser();
    stubExecution("ESTUDO", "IN_PROGRESS", List.of(frozenRow(3L, 1, "MATEMATICA", "B")));
    when(answersIa.findBySimulationAttemptIdAndUserIdOrderByAnsweredAtAscIdAsc(55L, 1L))
        .thenReturn(List.of());
    ConflictException ex = assertThrows(ConflictException.class,
        () -> service.getAiFeedback(1L, 55L, 1));
    assertEquals("FEEDBACK_NOT_AVAILABLE", ex.getCode());
  }

  // ---- isolamento ----

  @Test
  void officialAttemptIs404OnAiRoutes() {
    activeUser();
    User u = user(1L);
    Simulation official = new Simulation();
    ReflectionTestUtils.setField(official, "id", 8L);
    official.setOwner(u);
    official.setType("BY_DISCIPLINE");
    SimulationAttempt attempt = new SimulationAttempt();
    ReflectionTestUtils.setField(attempt, "id", 56L);
    attempt.setSimulation(official);
    attempt.setUser(u);
    attempt.setMode("PROVA");
    attempt.setStatus("IN_PROGRESS");
    when(attempts.findByIdAndUserId(56L, 1L)).thenReturn(Optional.of(attempt));

    assertEquals("SIMULATION_ATTEMPT_NOT_FOUND",
        assertThrows(ResourceNotFoundException.class, () -> service.getAiAttempt(1L, 56L)).getCode());
    assertEquals("SIMULATION_ATTEMPT_NOT_FOUND",
        assertThrows(ResourceNotFoundException.class,
            () -> service.answerPosition(1L, 56L, new AiAnswerRequest(1, "A", null))).getCode());
  }

  @Test
  void otherUsersAttemptIs404() {
    activeUser();
    when(attempts.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
    assertEquals("SIMULATION_ATTEMPT_NOT_FOUND",
        assertThrows(ResourceNotFoundException.class, () -> service.getAiAttempt(1L, 99L)).getCode());
  }
}
