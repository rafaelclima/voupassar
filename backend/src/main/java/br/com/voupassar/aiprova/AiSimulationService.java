package br.com.voupassar.aiprova;

import br.com.voupassar.aiprova.dto.AiAnswerRequest;
import br.com.voupassar.aiprova.dto.AiAnswerResponse;
import br.com.voupassar.aiprova.dto.AiAttemptResponse;
import br.com.voupassar.aiprova.dto.AiAttemptResponse.AiCadernoItem;
import br.com.voupassar.aiprova.dto.AiAttemptResponse.AiScoreSummary;
import br.com.voupassar.aiprova.dto.AiFeedbackResponse;
import br.com.voupassar.aiprova.dto.AiGenerateRequest;
import br.com.voupassar.aiprova.dto.AiResultResponse;
import br.com.voupassar.aiprova.dto.AiResultResponse.AiResultItem;
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
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.simulations.entity.Simulation;
import br.com.voupassar.simulations.entity.SimulationAttempt;
import br.com.voupassar.simulations.repository.SimulationAttemptRepository;
import br.com.voupassar.simulations.repository.SimulationRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Prova-IA (TASK A.3, Fase A: provedor mockado determinístico, sem chamada
 * real à LLM).
 *
 * <p>Reutiliza o ciclo de {@code simulations}/{@code simulation_attempts}
 * (TASKS.md decisão 5) SOBRE o caderno-IA congelado: criar (201, gabarito
 * oculto em andamento), retomar via GET, responder, concluir, abandonar,
 * resultado e feedback por posição (ESTUDO imediato; PROVA só após
 * encerrar). Regras espelhadas do ciclo oficial:
 * <ul>
 *   <li>Correção 100% no servidor contra o gabarito <b>congelado</b>; vale
 *       a <b>última</b> resposta por posição; sem resposta = não
 *       respondida, nunca erro inventado.</li>
 *   <li>Diferenças honestas para o oficial: caderno INLINE (enunciado +
 *       alternativas geradas; sem {@code GET /questions} para IA —
 *       isolamento); selo experimental por questão e no placar; sem
 *       ano/número oficial (formato <i>inspirado em</i>, nunca
 *       <i>extraído de</i>); sem anuladas; nada-IA alimenta o motor
 *       educacional.</li>
 *   <li>Falha do provedor/validação → erro em envelope com {@code traceId},
 *       sem caderno parcial (TASKS.md regra 5): a validação acontece ANTES
 *       de qualquer escrita.</li>
 * </ul>
 */
@Service
public class AiSimulationService {

  /** Tipo de simulação da prova-IA (DDL V23). */
  public static final String AI_TYPE = "AI_GENERATED_EDITION";

  static final String AI_QUESTION_NOTE =
      "Questão gerada por IA — não é questão oficial IFRN/EAJ.";
  static final String AI_SCORE_NOTE =
      "Placar sobre gabarito gerado por IA — sem validade de prova real.";
  static final String AI_ISOLATION_NOTE =
      "Prova-IA experimental: este placar não alimenta diagnóstico, roteiro, revisão nem mapa de conteúdo.";

  private static final Set<String> MODES = Set.of("ESTUDO", "PROVA");
  private static final Set<String> OPTIONS = Set.of("A", "B", "C", "D", "BLANK");
  private static final List<String> ANSWER_KEYS = List.of("A", "B", "C", "D");

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final Logger log = LoggerFactory.getLogger(AiSimulationService.class);

  private final UserRepository users;
  private final ExamRepository exams;
  private final SimulationRepository simulations;
  private final SimulationAttemptRepository attempts;
  private final AiGenerationRepository generations;
  private final AiGenerationQuestionRepository cadernoIa;
  private final AiGenerationAnswerRepository answersIa;
  private final AiProvaProvider provider;

  public AiSimulationService(
      UserRepository users,
      ExamRepository exams,
      SimulationRepository simulations,
      SimulationAttemptRepository attempts,
      AiGenerationRepository generations,
      AiGenerationQuestionRepository cadernoIa,
      AiGenerationAnswerRepository answersIa,
      AiProvaProvider provider) {
    this.users = users;
    this.exams = exams;
    this.simulations = simulations;
    this.attempts = attempts;
    this.generations = generations;
    this.cadernoIa = cadernoIa;
    this.answersIa = answersIa;
    this.provider = provider;
  }

