package br.com.voupassar.attempts.service;

import br.com.voupassar.attempts.dto.AttemptResponse;
import br.com.voupassar.attempts.dto.CreateAttemptRequest;
import br.com.voupassar.attempts.dto.CreateStudySessionRequest;
import br.com.voupassar.attempts.dto.StudySessionResponse;
import br.com.voupassar.attempts.entity.SimulationAttemptRef;
import br.com.voupassar.attempts.entity.StudySession;
import br.com.voupassar.attempts.repository.SimulationAttemptRefRepository;
import br.com.voupassar.attempts.repository.StudySessionRepository;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.performance.service.PerformanceService;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tentativas e sessões de estudo (TASK 3.7) — escrita do fato de resposta,
 * com ocultação do Modo Prova (TASK 5.3).
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Correção 100% no servidor a partir do gabarito da questão ({@code
 *       answer_key}): o cliente nunca informa {@code isCorrect}/{@code
 *       wasAnnulled}/{@code answeredAt}. Anulada ({@code answerKey X}) sai
 *       com {@code wasAnnulled=true, isCorrect=NULL} — pontuação
 *       DESCONHECIDA (TASK 1.3 §4), fora do aproveitamento.</li>
 *   <li>{@code BLANK} (em branco) conta como erro quando a questão não é
 *       anulada — nunca como acerto, nunca como anulada.</li>
 *   <li>Vínculo obrigatório: {@code studySessionId} ou {@code
 *       simulationAttemptId} (ERD §5 item 6 — resposta órfã proibida). Cada
 *       vínculo é validado por posse (dono do token), status ({@code
 *       IN_PROGRESS}) e coerência de modo com a tentativa.</li>
 *   <li>Modo Prova (TASK 5.3, AGENTS.md §9): durante a execução ({@code
 *       IN_PROGRESS}) o {@code POST} devolve {@code isCorrect=NULL} com nota
 *       de gabarito oculto — o valor correto permanece persistido no fato
 *       imutável e reaparece após encerrar (via {@code GET} da tentativa ou
 *       via resultado do simulado). {@code wasAnnulled} segue verídico
 *       (anulada não tem resposta correta a vazar). Fora de prova
 *       ({@code ESTUDO}/{@code REVISAO}, ou {@code PROVA} já encerrada) o
 *       resultado é devolvido normalmente.</li>
 *   <li>Tentativas são imutáveis (trigger): sem PUT/DELETE — correção só via
 *       nova tentativa.</li>
 *   <li>A cada registro, o agregado {@code student_topic_performance} é
 *       reconstruído pela TASK 4.1 ({@link PerformanceService}) na mesma
 *       transação — overview ao vivo e materializado nunca divergem.</li>
 * </ul>
 */
@Service
public class AttemptService {

  private static final Set<String> MODES = Set.of("ESTUDO", "PROVA", "REVISAO");
  private static final Set<String> OPTIONS = Set.of("A", "B", "C", "D", "BLANK");

  private static final Logger log = LoggerFactory.getLogger(AttemptService.class);

  private final UserRepository users;
  private final QuestionRepository questions;
  private final QuestionAttemptRepository attempts;
  private final StudySessionRepository sessions;
  private final SimulationAttemptRefRepository simulations;
  private final PerformanceService performance;
  private final br.com.voupassar.admin.service.TechMetrics metrics;

  public AttemptService(
      UserRepository users,
      QuestionRepository questions,
      QuestionAttemptRepository attempts,
      StudySessionRepository sessions,
      SimulationAttemptRefRepository simulations,
      PerformanceService performance,
      br.com.voupassar.admin.service.TechMetrics metrics) {
    this.users = users;
    this.questions = questions;
    this.attempts = attempts;
    this.sessions = sessions;
    this.simulations = simulations;
    this.performance = performance;
    this.metrics = metrics;
  }

  /** Abre uma sessão de estudo ({@code IN_PROGRESS}). */
  @Transactional
  public StudySessionResponse createSession(long userId, CreateStudySessionRequest req) {
    User user = requireActiveUser(userId);
    String mode = normalizeMode(req.mode());

    StudySession s = new StudySession();
    s.setUser(user);
    s.setMode(mode);
    s.setStatus("IN_PROGRESS");
    s.setStartedAt(OffsetDateTime.now());
    sessions.save(s);

    log.info("sessao aberta user_id={} session_id={} mode={}", userId, s.getId(), mode);
    return toSession(s);
  }

