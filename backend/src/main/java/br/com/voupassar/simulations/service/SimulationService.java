package br.com.voupassar.simulations.service;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
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
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.simulations.dto.CreateDisciplineSimulationRequest;
import br.com.voupassar.simulations.dto.CreateEditionSimulationRequest;
import br.com.voupassar.simulations.dto.StudyFeedbackResponse;
import br.com.voupassar.simulations.dto.SimulationAttemptResponse;
import br.com.voupassar.simulations.dto.SimulationAttemptResponse.CadernoItem;
import br.com.voupassar.simulations.dto.SimulationAttemptResponse.ScoreSummary;
import br.com.voupassar.simulations.dto.SimulationAttemptSummary;
import br.com.voupassar.simulations.dto.SimulationResultResponse;
import br.com.voupassar.simulations.dto.SimulationResultResponse.ResultItem;
import br.com.voupassar.simulations.entity.Simulation;
import br.com.voupassar.simulations.entity.SimulationAttempt;
import br.com.voupassar.simulations.entity.SimulationQuestion;
import br.com.voupassar.simulations.repository.SimulationAttemptRepository;
import br.com.voupassar.simulations.repository.SimulationQuestionRepository;
import br.com.voupassar.simulations.repository.SimulationRepository;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Simulado por disciplina (TASK 5.1) e simulado real por edição (TASK 5.5) —
 * escolher disciplina, quantidade e dificuldade, ou reproduzir a estrutura
 * integral de uma edição real; iniciar; retomar ({@code IN_PROGRESS});
 * concluir; abandonar; visualizar resultado — mais feedback imediato do Modo
 * Estudo (TASK 5.2).
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Seleção por amostra aleatória simples <b>sem reposição</b> sobre as
 *       candidatas elegíveis (disciplina + filtro de dificuldade,
 *       não-anuladas), via {@link SecureRandom}; a ordem sorteada vira a
 *       ordem do caderno e é <b>congelada</b> em {@code simulation_questions}
 *       (posição + gabarito vigente) — reprodutível e auditável após a
 *       criação, sem expor enunciado duplicado (resolve-se via TASK 3.4).</li>
 *   <li>Anuladas ficam fora da seleção (pontuação DESCONHECIDA, TASK 1.3
 *       §4); quantidade acima do disponível → {@code 400
 *       INSUFFICIENT_QUESTIONS} com o disponível explícito (nunca redução
 *       silenciosa).</li>
 *   <li>Correção 100% no servidor a partir do fato imutável {@code
 *       question_attempts} (TASK 3.7) confrontado com o gabarito
 *       <b>congelado</b>; vale a <b>última</b> tentativa por questão; o
 *       cliente nunca escreve nota ({@code score_json} é carimbado no
 *       servidor ao encerrar).</li>
 *   <li>Durante {@code IN_PROGRESS} o gabarito fica oculto (preserva a
 *       sensação de prova nos dois modos); a correção aparece só após
 *       concluir/abandonar. Não existe estado de pausa no DDL — "pausar" é
 *       manter {@code IN_PROGRESS} e retomar via {@code GET}.</li>
  *   <li>{@code REAL_EDITION} não é gerado aqui (TASK 5.5); {@code REVISAO}
  *       não gera simulado novo (ERD §2.6).</li>
  *   <li>Feedback imediato por posição (TASK 5.2): no Modo ESTUDO em qualquer
  *       status e no Modo PROVA somente após encerrar; durante a prova o
  *       gabarito permanece oculto (base da TASK 5.3). Vale a <b>última</b>
  *       tentativa por questão, confrontada com o gabarito <b>congelado</b>;
  *       sem resposta não há feedback.</li>
 * </ul>
 */
@Service
public class SimulationService {

  /** Teto de questões por simulado e de itens por página: protege banco e VPS modesta. */
  public static final int MAX_COUNT = 100;

  private static final Set<String> MODES = Set.of("ESTUDO", "PROVA");
  private static final Set<String> DIFFICULTIES = Set.of("FACIL", "MEDIA", "DIFICIL");
  private static final Set<String> SOURCE_TYPES =
      Set.of("OFFICIAL", "AUTHORAL", "ADAPTED", "INTERNAL_REVIEW", "EXPERIMENTAL");
  /** Origem padrão do simulado por disciplina (TASK 15.4): só oficiais. */
  private static final String DEFAULT_SOURCE_TYPE = "OFFICIAL";
  /** Valor que desliga o filtro de origem (caderno misto, TASK 15.4). */
  private static final String SOURCE_TYPE_ALL = "ALL";

  private static final Logger log = LoggerFactory.getLogger(SimulationService.class);

  private final UserRepository users;
  private final DisciplineRepository disciplines;
  private final QuestionRepository questions;
  private final SimulationRepository simulations;
  private final SimulationAttemptRepository attempts;
  private final SimulationQuestionRepository caderno;
  private final QuestionAttemptRepository responses;
  private final QuestionClassificationRepository classifications;
  private final ExamRepository exams;
  private final ExamEssayPromptRepository essayPrompts;
  private final Random random;
  private final br.com.voupassar.admin.service.TechMetrics metrics;

  @Autowired
  public SimulationService(
      UserRepository users,
      DisciplineRepository disciplines,
      QuestionRepository questions,
      SimulationRepository simulations,
      SimulationAttemptRepository attempts,
      SimulationQuestionRepository caderno,
      QuestionAttemptRepository responses,
      QuestionClassificationRepository classifications,
      ExamRepository exams,
      ExamEssayPromptRepository essayPrompts,
      br.com.voupassar.admin.service.TechMetrics metrics) {
    this(users, disciplines, questions, simulations, attempts, caderno, responses,
        classifications, exams, essayPrompts, new SecureRandom(), metrics);
  }

  SimulationService(
      UserRepository users,
      DisciplineRepository disciplines,
      QuestionRepository questions,
      SimulationRepository simulations,
      SimulationAttemptRepository attempts,
      SimulationQuestionRepository caderno,
      QuestionAttemptRepository responses,
      QuestionClassificationRepository classifications,
      ExamRepository exams,
      ExamEssayPromptRepository essayPrompts,
      Random random,
      br.com.voupassar.admin.service.TechMetrics metrics) {
    this.users = users;
    this.disciplines = disciplines;
    this.questions = questions;
    this.simulations = simulations;
    this.attempts = attempts;
    this.caderno = caderno;
    this.responses = responses;
    this.classifications = classifications;
    this.exams = exams;
    this.essayPrompts = essayPrompts;
    this.random = random;
    this.metrics = metrics;
  }