  /**
   * Gera e inicia uma prova-IA (caderno congelado, {@code IN_PROGRESS}).
   *
   * @return execução com o caderno-IA (gabarito oculto)
   */
  @Transactional
  public AiAttemptResponse createAiSimulation(long userId, AiGenerateRequest req) {
    User user = requireActiveUser(userId);
    String institution = normalizeInstitutionRequired(req == null ? null : req.institution());
    String mode = normalizeMode(req == null ? null : req.mode());
    String editionModel = resolveEditionModel(
        institution, req == null ? null : req.editionModel());

    // Fonte dos bytes (mock na Fase A; OpenRouter na C.1) + validação
    // estrutural ANTES de qualquer escrita (nunca caderno parcial).
    String rawJson = provider.generate(editionModel);
    AiProvaValidator.ValidatedBatch batch = AiProvaValidator.validate(rawJson);
    List<FrozenItem> frozen = parseFrozen(rawJson, batch.size());

    AiGeneration generation = new AiGeneration();
    generation.setUser(user);
    generation.setInstitution(institution);
    generation.setEditionModel(editionModel);
    generation.setMode(mode);
    generation.setPromptVersion(AiProvaPrompts.PROMPT_VERSION);
    generation.setModel(provider.modelId());
    generation.setStatus("READY");
    generation.setRepairAttempts(0);
    generation.setFrozenAt(OffsetDateTime.now());
    generations.save(generation);

    List<AiGenerationQuestion> rows = new ArrayList<>(frozen.size());
    for (FrozenItem item : frozen) {
      AiGenerationQuestion row = new AiGenerationQuestion();
      row.setGenerationId(generation.getId());
      row.setPosition((short) item.position());
      row.setDiscipline(item.discipline());
      row.setTopic(item.topic());
      row.setStem(item.stem());
      row.setOptionsJson(item.optionsJson());
      row.setFrozenAnswerKey(item.answer());
      rows.add(row);
    }
    cadernoIa.saveAll(rows);

    Simulation simulation = new Simulation();
    simulation.setOwner(user);
    simulation.setType(AI_TYPE);
    simulation.setExamId(null);
    simulation.setAiGenerationId(generation.getId());
    simulation.setFilterJson(filterJson(institution, editionModel, mode,
        AiProvaPrompts.PROMPT_VERSION, provider.modelId()));
    simulation.setTitle(title(institution, editionModel, mode));
    simulations.save(simulation);

    SimulationAttempt attempt = new SimulationAttempt();
    attempt.setSimulation(simulation);
    attempt.setUser(user);
    attempt.setMode(mode);
    attempt.setStatus("IN_PROGRESS");
    attempt.setStartedAt(OffsetDateTime.now());
    attempts.save(attempt);

    log.info("prova-ia criada user_id={} attempt_id={} generation_id={} editionModel={} mode={} model={}",
        userId, attempt.getId(), generation.getId(), editionModel, mode, provider.modelId());
    return toAttemptResponse(attempt, simulation, generation, rows, Map.of(), false);
  }