  /** Consulta uma sessão do dono do token. */
  @Transactional(readOnly = true)
  public StudySessionResponse getSession(long userId, long sessionId) {
    requireActiveUser(userId);
    return toSession(requireOwnedSession(userId, sessionId));
  }

  /** Encerra uma sessão ({@code IN_PROGRESS → FINISHED}). */
  @Transactional
  public StudySessionResponse finishSession(long userId, long sessionId) {
    requireActiveUser(userId);
    StudySession s = requireOwnedSession(userId, sessionId);
    if (!"IN_PROGRESS".equals(s.getStatus())) {
      throw new ConflictException("SESSION_CLOSED", "Sessão já encerrada (status " + s.getStatus() + ").");
    }
    s.setStatus("FINISHED");
    s.setFinishedAt(OffsetDateTime.now());
    sessions.save(s);

    log.info("sessao encerrada user_id={} session_id={}", userId, sessionId);
    return toSession(s);
  }

  /** Registra uma tentativa com correção do servidor (fato imutável). */
  @Transactional
  public AttemptResponse submitAttempt(long userId, CreateAttemptRequest req) {
    User user = requireActiveUser(userId);
    Question question = questions.findById(req.questionId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "QUESTION_NOT_FOUND", "Questão não encontrada."));

    String selected = normalizeOption(req.selectedOption());
    String mode = normalizeMode(req.mode());
    if (req.studySessionId() == null && req.simulationAttemptId() == null) {
      throw new BadRequestException(
          "ATTEMPT_LINK_REQUIRED",
          "Informe studySessionId ou simulationAttemptId (resposta órfã é proibida).");
    }

    StudySession session = null;
    if (req.studySessionId() != null) {
      session = requireOwnedSession(userId, req.studySessionId());
      if (!"IN_PROGRESS".equals(session.getStatus())) {
        throw new ConflictException(
            "SESSION_CLOSED", "Sessão já encerrada (status " + session.getStatus() + ").");
      }
      requireSameMode("sessão", session.getMode(), mode);
    }

    SimulationAttemptRef sim = null;
    if (req.simulationAttemptId() != null) {
      sim = simulations.findByIdAndUserId(req.simulationAttemptId(), userId)
          .orElseThrow(() -> new ResourceNotFoundException(
              "SIMULATION_ATTEMPT_NOT_FOUND", "Execução de simulado não encontrada."));
      if (!"IN_PROGRESS".equals(sim.getStatus())) {
        throw new ConflictException(
            "SIMULATION_CLOSED", "Simulado já encerrado (status " + sim.getStatus() + ").");
      }
      requireSameMode("simulado", sim.getMode(), mode);
    }

    boolean annulled = question.isAnnulled() || "X".equals(question.getAnswerKey());
    Boolean correct = annulled ? null : selected.equals(question.getAnswerKey());

    QuestionAttempt a = new QuestionAttempt();
    a.setUser(user);
    a.setQuestion(question);
    a.setStudySessionId(session == null ? null : session.getId());
    a.setSimulationAttemptId(sim == null ? null : sim.getId());
    a.setSelectedOption(selected);
    a.setCorrect(correct);
    a.setAnnulled(annulled);
    a.setTimeSpentSeconds(req.timeSpentSeconds());
    a.setMode(mode);
    a.setAnsweredAt(OffsetDateTime.now());
    attempts.save(a);
    performance.rebuildTopicPerformance(userId);