  /**
   * Cria e inicia um simulado por disciplina (caderno congelado, {@code
   * IN_PROGRESS}).
   */
  @Transactional
  public SimulationAttemptResponse createByDiscipline(long userId, CreateDisciplineSimulationRequest req) {
    User user = requireActiveUser(userId);
    Discipline discipline = requireDiscipline(req.disciplineCode());
    int count = requireCount(req.questionCount());
    String difficulty = normalizeDifficulty(req.difficulty());
    String mode = normalizeMode(req.mode());
    String sourceType = normalizeSimulationSourceType(req.sourceType());

    List<Long> candidates =
        questions.findCandidateIdsByDiscipline(discipline.getCode(), difficulty, sourceType);
    if (candidates.size() < count) {
      throw new BadRequestException(
          "INSUFFICIENT_QUESTIONS",
          "Disponíveis " + candidates.size() + " questões"
              + describeFilter(discipline.getCode(), difficulty, sourceType)
              + " (solicitadas " + count + "). Reduza a quantidade ou remova o filtro de dificuldade.");
    }

    List<Long> pool = new ArrayList<>(candidates);
    Collections.shuffle(pool, random);
    List<Long> selected = List.copyOf(pool.subList(0, count));
    Map<Long, Question> byId = questionsById(selected);

    Simulation simulation = new Simulation();
    simulation.setOwner(user);
    simulation.setType("BY_DISCIPLINE");
    simulation.setExamId(null);
    simulation.setFilterJson(filterJson(discipline.getCode(), count, difficulty, mode, sourceType));
    simulation.setTitle(title(discipline.getName(), count, difficulty, mode));
    simulations.save(simulation);

    SimulationAttempt attempt = new SimulationAttempt();
    attempt.setSimulation(simulation);
    attempt.setUser(user);
    attempt.setMode(mode);
    attempt.setStatus("IN_PROGRESS");
    attempt.setStartedAt(OffsetDateTime.now());
    attempts.save(attempt);

    List<SimulationQuestion> rows = new ArrayList<>(count);
    for (int i = 0; i < selected.size(); i++) {
      Question q = requirePresent(byId, selected.get(i));
      SimulationQuestion row = new SimulationQuestion();
      row.setSimulationAttemptId(attempt.getId());
      row.setPosition((short) (i + 1));
      row.setQuestion(q);
      row.setFrozenAnswerKey(q.getAnswerKey());
      rows.add(row);
    }
    caderno.saveAll(rows);

    log.info("simulado criado user_id={} attempt_id={} discipline={} count={} difficulty={} mode={} sourceType={}",
        userId, attempt.getId(), discipline.getCode(), count, difficulty, mode, sourceType);
    return toAttemptResponse(attempt, simulation, rows, byId, Map.of(), false);
  }

  /**
   * Cria e inicia um simulado real por edição (TASK 5.5): caderno integral da
   * edição em ordem original, {@code IN_PROGRESS}.
   *
   * <p>Regras de evidência:
   * <ul>
   *   <li>Estrutura dirigida pela configuração da própria edição ({@code
   *       exams}: total, LP, MAT, discursiva) — nunca regra universal.</li>
   *   <li>Sem sorteio e sem filtro de dificuldade: posições 1..N seguem o
   *       número da questão na edição; anuladas participam nas posições
   *       originais (fidelidade) e ficam fora do aproveitamento (pontuação
   *       DESCONHECIDA, TASK 1.3 §4).</li>
   *   <li>Banco divergente do esperado na capa (faltas, numeração com lacuna
   *       ou divisão disciplinar diferente) → {@code 409 INCOMPLETE_EDITION}
   *       com os números explícitos (nunca caderno parcial silencioso).</li>
   *   <li>Discursiva sai só como referência em notas (proposta + critérios
   *       impressos); correção automática DESCONHECIDA / fora do MVP.</li>
   * </ul>
   */
  @Transactional
  public SimulationAttemptResponse createByEdition(long userId, CreateEditionSimulationRequest req) {
    User user = requireActiveUser(userId);
    int year = requireEditionYear(req == null ? null : req.editionYear());
    String mode = normalizeMode(req == null ? null : req.mode());
    String institution = normalizeInstitutionOrDefault(req == null ? null : req.institution());
    Exam exam = requireExam(institution, year);

    List<Question> board = questions.findByEditionOrdered(institution, (short) year);
    validateEditionBoard(exam, board);

    Simulation simulation = new Simulation();
    simulation.setOwner(user);
    simulation.setType("REAL_EDITION");
    simulation.setExamId(exam.getId());
    simulation.setFilterJson(filterJsonEdition(institution, year, mode));
    simulation.setTitle(titleEdition(institution, year, board.size(), mode));
    simulations.save(simulation);

    SimulationAttempt attempt = new SimulationAttempt();
    attempt.setSimulation(simulation);
    attempt.setUser(user);
    attempt.setMode(mode);
    attempt.setStatus("IN_PROGRESS");
    attempt.setStartedAt(OffsetDateTime.now());
    attempts.save(attempt);

    List<SimulationQuestion> rows = new ArrayList<>(board.size());
    for (int i = 0; i < board.size(); i++) {
      Question q = board.get(i);
      SimulationQuestion row = new SimulationQuestion();
      row.setSimulationAttemptId(attempt.getId());
      row.setPosition((short) (i + 1));
      row.setQuestion(q);
      row.setFrozenAnswerKey(q.getAnswerKey());
      rows.add(row);
    }
    caderno.saveAll(rows);

    Map<Long, Question> byId = new HashMap<>(board.size() * 2);
    for (Question q : board) {
      byId.put(q.getId(), q);
    }
    SimulationAttemptResponse base = toAttemptResponse(attempt, simulation, rows, byId, Map.of(), false);
    List<String> notes = new ArrayList<>(base.notes().size() + 6);
    notes.addAll(editionNotes(exam, institution, year));
    notes.addAll(base.notes());
    log.info("simulado real criado user_id={} attempt_id={} edition={} {} count={} mode={}",
        userId, attempt.getId(), institution, year, board.size(), mode);
    return new SimulationAttemptResponse(
        base.attemptId(), base.simulationId(), base.type(), base.title(),
        base.disciplineCode(), base.disciplineName(), base.mode(), base.status(),
        base.questionCount(), base.startedAt(), base.submittedAt(),
        base.questions(), base.score(), List.copyOf(notes));
  }

