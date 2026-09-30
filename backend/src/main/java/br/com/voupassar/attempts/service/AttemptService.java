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
 * Tentativas e sessões de estudo (TASK 3.7) — escrita do fato de resposta.
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
 *   <li>O resultado é sempre devolvido (decisão da TASK 3.7); ocultar o
 *       gabarito no Modo Prova é responsabilidade da Fase 5.</li>
 *   <li>Tentativas são imutáveis (trigger): sem PUT/DELETE — correção só via
 *       nova tentativa.</li>
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

  public AttemptService(
      UserRepository users,
      QuestionRepository questions,
      QuestionAttemptRepository attempts,
      StudySessionRepository sessions,
      SimulationAttemptRefRepository simulations) {
    this.users = users;
    this.questions = questions;
    this.attempts = attempts;
    this.sessions = sessions;
    this.simulations = simulations;
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

    log.info("tentativa user_id={} attempt_id={} question_id={} mode={} annulled={}",
        userId, a.getId(), question.getId(), mode, annulled);
    return toAttempt(a);
  }

  /** Consulta uma tentativa do dono do token. */
  @Transactional(readOnly = true)
  public AttemptResponse getAttempt(long userId, long attemptId) {
    requireActiveUser(userId);
    QuestionAttempt a = attempts.findByIdAndUserId(attemptId, userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "ATTEMPT_NOT_FOUND", "Tentativa não encontrada."));
    return toAttempt(a);
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

  private static AttemptResponse toAttempt(QuestionAttempt a) {
    List<String> notes = new ArrayList<>(3);
    if (a.isAnnulled()) {
      notes.add("Questão anulada no gabarito oficial: conta como conteúdo respondido e fica fora do aproveitamento; regra de pontuação DESCONHECIDA.");
    }
    if ("BLANK".equals(a.getSelectedOption()) && !a.isAnnulled()) {
      notes.add("Resposta em branco: conta como erro (questão pontuável não anulada).");
    }
    if ("PROVA".equals(a.getMode())) {
      notes.add("Modo Prova: o resultado já está corrigido no servidor; ocultar o gabarito durante a execução é responsabilidade da camada de simulados (Fase 5).");
    }
    return new AttemptResponse(
        a.getId(),
        a.getQuestion().getId(),
        a.getSelectedOption(),
        a.getCorrect(),
        a.isAnnulled(),
        a.getMode(),
        a.getTimeSpentSeconds(),
        a.getStudySessionId(),
        a.getSimulationAttemptId(),
        a.getAnsweredAt(),
        List.copyOf(notes));
  }
}