    // TASK 5.3: o POST só é aceito com vínculo IN_PROGRESS (validado acima);
    // em PROVA a resposta é registrada mas o resultado fica oculto até encerrar.
    boolean hidden = "PROVA".equals(mode);
    log.info("tentativa user_id={} attempt_id={} question_id={} mode={} annulled={} hidden={}",
        userId, a.getId(), question.getId(), mode, annulled, hidden);
    metrics.attemptsSubmitted();
    return toAttempt(a, hidden);
  }

  /**
   * Consulta uma tentativa do dono do token (TASK 5.3: em PROVA com execução
   * ainda {@code IN_PROGRESS} o resultado segue oculto — sem atalho para
   * furar a prova via GET; após encerrar, revela normalmente).
   */
  @Transactional(readOnly = true)
  public AttemptResponse getAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    QuestionAttempt a = attempts.findByIdAndUserId(attemptId, userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "ATTEMPT_NOT_FOUND", "Tentativa não encontrada."));
    return toAttempt(a, isHiddenDuringProva(userId, a));
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

  private StudySession requireOwnedSession(long userId, long sessionId) {
    return sessions.findByIdAndUserId(sessionId, userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "SESSION_NOT_FOUND", "Sessão não encontrada."));
  }

  private static void requireSameMode(String owner, String expected, String actual) {
    if (!expected.equals(actual)) {
      throw new BadRequestException(
          "MODE_MISMATCH",
          "Modo da tentativa (" + actual + ") diverge do modo da " + owner + " (" + expected + ").");
    }
  }

  private static String normalizeMode(String mode) {
    String m = mode == null ? "" : mode.trim().toUpperCase();
    if (!MODES.contains(m)) {
      throw new BadRequestException("INVALID_MODE", "Modo deve ser ESTUDO, PROVA ou REVISAO.");
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

  private static StudySessionResponse toSession(StudySession s) {
    return new StudySessionResponse(
        s.getId(), s.getMode(), s.getStatus(), s.getStartedAt(), s.getFinishedAt());
  }

  /**
   * Revela o resultado salvo, salvo quando {@code hidden}: em prova em
   * andamento {@code isCorrect} sai NULL (oculto) — o valor persistido no
   * fato imutável é preservado e reaparece após encerrar.
   */
  private static AttemptResponse toAttempt(QuestionAttempt a, boolean hidden) {
    List<String> notes = new ArrayList<>(4);
    if (a.isAnnulled()) {
      notes.add("Questão anulada no gabarito oficial: conta como conteúdo respondido e fica fora do aproveitamento; regra de pontuação DESCONHECIDA.");
    }
    if ("BLANK".equals(a.getSelectedOption()) && !a.isAnnulled()) {
      notes.add("Resposta em branco: conta como erro (questão pontuável não anulada).");
    }
    if ("PROVA".equals(a.getMode())) {
      if (hidden) {
        notes.add("Modo Prova: resultado oculto durante a execução (preserva a sensação de prova — AGENTS.md §9); conclua ou abandone a execução/sessão para ver a correção.");
      } else {
        notes.add("Modo Prova: execução encerrada — correção revelada (confronto contra o gabarito vigente na tentativa; no simulado vale o gabarito congelado).");
      }
    }
    return new AttemptResponse(
        a.getId(),
        a.getQuestion().getId(),
        a.getSelectedOption(),
        hidden ? null : a.getCorrect(),
        a.isAnnulled(),
        a.getMode(),
        a.getTimeSpentSeconds(),
        a.getStudySessionId(),
        a.getSimulationAttemptId(),
        a.getAnsweredAt(),
        List.copyOf(notes));
  }

  /**
   * TASK 5.3: esconder quando a tentativa é {@code PROVA} e ao menos um
   * vínculo ainda está aberto ({@code IN_PROGRESS}). Vínculo encerrado ou
   * ausente = prova já terminou = revelar.
   */
  private boolean isHiddenDuringProva(long userId, QuestionAttempt a) {
    if (!"PROVA".equals(a.getMode())) {
      return false;
    }
    if (a.getSimulationAttemptId() != null) {
      var sim = simulations.findByIdAndUserId(a.getSimulationAttemptId(), userId);
      if (sim.isPresent() && "IN_PROGRESS".equals(sim.get().getStatus())) {
        return true;
      }
    }
    if (a.getStudySessionId() != null) {
      var session = sessions.findByIdAndUserId(a.getStudySessionId(), userId);
      if (session.isPresent() && "IN_PROGRESS".equals(session.get().getStatus())) {
        return true;
      }
    }
    // Sem vínculo aberto (ambos encerrados, ou tentativa legada sem vínculo
    // rastreável): nada em execução para proteger — revelar.
    // Se houver ao menos um vínculo e todos os rastreáveis estiverem
    // encerrados, também revelar; se nenhum vínculo, revelar (nunca inventar
    // ocultação sem evidência de prova em andamento).
    if (a.getSimulationAttemptId() == null && a.getStudySessionId() == null) {
      return false;
    }
    // Há vínculo(s) mas nenhum IN_PROGRESS encontrado: verificar se ao menos
    // um vínculo existe e está encerrado (revelar) vs. vínculo sumiu (revelar).
    return false;
  }
}