  /** Consulta uma execução do dono do token (retomar o caderno em andamento ou rever o encerrado). */
  @Transactional(readOnly = true)
  public SimulationAttemptResponse getAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    SimulationAttempt attempt = requireOwnedAttempt(userId, attemptId);
    return loadAttemptResponse(attempt, !"IN_PROGRESS".equals(attempt.getStatus()));
  }

  /** Lista as execuções do dono do token (mais recentes primeiro). */
  @Transactional(readOnly = true)
  public PageResponse<SimulationAttemptSummary> listAttempts(long userId, int page, int size) {
    requireActiveUser(userId);
    if (page < 0) {
      throw new BadRequestException("Página inválida: " + page + " (0-based).");
    }
    if (size < 1 || size > MAX_COUNT) {
      throw new BadRequestException(
          "Tamanho de página inválido: " + size + " (permitido 1–" + MAX_COUNT + ").");
    }
    Page<SimulationAttempt> result =
        attempts.findByUserIdOrderByStartedAtDescIdDesc(userId, PageRequest.of(page, size));
    List<SimulationAttempt> content = result.getContent();

    List<Long> attemptIds = content.stream().map(SimulationAttempt::getId).toList();
    Map<Long, List<SimulationQuestion>> rowsByAttempt = Map.of();
    Map<Long, Question> questionsById = Map.of();
    Map<Long, Simulation> simulationsById = Map.of();
    if (!attemptIds.isEmpty()) {
      List<SimulationQuestion> rows =
          caderno.findBySimulationAttemptIdInOrderBySimulationAttemptIdAscPositionAsc(attemptIds);
      rowsByAttempt = groupByAttempt(rows);
      Set<Long> questionIds = new HashSet<>();
      for (SimulationQuestion r : rows) {
        questionIds.add(r.getQuestion().getId());
      }
      questionsById = questionsById(new ArrayList<>(questionIds));
      Set<Long> simulationIds = new HashSet<>();
      for (SimulationAttempt a : content) {
        simulationIds.add(a.getSimulation().getId());
      }
      simulationsById = simulationsById(new ArrayList<>(simulationIds));
    }

    List<SimulationAttemptSummary> out = new ArrayList<>(content.size());
    for (SimulationAttempt a : content) {
      List<SimulationQuestion> rows = rowsByAttempt.getOrDefault(a.getId(), List.of());
      Simulation s = simulationsById.get(a.getSimulation().getId());
      String title = s == null ? "Simulado" : s.getTitle();
      boolean realEdition = s != null && "REAL_EDITION".equals(s.getType());
      String discCode = null;
      if (!realEdition && !rows.isEmpty()) {
        Question first = questionsById.get(rows.get(0).getQuestion().getId());
        if (first != null) {
          discCode = first.getDiscipline().getCode();
        }
      }
      out.add(new SimulationAttemptSummary(
          a.getId(), a.getSimulation().getId(), title, discCode,
          a.getMode(), a.getStatus(), rows.size(), a.getStartedAt(), a.getSubmittedAt()));
    }
    return new PageResponse<>(
        List.copyOf(out), result.getNumber(), result.getSize(),
        result.getTotalElements(), result.getTotalPages(), result.isFirst(), result.isLast());
  }

  /** Conclui a execução ({@code IN_PROGRESS → SUBMITTED}) com o placar calculado no servidor. */
  @Transactional
  public SimulationResultResponse submitAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    SimulationAttempt attempt = requireOwnedAttempt(userId, attemptId);
    requireInProgress(attempt);
    attempt.setStatus("SUBMITTED");
    attempt.setSubmittedAt(OffsetDateTime.now());
    ScoredBoard board = scoreBoard(attempt);
    attempt.setScoreJson(scoreJson(board, attempt.getMode(), attempt.getStatus()));
    attempts.save(attempt);

    log.info("simulado concluído user_id={} attempt_id={} scored={} correct={}",
        userId, attemptId, board.scored, board.correct);
    metrics.simulationsSubmitted();
    return toResultResponse(attempt, board, false);
  }

  /** Desiste da execução ({@code IN_PROGRESS → ABANDONED}) com o placar parcial do servidor. */
  @Transactional
  public SimulationResultResponse abandonAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    SimulationAttempt attempt = requireOwnedAttempt(userId, attemptId);
    requireInProgress(attempt);
    attempt.setStatus("ABANDONED");
    attempt.setSubmittedAt(OffsetDateTime.now());
    ScoredBoard board = scoreBoard(attempt);
    attempt.setScoreJson(scoreJson(board, attempt.getMode(), attempt.getStatus()));
    attempts.save(attempt);

    log.info("simulado abandonado user_id={} attempt_id={} answered={}",
        userId, attemptId, board.answered);
    return toResultResponse(attempt, board, true);
  }

  /** Visualiza o resultado (só após concluir ou abandonar). */
  @Transactional(readOnly = true)
  public SimulationResultResponse getResult(long userId, long attemptId) {
    requireActiveUser(userId);
    SimulationAttempt attempt = requireOwnedAttempt(userId, attemptId);
    if ("IN_PROGRESS".equals(attempt.getStatus())) {
      throw new ConflictException(
          "SIMULATION_NOT_FINISHED",
          "Simulado ainda em andamento: conclua ou abandone para ver o resultado (gabarito oculto durante a execução).");
    }
    return toResultResponse(attempt, scoreBoard(attempt), "ABANDONED".equals(attempt.getStatus()));
  }

  /**
   * Feedback imediato de uma posição do caderno (TASK 5.2 — Modo Estudo).
   *
   * <p>Mostra acerto/erro, resposta correta (gabarito congelado) e
   * conteúdo relacionado (assunto/subassunto vigentes) a partir da
   * <b>última</b> tentativa vinculada a esta execução. Sem resposta, não há
   * feedback (nunca inventado).
   *
   * <p>Disponível no Modo ESTUDO em qualquer status e no Modo PROVA somente
   * após encerrar: durante a execução da prova o gabarito permanece oculto
   * (preserva a sensação de prova — base da TASK 5.3).
   */
  @Transactional(readOnly = true)
  public StudyFeedbackResponse getStudyFeedback(long userId, long attemptId, int position) {
    requireActiveUser(userId);
    SimulationAttempt attempt = requireOwnedAttempt(userId, attemptId);
    if (position < 1) {
      throw new BadRequestException(
          "INVALID_POSITION", "Posição inválida: " + position + " (a primeira posição é 1).");
    }
    if ("PROVA".equals(attempt.getMode()) && "IN_PROGRESS".equals(attempt.getStatus())) {
      throw new ConflictException(
          "STUDY_FEEDBACK_UNAVAILABLE",
          "Feedback imediato indisponível durante a execução no Modo Prova: "
              + "conclua ou abandone para ver a correção (o gabarito fica oculto até encerrar).");
    }

    List<SimulationQuestion> rows =
        caderno.findBySimulationAttemptIdOrderByPositionAsc(attemptId);
    SimulationQuestion row = null;
    for (SimulationQuestion r : rows) {
      if (r.getPosition() != null && r.getPosition() == position) {
        row = r;
        break;
      }
    }
    if (row == null) {
      throw new ResourceNotFoundException(
          "SIMULATION_QUESTION_NOT_FOUND",
          "Posição " + position + " não encontrada neste simulado (" + rows.size() + " posições).");
    }

    Question question = requirePresent(
        questionsById(List.of(row.getQuestion().getId())), row.getQuestion().getId());
    QuestionAttempt last = null;
    for (QuestionAttempt a : responses.findBySimulationAttemptIdAndUserId(attemptId, userId)) {
      if (a.getQuestion() != null && question.getId().equals(a.getQuestion().getId())
          && (last == null || compareRecency(a, last) > 0)) {
        last = a;
      }
    }
    if (last == null) {
      throw new ConflictException(
          "FEEDBACK_NOT_AVAILABLE",
          "Posição " + position + " ainda sem resposta nesta execução: "
              + "responda via POST /attempts com simulationAttemptId=" + attemptId
              + " antes de ver o feedback.");
    }

    boolean wasAnnulled = question.isAnnulled()
        || "X".equals(row.getFrozenAnswerKey())
        || "X".equals(question.getAnswerKey())
        || last.isAnnulled();
    Boolean isCorrect = wasAnnulled ? null : last.getSelectedOption().equals(row.getFrozenAnswerKey());

    List<QuestionClassification> actives =
        classifications.findActiveByQuestionId(question.getId());
    QuestionClassification classification = actives.isEmpty() ? null : actives.get(0);
    Topic topic = classification == null ? null : classification.getTopic();
    Subtopic subtopic = classification == null ? null : classification.getSubtopic();

    List<String> notes = feedbackNotes(attempt, rows.size(), question, classification, wasAnnulled);
    return new StudyFeedbackResponse(
        attempt.getId(), position, question.getId(),
        question.getDiscipline().getCode(), question.getDiscipline().getName(),
        question.getSourceYear() == null ? null : question.getSourceYear().intValue(),
        question.getExam() != null ? question.getExam().getInstitution() : null,
        question.getSourceQuestionNumber() == null ? null : question.getSourceQuestionNumber().intValue(),
        last.getSelectedOption(), isCorrect, wasAnnulled,
        row.getFrozenAnswerKey(),
        topic == null ? null : topic.getId(),
        topic == null ? null : topic.getCode(),
        topic == null ? null : topic.getName(),
        subtopic == null ? null : subtopic.getId(),
        subtopic == null ? null : subtopic.getCode(),
        subtopic == null ? null : subtopic.getName(),
        classification == null ? null : classification.getConfidence(),
        classification == null ? null : classification.getTaxonomyVersion(),
        List.copyOf(notes));
  }

  // ---- internals ----

  private User requireActiveUser(long userId) {
    User user = users.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));
    if (!user.isActive()) {
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente.");
    }
    return user;
  }

  private SimulationAttempt requireOwnedAttempt(long userId, long attemptId) {
    return attempts.findByIdAndUserId(attemptId, userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "SIMULATION_ATTEMPT_NOT_FOUND", "Execução de simulado não encontrada."));
  }

  private static void requireInProgress(SimulationAttempt attempt) {
    if (!"IN_PROGRESS".equals(attempt.getStatus())) {
      throw new ConflictException(
          "SIMULATION_CLOSED", "Simulado já encerrado (status " + attempt.getStatus() + ").");
    }
  }

  private Discipline requireDiscipline(String code) {
    if (code == null || code.isBlank()) {
      throw new BadRequestException("Disciplina é obrigatória.");
    }
    String normalized = code.trim().toUpperCase();
    if (!normalized.matches("[A-Z_]{1,64}")) {
      throw new BadRequestException("Código de disciplina inválido: " + code + ".");
    }
    return disciplines.findByCode(normalized)
        .orElseThrow(() -> new ResourceNotFoundException(
            "DISCIPLINE_NOT_FOUND", "Disciplina " + normalized + " não encontrada."));
  }

  private static int requireCount(Integer count) {
    if (count == null) {
      throw new BadRequestException("Quantidade é obrigatória.");
    }
    if (count < 1 || count > MAX_COUNT) {
      throw new BadRequestException(
          "INVALID_QUESTION_COUNT", "Quantidade inválida: " + count + " (permitido 1–" + MAX_COUNT + ").");
    }
    return count;
  }

  private static String normalizeDifficulty(String difficulty) {
    if (difficulty == null || difficulty.isBlank()) {
      return null;
    }
    String normalized = difficulty.trim().toUpperCase();
    if (!DIFFICULTIES.contains(normalized)) {
      throw new BadRequestException(
          "INVALID_DIFFICULTY",
          "Dificuldade inválida: " + difficulty + " (permitido FACIL, MEDIA, DIFICIL).");
    }
    return normalized;
  }

  private static String normalizeMode(String mode) {
    String m = mode == null ? "" : mode.trim().toUpperCase();
    if (!MODES.contains(m)) {
      throw new BadRequestException("INVALID_MODE", "Modo deve ser ESTUDO ou PROVA.");
    }
    return m;
  }

  /**
   * Origem do simulado por disciplina (TASK 15.4). NULL/em-branco = {@code
   * OFFICIAL} (padrão); {@code ALL} = sem filtro (caderno misto, retorna
   * NULL para a consulta). Demais valores seguem {@code
   * questions.source_type}; outro valor = 400.
   */
  private static String normalizeSimulationSourceType(String sourceType) {
    if (sourceType == null || sourceType.isBlank()) {
      return DEFAULT_SOURCE_TYPE;
    }
    String normalized = sourceType.trim().toUpperCase();
    if (SOURCE_TYPE_ALL.equals(normalized)) {
      return null;
    }
    if (!SOURCE_TYPES.contains(normalized)) {
      throw new BadRequestException(
          "INVALID_SOURCE_TYPE",
          "Origem inválida: " + sourceType
              + " (permitido OFFICIAL, AUTHORAL, ADAPTED, INTERNAL_REVIEW, EXPERIMENTAL ou ALL).");
    }
    return normalized;
  }

  private static int requireEditionYear(Integer year) {
    if (year == null) {
      throw new BadRequestException("Ano da edição é obrigatório.");
    }
    if (year < 2000 || year > 2100) {
      throw new BadRequestException(
          "INVALID_EDITION_YEAR", "Ano de edição inválido: " + year + " (permitido 2000–2100).");
    }
    return year;
  }

  private Exam requireExam(String institution, int year) {
    Short y = (short) year;
    return exams.findByInstitutionAndYear(institution, y).orElseThrow(() -> {
      if ("IFRN".equals(institution) && year == 2021) {
        return new ResourceNotFoundException(
            "EDITION_NOT_FOUND",
            "Edição IFRN 2021 não encontrada: ausente do dataset inicial (AGENTS.md §3)."
                + " EAJ-2021 existe (50Q em 4 áreas) — informe institution=EAJ.");
      }
      if ("EAJ".equals(institution)) {
        return new ResourceNotFoundException(
            "EDITION_NOT_FOUND",
            "Edição EAJ " + year + " não encontrada: EAJ possui apenas 2021/2022/2025 no dataset"
                + " (nunca interpolar edições inexistentes).");
      }
      return new ResourceNotFoundException(
          "EDITION_NOT_FOUND", "Edição " + institution + " " + year + " não encontrada.");
    });
  }

  /**
   * Normaliza {@code institution} do simulado real (TASK E.1):
   * ausente = {@code IFRN} (compatibilidade); senão IFRN|EAJ, senão 400.
   */
  private static String normalizeInstitutionOrDefault(String institution) {
    if (institution == null || institution.isBlank()) {
      return "IFRN";
    }
    String normalized = institution.trim().toUpperCase();
    if (!"IFRN".equals(normalized) && !"EAJ".equals(normalized)) {
      throw new BadRequestException(
          "INVALID_INSTITUTION",
          "Processo seletivo inválido: " + institution + " (permitido IFRN, EAJ).");
    }
    return normalized;
  }

  /**
   * Garante fidelidade do caderno real: total, numeração 1..N sem lacunas e
   * divisão por disciplina iguais aos da configuração daquela edição
   * (TASK E.1: EAJ-2021 = 50Q 15/15/12/8; EAJ-2022/2025 = 40Q 20/20).
   */
  private static void validateEditionBoard(Exam exam, List<Question> board) {
    String institution = exam.getInstitution() == null ? "IFRN" : exam.getInstitution();
    int year = exam.getYear().intValue();
    String tag = institution + " " + year;
    int expected = exam.getObjectiveCount().intValue();
    if (board.isEmpty()) {
      throw new ConflictException(
          "INCOMPLETE_EDITION",
          "Edição " + tag + " sem questões importadas (esperadas " + expected
              + " pela capa): NECESSITA REVISÃO antes de publicar simulado real.");
    }
    if (board.size() != expected) {
      throw new ConflictException(
          "INCOMPLETE_EDITION",
          "Edição " + tag + " incompleta: esperadas " + expected
              + " objetivas (capa) × importadas " + board.size()
              + ": NECESSITA REVISÃO antes de publicar simulado real.");
    }
    Set<Integer> numbers = new HashSet<>();
    for (Question q : board) {
      if (q.getSourceQuestionNumber() != null) {
        numbers.add(q.getSourceQuestionNumber().intValue());
      }
    }
    for (int n = 1; n <= expected; n++) {
      if (!numbers.contains(n)) {
        throw new ConflictException(
            "INCOMPLETE_EDITION",
            "Edição " + tag + " com numeração incompleta (ausente a questão " + n
                + "): NECESSITA REVISÃO antes de publicar simulado real.");
      }
    }
    int lpExpected = exam.getLpCount() == null ? 0 : exam.getLpCount().intValue();
    int matExpected = exam.getMatCount() == null ? 0 : exam.getMatCount().intValue();
    int cnExpected = exam.getCnCount() == null ? 0 : exam.getCnCount().intValue();
    int chExpected = exam.getChCount() == null ? 0 : exam.getChCount().intValue();
    if (lpExpected + matExpected + cnExpected + chExpected == expected) {
      long lp = 0;
      long mat = 0;
      long cn = 0;
      long ch = 0;
      for (Question q : board) {
        String code = q.getDiscipline() == null ? "" : q.getDiscipline().getCode();
        if ("LINGUA_PORTUGUESA".equals(code)) {
          lp++;
        } else if ("MATEMATICA".equals(code)) {
          mat++;
        } else if ("CIENCIAS_NATUREZA".equals(code)) {
          cn++;
        } else if ("CIENCIAS_HUMANAS".equals(code)) {
          ch++;
        }
      }
      if (lp != lpExpected || mat != matExpected || cn != cnExpected || ch != chExpected) {
        throw new ConflictException(
            "INCOMPLETE_EDITION",
            "Edição " + tag + " com divisão disciplinar divergente: esperado LP "
                + lpExpected + " + MAT " + matExpected + " + CN " + cnExpected + " + CH " + chExpected
                + " × importado LP " + lp + " + MAT " + mat + " + CN " + cn + " + CH " + ch
                + ": NECESSITA REVISÃO antes de publicar simulado real.");
      }
    } else if (lpExpected + matExpected == expected) {
      long lp = 0;
      long mat = 0;
      for (Question q : board) {
        String code = q.getDiscipline() == null ? "" : q.getDiscipline().getCode();
        if ("LINGUA_PORTUGUESA".equals(code)) {
          lp++;
        } else if ("MATEMATICA".equals(code)) {
          mat++;
        }
      }
      if (lp != lpExpected || mat != matExpected) {
        throw new ConflictException(
            "INCOMPLETE_EDITION",
            "Edição " + tag + " com divisão disciplinar divergente: esperado LP "
                + lpExpected + " + MAT " + matExpected + " × importado LP " + lp
                + " + MAT " + mat + ": NECESSITA REVISÃO antes de publicar simulado real.");
      }
    }
  }

  private List<String> editionNotes(Exam exam, int year) {
    String institution = exam.getInstitution() == null ? "IFRN" : exam.getInstitution();
    return editionNotes(exam, institution, year);
  }

  private List<String> editionNotes(Exam exam, String institution, int year) {
    List<String> notes = new ArrayList<>(6);
    notes.add("Simulado real da edição " + institution + " " + year + " (edital " + exam.getEdital()
        + "): caderno integral em ordem original (posições 1.." + exam.getObjectiveCount()
        + ", incluindo anuladas nas posições originais) — sem sorteio e sem filtro de dificuldade.");
    int cn = exam.getCnCount() == null ? 0 : exam.getCnCount().intValue();
    int ch = exam.getChCount() == null ? 0 : exam.getChCount().intValue();
    String structure = "LP " + exam.getLpCount() + " + MAT " + exam.getMatCount();
    if (cn + ch > 0) {
      structure += " + CN " + cn + " + CH " + ch;
    }
    notes.add("Estrutura dirigida pela configuração desta edição (" + structure + " objetivas"
        + (Boolean.TRUE.equals(exam.getHasEssay()) ? " + 1 produção textual" : "")
        + ", " + exam.getDurationMinutes() + " min)"
        + " — nunca regra universal.");
    if (Boolean.TRUE.equals(exam.getHasEssay())) {
      String essay = essayPrompts.findByExamInstitutionAndYear(institution, (short) year)
          .map(p -> p.getGenre() + " — " + p.getTheme() + " (pseudônimo " + p.getPseudonym() + ")")
          .orElse(null);
      notes.add(essay == null
          ? "Produção textual desta edição como referência (sem correção automática: pontuação DESCONHECIDA)."
          : "Produção textual desta edição como referência (" + essay
              + ") — sem correção automática (pontuação DESCONHECIDA, fora do MVP).");
    } else if ("EAJ".equals(institution)) {
      notes.add("Edição EAJ sem produção textual (has_essay FALSE nas 3 edições observadas).");
    }
    if (exam.getScoringRule() == null) {
      notes.add("scoring_rule DESCONHECIDA: anuladas fora do aproveitamento; regra de pontuação sem fonte oficial.");
    }
    if ("EAJ".equals(institution)) {
      notes.add("Gabarito EAJ = transcrição da curadoria (TRANSCRIBED_FROM_MD, sem PDF oficial no repo — ver docs/blockers.md).");
    }
    notes.add("Questões com revisão humana PENDENTE (TASK 12.2): classificação pedagógica nunca é verdade oficial do "
        + ("EAJ".equals(institution) ? "EAJ/UFRN (Comperve)." : "IFRN."));
    return notes;
  }

  private static String describeFilter(String disciplineCode, String difficulty, String sourceType) {
    return " em " + disciplineCode + (difficulty == null ? "" : " com dificuldade " + difficulty)
        + describeSourceType(sourceType);
  }

  private static String describeSourceType(String sourceType) {
    if (sourceType == null) {
      return " de todas as origens";
    }
    return switch (sourceType) {
      case "OFFICIAL" -> " oficiais";
      case "AUTHORAL" -> " autorais";
      default -> " (origem " + sourceType + ")";
    };
  }

  private static String title(String disciplineName, int count, String difficulty, String mode) {
    return "Simulado " + disciplineName + " — " + count + (count == 1 ? " questão" : " questões")
        + (difficulty == null ? "" : " " + difficulty) + " [" + mode + "]";
  }

  private static String titleEdition(String institution, int year, int count, String mode) {
    return "Simulado Edição " + institution + " " + year + " — " + count
        + (count == 1 ? " questão" : " questões") + " [" + mode + "]";
  }

  private static String titleEdition(int year, int count, String mode) {
    return titleEdition("IFRN", year, count, mode);
  }

  private static String filterJsonEdition(String institution, int year, String mode) {
    return "{\"type\":\"REAL_EDITION\",\"institution\":\"" + institution
        + "\",\"editionYear\":" + year + ",\"mode\":\"" + mode + "\"}";
  }

  private static String filterJsonEdition(int year, String mode) {
    return filterJsonEdition("IFRN", year, mode);
  }

  private static String filterJson(
      String disciplineCode, int count, String difficulty, String mode, String sourceType) {
    return "{\"type\":\"BY_DISCIPLINE\",\"discipline\":\"" + disciplineCode
        + "\",\"questionCount\":" + count
        + ",\"difficulty\":" + (difficulty == null ? "null" : "\"" + difficulty + "\"")
        + ",\"sourceType\":" + (sourceType == null ? "\"ALL\"" : "\"" + sourceType + "\"")
        + ",\"mode\":\"" + mode + "\"}";
  }

  private static String scoreJson(ScoredBoard board, String mode, String status) {
    return "{\"total\":" + board.total
        + ",\"answered\":" + board.answered
        + ",\"unanswered\":" + board.unanswered
        + ",\"scored\":" + board.scored
        + ",\"correct\":" + board.correct
        + ",\"incorrect\":" + board.incorrect
        + ",\"annulled\":" + board.annulled
        + ",\"accuracy\":" + (board.accuracy == null
            ? "null" : String.format(Locale.US, "%.4f", board.accuracy))
        + ",\"mode\":\"" + mode + "\",\"status\":\"" + status + "\"}";
  }

  private Map<Long, Question> questionsById(List<Long> ids) {
    Map<Long, Question> out = new HashMap<>(ids.size() * 2);
    for (Question q : questions.findAllById(ids)) {
      out.put(q.getId(), q);
    }
    return out;
  }

  private Map<Long, Simulation> simulationsById(List<Long> ids) {
    Map<Long, Simulation> out = new HashMap<>(ids.size() * 2);
    for (Simulation s : simulations.findAllById(ids)) {
      out.put(s.getId(), s);
    }
    return out;
  }

  private static Question requirePresent(Map<Long, Question> byId, Long id) {
    Question q = byId.get(id);
    if (q == null) {
      throw new IllegalStateException("Questão candidata " + id + " não encontrada.");
    }
    return q;
  }

  private static Map<Long, List<SimulationQuestion>> groupByAttempt(List<SimulationQuestion> rows) {
    Map<Long, List<SimulationQuestion>> out = new LinkedHashMap<>();
    for (SimulationQuestion r : rows) {
      out.computeIfAbsent(r.getSimulationAttemptId(), k -> new ArrayList<>()).add(r);
    }
    return out;
  }

  /**
   * Última tentativa por questão (maior {@code answeredAt}, desempate por
   * maior {@code id}) — o fato é imutável, correção só via nova tentativa.
   */
  private static Map<Long, QuestionAttempt> latestByQuestion(List<QuestionAttempt> all) {
    Map<Long, QuestionAttempt> out = new HashMap<>(all.size() * 2);
    for (QuestionAttempt a : all) {
      QuestionAttempt current = out.get(a.getQuestion().getId());
      if (current == null || compareRecency(a, current) > 0) {
        out.put(a.getQuestion().getId(), a);
      }
    }
    return out;
  }

  private static int compareRecency(QuestionAttempt a, QuestionAttempt b) {
    if (a.getAnsweredAt() == null && b.getAnsweredAt() == null) {
      return Long.compare(a.getId() == null ? 0L : a.getId(), b.getId() == null ? 0L : b.getId());
    }
    if (a.getAnsweredAt() == null) {
      return -1;
    }
    if (b.getAnsweredAt() == null) {
      return 1;
    }
    int cmp = a.getAnsweredAt().compareTo(b.getAnsweredAt());
    if (cmp != 0) {
      return cmp;
    }
    return Long.compare(a.getId() == null ? 0L : a.getId(), b.getId() == null ? 0L : b.getId());
  }

  /** Monta o placar a partir do caderno congelado + última resposta por questão. */
  private ScoredBoard scoreBoard(SimulationAttempt attempt) {
    List<SimulationQuestion> rows =
        caderno.findBySimulationAttemptIdOrderByPositionAsc(attempt.getId());
    List<Long> ids = rows.stream().map(r -> r.getQuestion().getId()).toList();
    Map<Long, Question> byId = questionsById(ids);
    Map<Long, QuestionAttempt> latest =
        latestByQuestion(responses.findBySimulationAttemptIdAndUserId(attempt.getId(), attempt.getUser().getId()));

    List<PerQuestion> items = new ArrayList<>(rows.size());
    long scored = 0;
    long correct = 0;
    long annulled = 0;
    long answered = 0;
    for (SimulationQuestion row : rows) {
      Question q = requirePresent(byId, row.getQuestion().getId());
      QuestionAttempt last = latest.get(q.getId());
      boolean isAnnulled = q.isAnnulled() || "X".equals(q.getAnswerKey())
          || (last != null && last.isAnnulled());
      Boolean isCorrect = null;
      if (last != null) {
        answered++;
        if (!isAnnulled) {
          scored++;
          isCorrect = last.getCorrect();
          if (Boolean.TRUE.equals(isCorrect)) {
            correct++;
          }
        } else {
          annulled++;
        }
      } else if (isAnnulled) {
        // Sem resposta e questão hoje anulada: conta no resumo como anulada,
        // sem pontuar (regra DESCONHECIDA) — nunca como erro inventado.
        annulled++;
      }
      items.add(new PerQuestion(row, q, last, isAnnulled, isCorrect));
    }
    long total = rows.size();
    long unanswered = total - answered;
    long incorrect = Math.max(0L, scored - correct);
    Double accuracy = scored == 0 ? null : correct / (double) scored;
    return new ScoredBoard(items, total, answered, unanswered, scored, correct, incorrect, annulled, accuracy);
  }

  private SimulationAttemptResponse loadAttemptResponse(SimulationAttempt attempt, boolean reveal) {
    List<SimulationQuestion> rows =
        caderno.findBySimulationAttemptIdOrderByPositionAsc(attempt.getId());
    List<Long> ids = rows.stream().map(r -> r.getQuestion().getId()).toList();
    Map<Long, Question> byId = questionsById(ids);
    Map<Long, QuestionAttempt> latest = Map.of();
    if (reveal) {
      latest = latestByQuestion(
          responses.findBySimulationAttemptIdAndUserId(attempt.getId(), attempt.getUser().getId()));
    } else {
      // Em andamento: só o conjunto de respondidas (progresso), sem correção.
      Set<Long> answeredIds = new HashSet<>();
      for (QuestionAttempt a : responses.findBySimulationAttemptIdAndUserId(
          attempt.getId(), attempt.getUser().getId())) {
        answeredIds.add(a.getQuestion().getId());
      }
      Map<Long, QuestionAttempt> flags = new HashMap<>();
      for (Long qid : answeredIds) {
        flags.put(qid, null);
      }
      latest = flags;
    }
    return toAttemptResponse(attempt, attempt.getSimulation(), rows, byId, latest, reveal);
  }

  private SimulationAttemptResponse toAttemptResponse(
      SimulationAttempt attempt,
      Simulation simulation,
      List<SimulationQuestion> rows,
      Map<Long, Question> byId,
      Map<Long, QuestionAttempt> latest,
      boolean reveal) {
    List<CadernoItem> items = new ArrayList<>(rows.size());
    String discCode = null;
    String discName = null;
    for (SimulationQuestion row : rows) {
      Question q = requirePresent(byId, row.getQuestion().getId());
      if (discCode == null) {
        discCode = q.getDiscipline().getCode();
        discName = q.getDiscipline().getName();
      }
      boolean answered = latest.containsKey(q.getId());
      QuestionAttempt last = latest.get(q.getId());
      boolean isAnnulled = q.isAnnulled() || "X".equals(q.getAnswerKey())
          || (last != null && last.isAnnulled());
      items.add(new CadernoItem(
          row.getPosition().intValue(),
          q.getId(),
          q.getDiscipline().getCode(),
          q.getSourceYear() == null ? null : q.getSourceYear().intValue(),
          q.getExam() != null ? q.getExam().getInstitution() : null,
          q.getSourceQuestionNumber() == null ? null : q.getSourceQuestionNumber().intValue(),
          answered,
          reveal ? row.getFrozenAnswerKey() : null,
          reveal && last != null ? last.getSelectedOption() : null,
          reveal && last != null && !isAnnulled ? last.getCorrect() : null,
          isAnnulled && answered));
    }

    ScoreSummary score = null;
    if (reveal) {
      ScoredBoard board = scoreBoard(attempt);
      score = new ScoreSummary(board.total, board.answered, board.unanswered,
          board.scored, board.correct, board.incorrect, board.annulled, board.accuracy);
    }

    boolean realEdition = "REAL_EDITION".equals(simulation.getType());
    List<String> notes = attemptNotes(attempt, simulation.getType(), reveal);
    return new SimulationAttemptResponse(
        attempt.getId(), simulation.getId(), simulation.getType(), simulation.getTitle(),
        realEdition ? null : discCode, realEdition ? null : discName,
        attempt.getMode(), attempt.getStatus(), rows.size(),
        attempt.getStartedAt(), attempt.getSubmittedAt(),
        List.copyOf(items), score, List.copyOf(notes));
  }

  private SimulationResultResponse toResultResponse(
      SimulationAttempt attempt, ScoredBoard board, boolean abandoned) {
    List<ResultItem> items = new ArrayList<>(board.items.size());
    String discCode = null;
    for (PerQuestion p : board.items) {
      Question q = p.question;
      if (discCode == null) {
        discCode = q.getDiscipline().getCode();
      }
      boolean unanswered = p.last == null;
      items.add(new ResultItem(
          p.row.getPosition().intValue(),
          q.getId(),
          q.getDiscipline().getCode(),
          q.getSourceYear() == null ? null : q.getSourceYear().intValue(),
          q.getExam() != null ? q.getExam().getInstitution() : null,
          q.getSourceQuestionNumber() == null ? null : q.getSourceQuestionNumber().intValue(),
          p.last == null ? null : p.last.getSelectedOption(),
          p.isCorrect,
          p.wasAnnulled,
          p.row.getFrozenAnswerKey(),
          unanswered));
    }
    List<String> notes = resultNotes(attempt, board, abandoned);
    boolean realEdition = "REAL_EDITION".equals(attempt.getSimulation().getType());
    return new SimulationResultResponse(
        attempt.getId(), attempt.getSimulation().getId(), attempt.getSimulation().getTitle(),
        realEdition ? null : discCode, attempt.getMode(), attempt.getStatus(),
        board.total, board.answered, board.unanswered, board.scored,
        board.correct, board.incorrect, board.annulled, board.accuracy,
        List.copyOf(items), List.copyOf(notes));
  }

  private static List<String> attemptNotes(SimulationAttempt attempt, String type, boolean reveal) {
    List<String> notes = new ArrayList<>();
    if ("REAL_EDITION".equals(type)) {
      notes.add("Caderno integral da edição em ordem original (posições 1..N, sem sorteio); "
          + "anuladas participam nas posições originais e ficam fora do aproveitamento.");
    }
    if (!reveal) {
      notes.add("Gabarito oculto durante a execução (preserva a sensação de prova). "
          + "No Modo Estudo o feedback imediato vem de POST /attempts; "
          + "no Modo Prova a correção aparece só após concluir.");
      notes.add("Sem estado de pausa no servidor: retome este caderno via GET até concluir ou abandonar.");
    } else {
      notes.add("Caderno congelado na criação (posição + gabarito vigente): a correção usa o "
          + "frozen_answer_key, nunca o gabarito atual.");
    }
    notes.add("Responda via POST /attempts com simulationAttemptId=" + attempt.getId()
        + " (correção do servidor, TASK 3.7); após encerrar, novas respostas retornam 409 SIMULATION_CLOSED.");
    notes.add("Anuladas ficam fora do aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4).");
    notes.add("Enunciados em GET /api/v1/questions/{id} (este caderno referencia, nunca duplica).");
    return notes;
  }

  private static List<String> feedbackNotes(
      SimulationAttempt attempt, int total, Question question,
      QuestionClassification classification, boolean wasAnnulled) {
    List<String> notes = new ArrayList<>();
    notes.add("Correção contra o gabarito congelado na criação (frozen_answer_key), "
        + "consistente com o placar do simulado mesmo após reclassificação posterior.");
    if (wasAnnulled) {
      notes.add("Questão anulada: conta como conteúdo respondido e fica fora do "
          + "aproveitamento; regra de pontuação DESCONHECIDA (TASK 1.3 §4).");
    }
    if (classification == null) {
      notes.add("Sem classificação pedagógica vigente: assunto NÃO CONFIRMADO.");
    } else if ("BAIXA".equals(classification.getConfidence())) {
      notes.add("Classificação pedagógica " + classification.getTaxonomyVersion()
          + " com confiança BAIXA: assunto NÃO CONFIRMADO — nunca verdade oficial do IFRN.");
    }
    notes.add("Enunciado e alternativas em GET /api/v1/questions/" + question.getId()
        + " (este feedback referencia, nunca duplica).");
    if (!"IN_PROGRESS".equals(attempt.getStatus())) {
      notes.add("Execução " + attempt.getStatus() + ": o placar completo está em "
          + "GET /api/v1/simulations/attempts/" + attempt.getId() + "/result.");
    } else {
      notes.add("Posição respondida de um caderno com " + total + " posições: "
          + "retome via GET /api/v1/simulations/attempts/" + attempt.getId() + " até concluir.");
    }
    return notes;
  }

  private static List<String> resultNotes(
      SimulationAttempt attempt, ScoredBoard board, boolean abandoned) {
    List<String> notes = new ArrayList<>();
    if (abandoned) {
      notes.add("Execução abandonada: placar parcial (vale a última resposta por questão).");
    } else {
      notes.add("Execução concluída: vale a última tentativa por questão vinculada a esta execução.");
    }
    if (board.unanswered > 0) {
      notes.add(board.unanswered + " questão(ões) sem resposta: contam como não respondidas, nunca como erro inventado.");
    }
    if (board.annulled > 0) {
      notes.add("Anuladas (" + board.annulled + ") ficam fora do aproveitamento; regra de pontuação DESCONHECIDA (TASK 1.3 §4).");
    }
    if (board.scored == 0) {
      notes.add("Sem tentativas pontuáveis: aproveitamento NULL (nunca zero inventado).");
    }
    notes.add("Confronto contra o gabarito congelado (frozen_answer_key), não contra o gabarito atual.");
    notes.add("Modo " + attempt.getMode() + ": o diagnóstico (TASK 4.2) e a revisão (TASK 4.5) "
        + "já incorporam estas respostas via question_attempts.");
    return notes;
  }

  /** Uma posição do caderno com a última resposta resolvida. */
  private static final class PerQuestion {
    final SimulationQuestion row;
    final Question question;
    final QuestionAttempt last;
    final boolean wasAnnulled;
    final Boolean isCorrect;

    PerQuestion(SimulationQuestion row, Question question, QuestionAttempt last,
        boolean wasAnnulled, Boolean isCorrect) {
      this.row = row;
      this.question = question;
      this.last = last;
      this.wasAnnulled = wasAnnulled;
      this.isCorrect = isCorrect;
    }
  }

  /** Placar calculado no servidor (nunca vindo do cliente). */
  private static final class ScoredBoard {
    final List<PerQuestion> items;
    final long total;
    final long answered;
    final long unanswered;
    final long scored;
    final long correct;
    final long incorrect;
    final long annulled;
    final Double accuracy;

    ScoredBoard(List<PerQuestion> items, long total, long answered, long unanswered,
        long scored, long correct, long incorrect, long annulled, Double accuracy) {
      this.items = items;
      this.total = total;
      this.answered = answered;
      this.unanswered = unanswered;
      this.scored = scored;
      this.correct = correct;
      this.incorrect = incorrect;
      this.annulled = annulled;
      this.accuracy = accuracy;
    }
  }

}