  /** Consulta uma execução-IA do dono do token (retomar ou rever). */
  @Transactional(readOnly = true)
  public AiAttemptResponse getAiAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    LoadedAttempt loaded = requireAiAttempt(userId, attemptId);
    return toAttemptResponse(loaded.attempt(), loaded.simulation(), loaded.generation(),
        loaded.rows(), latestByPosition(loaded.attemptId(), userId),
        !"IN_PROGRESS".equals(loaded.attempt().getStatus()));
  }

  /** Responde uma posição do caderno-IA (fato próprio, nunca question_attempts). */
  @Transactional
  public AiAnswerResponse answerPosition(long userId, long attemptId, AiAnswerRequest req) {
    requireActiveUser(userId);
    LoadedAttempt loaded = requireAiAttempt(userId, attemptId);
    requireInProgress(loaded.attempt());
    AiGenerationQuestion row = requirePosition(loaded.rows(), req == null ? null : req.position());
    String selected = normalizeOption(req == null ? null : req.selectedOption());

    AiGenerationAnswer answer = new AiGenerationAnswer();
    answer.setUser(loaded.attempt().getUser());
    answer.setGenerationId(loaded.generation().getId());
    answer.setPosition(row.getPosition());
    answer.setSimulationAttempt(loaded.attempt());
    answer.setSelectedOption(selected);
    answer.setCorrect(selected.equals(row.getFrozenAnswerKey()));
    answer.setTimeSpentSeconds(req == null ? null : req.timeSpentSeconds());
    answer.setMode(loaded.attempt().getMode());
    answer.setAnsweredAt(OffsetDateTime.now());
    answersIa.save(answer);

    // Mesmo contrato de ocultação do oficial: em PROVA em andamento o
    // resultado fica oculto; o valor persistido reaparece após encerrar.
    boolean hidden = "PROVA".equals(loaded.attempt().getMode());
    List<String> notes = new ArrayList<>(3);
    notes.add(hidden
        ? "Modo Prova: resultado oculto durante a execução (preserva a sensação de prova); conclua ou abandone para ver a correção."
        : "Modo Estudo: correção imediata contra o gabarito-IA congelado (sem garantia pedagógica).");
    if ("BLANK".equals(selected)) {
      notes.add("Resposta em branco: conta como erro (posição pontuável).");
    }
    notes.add(AI_QUESTION_NOTE);
    log.info("resposta-ia user_id={} attempt_id={} position={} mode={} hidden={}",
        userId, attemptId, row.getPosition(), loaded.attempt().getMode(), hidden);
    return new AiAnswerResponse(answer.getId(), attemptId, row.getPosition().intValue(),
        selected, hidden ? null : answer.isCorrect(),
        hidden ? null : row.getFrozenAnswerKey(), true, List.copyOf(notes));
  }

  /** Conclui a execução-IA ({@code IN_PROGRESS → SUBMITTED}) com o placar do servidor. */
  @Transactional
  public AiResultResponse submitAiAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    LoadedAttempt loaded = requireAiAttempt(userId, attemptId);
    requireInProgress(loaded.attempt());
    loaded.attempt().setStatus("SUBMITTED");
    loaded.attempt().setSubmittedAt(OffsetDateTime.now());
    AiScoredBoard board = scoreBoard(loaded);
    loaded.attempt().setScoreJson(scoreJson(board, loaded.attempt().getMode(), "SUBMITTED"));
    attempts.save(loaded.attempt());

    log.info("prova-ia concluída user_id={} attempt_id={} scored={} correct={}",
        userId, attemptId, board.scored, board.correct);
    return toResultResponse(loaded, board, false);
  }

  /** Desiste da execução-IA ({@code IN_PROGRESS → ABANDONED}) com o placar parcial. */
  @Transactional
  public AiResultResponse abandonAiAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    LoadedAttempt loaded = requireAiAttempt(userId, attemptId);
    requireInProgress(loaded.attempt());
    loaded.attempt().setStatus("ABANDONED");
    loaded.attempt().setSubmittedAt(OffsetDateTime.now());
    AiScoredBoard board = scoreBoard(loaded);
    loaded.attempt().setScoreJson(scoreJson(board, loaded.attempt().getMode(), "ABANDONED"));
    attempts.save(loaded.attempt());

    log.info("prova-ia abandonada user_id={} attempt_id={} answered={}",
        userId, attemptId, board.answered);
    return toResultResponse(loaded, board, true);
  }

  /** Visualiza o resultado-IA (só após concluir ou abandonar). */
  @Transactional(readOnly = true)
  public AiResultResponse getAiResult(long userId, long attemptId) {
    requireActiveUser(userId);
    LoadedAttempt loaded = requireAiAttempt(userId, attemptId);
    if ("IN_PROGRESS".equals(loaded.attempt().getStatus())) {
      throw new ConflictException(
          "SIMULATION_NOT_FINISHED",
          "Prova-IA ainda em andamento: conclua ou abandone para ver o resultado (gabarito oculto durante a execução).");
    }
    return toResultResponse(loaded, scoreBoard(loaded),
        "ABANDONED".equals(loaded.attempt().getStatus()));
  }

  /**
   * Feedback imediato de uma posição-IA: acerto/erro, resposta do gabarito
   * congelado e assunto rotulado, a partir da <b>última</b> resposta. No
   * Modo ESTUDO em qualquer status; no Modo PROVA só após encerrar.
   */
  @Transactional(readOnly = true)
  public AiFeedbackResponse getAiFeedback(long userId, long attemptId, int position) {
    requireActiveUser(userId);
    LoadedAttempt loaded = requireAiAttempt(userId, attemptId);
    if (position < 1) {
      throw new BadRequestException(
          "INVALID_POSITION", "Posição inválida: " + position + " (a primeira posição é 1).");
    }
    if ("PROVA".equals(loaded.attempt().getMode())
        && "IN_PROGRESS".equals(loaded.attempt().getStatus())) {
      throw new ConflictException(
          "STUDY_FEEDBACK_UNAVAILABLE",
          "Feedback imediato indisponível durante a execução no Modo Prova: "
              + "conclua ou abandone para ver a correção (o gabarito fica oculto até encerrar).");
    }
    AiGenerationQuestion row = null;
    for (AiGenerationQuestion r : loaded.rows()) {
      if (r.getPosition() != null && r.getPosition() == position) {
        row = r;
        break;
      }
    }
    if (row == null) {
      throw new ResourceNotFoundException(
          "SIMULATION_QUESTION_NOT_FOUND",
          "Posição " + position + " não encontrada nesta prova-IA (" + loaded.rows().size() + " posições).");
    }
    Map<Integer, AiGenerationAnswer> latest = latestByPosition(loaded.attemptId(), userId);
    AiGenerationAnswer last = latest.get(position);
    if (last == null) {
      throw new ConflictException(
          "FEEDBACK_NOT_AVAILABLE",
          "Posição " + position + " ainda sem resposta nesta execução: "
              + "responda via POST /api/v1/simulations/ai/attempts/" + attemptId
              + "/answers antes de ver o feedback.");
    }
    List<String> notes = new ArrayList<>(3);
    notes.add("Correção contra o gabarito-IA congelado na geração (sem garantia pedagógica).");
    notes.add(AI_QUESTION_NOTE);
    if (!"IN_PROGRESS".equals(loaded.attempt().getStatus())) {
      notes.add("Execução " + loaded.attempt().getStatus() + ": o placar completo está em "
          + "GET /api/v1/simulations/ai/attempts/" + attemptId + "/result.");
    }
    return new AiFeedbackResponse(attemptId, position, row.getDiscipline(), row.getTopic(),
        last.getSelectedOption(), last.isCorrect(), row.getFrozenAnswerKey(), true, List.copyOf(notes));
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

  /**
   * Execução-IA do dono do token com geração e caderno resolvidos.
   * Inexistente, de outro aluno ou de simulado oficial → 404 sem
   * distinguir (mesmo padrão anti-enumeração do ciclo oficial).
   */
  private LoadedAttempt requireAiAttempt(long userId, long attemptId) {
    SimulationAttempt attempt = attempts.findByIdAndUserId(attemptId, userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "SIMULATION_ATTEMPT_NOT_FOUND", "Execução de simulado não encontrada."));
    Simulation simulation = attempt.getSimulation();
    if (simulation == null || !AI_TYPE.equals(simulation.getType())
        || simulation.getAiGenerationId() == null) {
      throw new ResourceNotFoundException(
          "SIMULATION_ATTEMPT_NOT_FOUND", "Execução de simulado não encontrada.");
    }
    AiGeneration generation = generations.findById(simulation.getAiGenerationId())
        .orElseThrow(() -> new IllegalStateException(
            "Geração-IA " + simulation.getAiGenerationId() + " ausente para a execução " + attemptId));
    List<AiGenerationQuestion> rows =
        cadernoIa.findByGenerationIdOrderByPositionAsc(generation.getId());
    return new LoadedAttempt(attempt, simulation, generation, rows);
  }

  private static void requireInProgress(SimulationAttempt attempt) {
    if (!"IN_PROGRESS".equals(attempt.getStatus())) {
      throw new ConflictException(
          "SIMULATION_CLOSED", "Simulado já encerrado (status " + attempt.getStatus() + ").");
    }
  }

  private static String normalizeInstitutionRequired(String institution) {
    if (institution == null || institution.isBlank()) {
      throw new BadRequestException(
          "INVALID_INSTITUTION",
          "Processo seletivo é obrigatório (permitido IFRN, EAJ).");
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
   * Edição-modelo: ausente = mais recente da trilha no banco (nunca
   * inventada); informada e desconhecida → 404; de outra trilha → 404
   * (não pertence a este processo).
   */
  private String resolveEditionModel(String institution, String editionModel) {
    if (editionModel == null || editionModel.isBlank()) {
      List<Exam> track = exams.findByInstitutionOrderByYearAsc(institution);
      if (track.isEmpty()) {
        throw new ResourceNotFoundException(
            "EDITION_NOT_FOUND",
            "Nenhuma edição " + institution + " no banco: edição-modelo exigida (nunca inventar estrutura).");
      }
      Short latest = track.get(track.size() - 1).getYear();
      return institution + "-" + latest.intValue();
    }
    String normalized = editionModel.trim().toUpperCase();
    AiProvaSpec.EditionSpec spec = AiProvaSpec.MODELS.get(normalized);
    if (spec == null) {
      throw new ResourceNotFoundException(
          "EDITION_NOT_FOUND",
          "Edição-modelo desconhecida: " + editionModel
              + " (só edições presentes — IFRN-2021 e EAJ-2023/2024/2026 não existem).");
    }
    if (!normalized.startsWith(institution)) {
      throw new ResourceNotFoundException(
          "EDITION_NOT_FOUND",
          "Edição-modelo " + normalized + " não pertence à trilha " + institution + ".");
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

  private static String normalizeOption(String option) {
    String o = option == null ? "" : option.trim().toUpperCase();
    if (!OPTIONS.contains(o)) {
      throw new BadRequestException("INVALID_OPTION", "Resposta deve ser A, B, C, D ou BLANK.");
    }
    return o;
  }

  private static AiGenerationQuestion requirePosition(List<AiGenerationQuestion> rows, Integer position) {
    if (position == null || position < 1) {
      throw new BadRequestException(
          "INVALID_POSITION", "Posição inválida: " + position + " (a primeira posição é 1).");
    }
    for (AiGenerationQuestion row : rows) {
      if (row.getPosition() != null && row.getPosition().intValue() == position) {
        return row;
      }
    }
    throw new ResourceNotFoundException(
        "SIMULATION_QUESTION_NOT_FOUND",
        "Posição " + position + " não encontrada nesta prova-IA (" + rows.size() + " posições).");
  }

  /** Item congelável extraído do lote validado (validador já garantiu o contrato). */
  private record FrozenItem(int position, String discipline, String topic,
      String stem, String optionsJson, String answer) {}

  private static List<FrozenItem> parseFrozen(String rawJson, int size) {
    JsonNode root;
    try {
      root = MAPPER.readTree(rawJson);
    } catch (Exception e) {
      throw new IllegalStateException("Lote validado não parseável (inconsistência interna).", e);
    }
    List<FrozenItem> out = new ArrayList<>(size);
    for (JsonNode q : root.get("questions")) {
      StringBuilder options = new StringBuilder("{");
      for (int i = 0; i < ANSWER_KEYS.size(); i++) {
        String key = ANSWER_KEYS.get(i);
        if (i > 0) {
          options.append(",");
        }
        options.append("\"").append(key).append("\":\"")
            .append(escape(q.get("options").get(key).stringValue())).append("\"");
      }
      options.append("}");
      out.add(new FrozenItem(
          q.get("n").intValue(),
          q.get("discipline").stringValue(),
          q.get("topic").stringValue(),
          q.get("stem").stringValue(),
          options.toString(),
          q.get("answer").stringValue()));
    }
    out.sort((a, b) -> Integer.compare(a.position(), b.position()));
    return out;
  }

  private static String escape(String value) {
    return value.replace("\\", "\\\\").replace("\"", "\\\"");
  }

  private static Map<String, String> optionsMap(String optionsJson) {
    try {
      JsonNode node = MAPPER.readTree(optionsJson);
      Map<String, String> out = new LinkedHashMap<>(4);
      for (String key : ANSWER_KEYS) {
        out.put(key, node.get(key).stringValue());
      }
      return out;
    } catch (Exception e) {
      throw new IllegalStateException("options_json do caderno congelado não parseável.", e);
    }
  }

  /**
   * Última resposta por posição (maior {@code answeredAt}, desempate por
   * maior {@code id}) — o fato é imutável, correção só via nova resposta.
   */
  private Map<Integer, AiGenerationAnswer> latestByPosition(long attemptId, long userId) {
    List<AiGenerationAnswer> all =
        answersIa.findBySimulationAttemptIdAndUserIdOrderByAnsweredAtAscIdAsc(attemptId, userId);
    Map<Integer, AiGenerationAnswer> out = new HashMap<>(all.size() * 2);
    for (AiGenerationAnswer a : all) {
      int pos = a.getPosition().intValue();
      AiGenerationAnswer current = out.get(pos);
      if (current == null || compareRecency(a, current) > 0) {
        out.put(pos, a);
      }
    }
    return out;
  }

  private static int compareRecency(AiGenerationAnswer a, AiGenerationAnswer b) {
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

  /** Placar contra o caderno-IA congelado (sem anuladas: IA nunca tem X). */
  private AiScoredBoard scoreBoard(LoadedAttempt loaded) {
    Map<Integer, AiGenerationAnswer> latest =
        latestByPosition(loaded.attemptId(), loaded.attempt().getUser().getId());
    List<AiScoredItem> items = new ArrayList<>(loaded.rows().size());
    long answered = 0;
    long correct = 0;
    for (AiGenerationQuestion row : loaded.rows()) {
      AiGenerationAnswer last = latest.get(row.getPosition().intValue());
      Boolean isCorrect = null;
      if (last != null) {
        answered++;
        isCorrect = last.isCorrect();
        if (isCorrect) {
          correct++;
        }
      }
      items.add(new AiScoredItem(row, last, isCorrect));
    }
    long total = loaded.rows().size();
    long unanswered = total - answered;
    long incorrect = Math.max(0L, answered - correct);
    Double accuracy = answered == 0 ? null : correct / (double) answered;
    return new AiScoredBoard(items, total, answered, unanswered, answered, correct, incorrect, accuracy);
  }

  private AiAttemptResponse toAttemptResponse(
      SimulationAttempt attempt,
      Simulation simulation,
      AiGeneration generation,
      List<AiGenerationQuestion> rows,
      Map<Integer, AiGenerationAnswer> latest,
      boolean reveal) {
    List<AiCadernoItem> items = new ArrayList<>(rows.size());
    for (AiGenerationQuestion row : rows) {
      AiGenerationAnswer last = latest.get(row.getPosition().intValue());
      items.add(new AiCadernoItem(
          row.getPosition().intValue(),
          row.getDiscipline(),
          row.getTopic(),
          row.getStem(),
          optionsMap(row.getOptionsJson()),
          last != null,
          reveal ? row.getFrozenAnswerKey() : null,
          reveal && last != null ? last.getSelectedOption() : null,
          reveal && last != null ? last.isCorrect() : null,
          true,
          AI_QUESTION_NOTE));
    }
    AiScoreSummary score = null;
    if (reveal) {
      AiScoredBoard board = scoreBoard(
          new LoadedAttempt(attempt, simulation, generation, rows));
      score = new AiScoreSummary(board.total, board.answered, board.unanswered,
          board.scored, board.correct, board.incorrect, board.accuracy);
    }
    List<String> notes = new ArrayList<>(4);
    notes.add("Prova-IA experimental (" + AI_TYPE + "): caderno gerado por IA no formato "
        + generation.getEditionModel() + " (" + rows.size() + " posições) — "
        + AI_SCORE_NOTE);
    if (!reveal) {
      notes.add("Gabarito oculto durante a execução (preserva a sensação de prova). "
          + (attempt.getMode().equals("ESTUDO")
              ? "No Modo Estudo a correção de cada resposta vem imediata na rota de respostas."
              : "No Modo Prova a correção aparece só após concluir."));
      notes.add("Sem estado de pausa no servidor: retome este caderno via GET até concluir ou abandonar.");
    } else {
      notes.add("Caderno congelado na geração (posição + gabarito-IA): a correção usa o "
          + "frozen_answer_key, nunca outro gabarito.");
    }
    notes.add("Responda via POST /api/v1/simulations/ai/attempts/" + attempt.getId()
        + "/answers (fato próprio; após encerrar, novas respostas retornam 409 SIMULATION_CLOSED).");
    return new AiAttemptResponse(
        attempt.getId(), simulation.getId(), generation.getId(), simulation.getType(),
        simulation.getTitle(), generation.getInstitution(), generation.getEditionModel(),
        attempt.getMode(), attempt.getStatus(), rows.size(),
        attempt.getStartedAt(), attempt.getSubmittedAt(),
        List.copyOf(items), score, List.copyOf(notes));
  }

  private AiResultResponse toResultResponse(LoadedAttempt loaded, AiScoredBoard board, boolean abandoned) {
    List<AiResultItem> items = new ArrayList<>(board.items.size());
    for (AiScoredItem item : board.items) {
      items.add(new AiResultItem(
          item.row().getPosition().intValue(),
          item.row().getDiscipline(),
          item.row().getTopic(),
          item.last() == null ? null : item.last().getSelectedOption(),
          item.isCorrect(),
          item.row().getFrozenAnswerKey(),
          item.last() == null,
          true));
    }
    List<String> notes = new ArrayList<>(6);
    notes.add(AI_SCORE_NOTE);
    notes.add(abandoned
        ? "Execução abandonada: placar parcial (vale a última resposta por posição)."
        : "Execução concluída: vale a última resposta por posição.");
    if (board.unanswered > 0) {
      notes.add(board.unanswered + " posição(ões) sem resposta: contam como não respondidas, nunca como erro inventado.");
    }
    if (board.scored == 0) {
      notes.add("Sem posições pontuáveis: aproveitamento NULL (nunca zero inventado).");
    }
    notes.add("Confronto contra o gabarito-IA congelado (frozen_answer_key).");
    notes.add(AI_ISOLATION_NOTE);
    SimulationAttempt attempt = loaded.attempt();
    return new AiResultResponse(
        attempt.getId(), loaded.simulation().getId(), loaded.generation().getId(),
        loaded.simulation().getTitle(), loaded.generation().getInstitution(),
        loaded.generation().getEditionModel(), attempt.getMode(), attempt.getStatus(),
        board.total, board.answered, board.unanswered, board.scored,
        board.correct, board.incorrect, board.accuracy,
        List.copyOf(items), List.copyOf(notes));
  }

  private static String title(String institution, String editionModel, String mode) {
    return "Prova-IA " + editionModel + " (experimental) [" + mode + "]";
  }

  private static String filterJson(
      String institution, String editionModel, String mode, String promptVersion, String model) {
    return "{\"type\":\"" + AI_TYPE + "\",\"institution\":\"" + institution
        + "\",\"editionModel\":\"" + editionModel + "\",\"mode\":\"" + mode
        + "\",\"promptVersion\":\"" + promptVersion + "\",\"model\":\"" + model
        + "\",\"experimental\":true}";
  }

  private static String scoreJson(AiScoredBoard board, String mode, String status) {
    return "{\"total\":" + board.total
        + ",\"answered\":" + board.answered
        + ",\"unanswered\":" + board.unanswered
        + ",\"scored\":" + board.scored
        + ",\"correct\":" + board.correct
        + ",\"incorrect\":" + board.incorrect
        + ",\"accuracy\":" + (board.accuracy == null
            ? "null" : String.format(Locale.US, "%.4f", board.accuracy))
        + ",\"mode\":\"" + mode + "\",\"status\":\"" + status + "\"}";
  }

  /** Execução-IA resolvida (tentativa + simulação + geração + caderno). */
  private record LoadedAttempt(SimulationAttempt attempt, Simulation simulation,
      AiGeneration generation, List<AiGenerationQuestion> rows) {
    long attemptId() {
      return attempt.getId();
    }
  }

  /** Uma posição-IA com a última resposta resolvida. */
  private record AiScoredItem(AiGenerationQuestion row, AiGenerationAnswer last, Boolean isCorrect) {}

  /** Placar calculado no servidor (nunca vindo do cliente). */
  private record AiScoredBoard(List<AiScoredItem> items, long total, long answered,
      long unanswered, long scored, long correct, long incorrect, Double accuracy) {}
}
