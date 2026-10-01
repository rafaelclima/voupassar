package br.com.voupassar.review.service;

import br.com.voupassar.attempts.entity.StudySession;
import br.com.voupassar.attempts.repository.StudySessionRepository;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.review.dto.CreateReviewSessionRequest;
import br.com.voupassar.review.dto.ReviewQueueResponse;
import br.com.voupassar.review.dto.ReviewQueueResponse.ReviewItem;
import br.com.voupassar.review.dto.ReviewSessionResponse;
import br.com.voupassar.review.dto.ReviewSessionResponse.ReviewSessionItem;
import br.com.voupassar.review.dto.ReviewSessionResultResponse;
import br.com.voupassar.review.dto.ReviewSessionResultResponse.ResultItem;
import br.com.voupassar.review.entity.ReviewSessionQuestion;
import br.com.voupassar.review.repository.ReviewSessionQuestionRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Modo Revisão — experiência dedicada (TASK 5.4).
 *
 * <p>Cria, retoma e encerra sessões de revisão sobre o caderno congelado em
 * {@code review_session_questions} (DDL V5). O caderno congela o top-N da
 * fila da TASK 4.5 (ordem determinística) no momento da criação; o progresso
 * e o resultado derivam das tentativas com {@code mode=REVISAO} vinculadas à
 * sessão (fato imutável, TASK 3.7).
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Revisão não gera simulado (ERD §2.6): aqui nasce
 *       {@code study_sessions} com {@code mode=REVISAO}, nunca
 *       {@code simulations}. Sem {@code frozen_answer_key} (diferença
 *       intencional para a TASK 5.1): a revisão responde via
 *       {@code POST /attempts}, cuja correção usa o gabarito vigente.</li>
 *   <li>Feedback imediato: {@code isCorrect} da última tentativa
 *       <b>nesta sessão</b> é sempre revelado — REVISAO nunca oculta
 *       resultado (a ocultação da TASK 5.3 vale só para PROVA).</li>
 *   <li>Fila vazia (com filtros válidos) → {@code 400 NO_REVIEW_ITEMS}
 *       com orientação (nunca sessão vazia inventada). Filtros inválidos
 *       herdam 404 da fila (disciplina/assunto inexistentes).</li>
 *   <li>Enunciados nunca duplicados: resolve-se via {@code GET
 *       /api/v1/questions/{id}} (TASK 3.4).</li>
 * </ul>
 */
@Service
public class ReviewSessionService {

  private static final Logger log = LoggerFactory.getLogger(ReviewSessionService.class);

  private final UserRepository users;
  private final StudySessionRepository sessions;
  private final ReviewSessionQuestionRepository caderno;
  private final QuestionAttemptRepository responses;
  private final QuestionRepository questions;
  private final ReviewService review;

  public ReviewSessionService(
      UserRepository users,
      StudySessionRepository sessions,
      ReviewSessionQuestionRepository caderno,
      QuestionAttemptRepository responses,
      QuestionRepository questions,
      ReviewService review) {
    this.users = users;
    this.sessions = sessions;
    this.caderno = caderno;
    this.responses = responses;
    this.questions = questions;
    this.review = review;
  }

  /**
   * Cria e inicia uma sessão de revisão (caderno congelado, {@code
   * IN_PROGRESS}).
   */
  @Transactional
  public ReviewSessionResponse create(long userId, CreateReviewSessionRequest req) {
    requireActiveUser(userId);
    boolean onlyErrors = req.onlyErrors() != null && req.onlyErrors();
    ReviewQueueResponse queue = review.getQueue(
        userId, req.limit(), req.discipline(), req.topicId(), onlyErrors);
    if (queue.items().isEmpty()) {
      throw new BadRequestException(
          "NO_REVIEW_ITEMS",
          "Nenhuma questão na fila de revisão para estes filtros"
              + describeFilters(req.discipline(), req.topicId(), onlyErrors)
              + ": responda questões no modo estudo ou em simulados para alimentar a revisão "
              + "(questões nunca tentadas pertencem ao diagnóstico TASK 4.2 e ao roteiro TASK 4.4).");
    }

    User user = users.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));
    StudySession session = new StudySession();
    session.setUser(user);
    session.setMode("REVISAO");
    session.setStatus("IN_PROGRESS");
    session.setStartedAt(OffsetDateTime.now());
    sessions.save(session);

    List<Long> ids = queue.items().stream().map(ReviewItem::questionId).toList();
    Map<Long, Question> byId = questionsById(ids);
    List<ReviewSessionQuestion> rows = new ArrayList<>(ids.size());
    for (int i = 0; i < ids.size(); i++) {
      Question q = requirePresent(byId, ids.get(i));
      ReviewSessionQuestion row = new ReviewSessionQuestion();
      row.setStudySessionId(session.getId());
      row.setPosition((short) (i + 1));
      row.setQuestion(q);
      rows.add(row);
    }
    caderno.saveAll(rows);

    Map<Long, ReviewItem> meta = metaByQuestion(queue);
    log.info("sessao de revisao criada user_id={} session_id={} items={}",
        userId, session.getId(), rows.size());
    return toResponse(session, rows, byId, Map.of(), meta, true);
  }

  /** Retoma uma sessão de revisão do dono do token (progresso + feedback imediato). */
  @Transactional(readOnly = true)
  public ReviewSessionResponse get(long userId, long sessionId) {
    requireActiveUser(userId);
    StudySession session = requireOwnedSession(userId, sessionId);
    List<ReviewSessionQuestion> rows = requireCaderno(session);
    Map<Long, QuestionAttempt> latest = latestByQuestion(
        responses.findByStudySessionIdAndUserId(sessionId, userId));
    Map<Long, Question> byId = questionsById(questionIds(rows));
    return toResponse(session, rows, byId, latest, liveMeta(userId), false);
  }

  /** Visualiza o resultado (só após encerrar a sessão). */
  @Transactional(readOnly = true)
  public ReviewSessionResultResponse getResult(long userId, long sessionId) {
    requireActiveUser(userId);
    StudySession session = requireOwnedSession(userId, sessionId);
    List<ReviewSessionQuestion> rows = requireCaderno(session);
    if ("IN_PROGRESS".equals(session.getStatus())) {
      throw new ConflictException(
          "REVIEW_NOT_FINISHED",
          "Sessão de revisão ainda em andamento: responda via POST /attempts "
              + "(mode=REVISAO, studySessionId=" + sessionId + ") e encerre via "
              + "POST /api/v1/study-sessions/" + sessionId + "/finish para ver o resultado "
              + "(feedback imediato por questão já está disponível no GET desta sessão).");
    }
    Map<Long, QuestionAttempt> latest = latestByQuestion(
        responses.findByStudySessionIdAndUserId(sessionId, userId));
    Map<Long, Question> byId = questionsById(questionIds(rows));
    return toResult(session, rows, byId, latest);
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

  /**
   * A sessão só é "de revisão" quando tem caderno congelado. Sessões
   * ESTUDO/PROVA ou REVISAO genéricas (sem caderno) respondem 404 neste
   * contexto — nunca com caderno inventado.
   */
  private List<ReviewSessionQuestion> requireCaderno(StudySession session) {
    List<ReviewSessionQuestion> rows =
        caderno.findByStudySessionIdOrderByPositionAsc(session.getId());
    if (!"REVISAO".equals(session.getMode()) || rows.isEmpty()) {
      throw new ResourceNotFoundException(
          "REVIEW_SESSION_NOT_FOUND",
          "Sessão de revisão não encontrada: crie uma via POST /api/v1/review/sessions "
              + "(a fila vive em GET /api/v1/review/queue).");
    }
    return rows;
  }

  private Map<Long, Question> questionsById(List<Long> ids) {
    Map<Long, Question> out = new HashMap<>(ids.size() * 2);
    for (Question q : questions.findAllById(ids)) {
      out.put(q.getId(), q);
    }
    return out;
  }

  private static Question requirePresent(Map<Long, Question> byId, Long id) {
    Question q = byId.get(id);
    if (q == null) {
      throw new IllegalStateException("Questão da fila " + id + " não encontrada.");
    }
    return q;
  }

  private static List<Long> questionIds(List<ReviewSessionQuestion> rows) {
    List<Long> ids = new ArrayList<>(rows.size());
    for (ReviewSessionQuestion r : rows) {
      ids.add(r.getQuestion().getId());
    }
    return ids;
  }

  private static Map<Long, ReviewItem> metaByQuestion(ReviewQueueResponse queue) {
    Map<Long, ReviewItem> out = new HashMap<>(queue.items().size() * 2);
    for (ReviewItem item : queue.items()) {
      out.put(item.questionId(), item);
    }
    return out;
  }

  /**
   * Fila vigente para enriquecer o GET/resultado com categoria e motivo. Usa
   * o top-100 sem filtros; itens fora dele mantêm posição e progresso com
   * motivo indisponível (nunca motivo inventado).
   */
  private Map<Long, ReviewItem> liveMeta(long userId) {
    try {
      return metaByQuestion(review.getQueue(userId, ReviewService.MAX_LIMIT, null, null, false));
    } catch (RuntimeException e) {
      // A leitura do caderno nunca quebra por causa do enriquecimento:
      // sem fila vigente, itens saem sem categoria/motivo (ver toResponse).
      log.warn("fila vigente indisponível para sessão de revisão user_id={}: {}", userId, e.getMessage());
      return Map.of();
    }
  }

  /**
   * Última tentativa por questão <b>nesta sessão</b> (maior
   * {@code answeredAt}, desempate por maior {@code id}) — o fato é imutável,
   * correção só via nova tentativa.
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

  private static String describeFilters(String discipline, Long topicId, boolean onlyErrors) {
    StringBuilder sb = new StringBuilder(" (");
    sb.append("disciplina=").append(discipline == null ? "todas" : discipline);
    sb.append(", assunto=").append(topicId == null ? "todos" : topicId);
    sb.append(", onlyErrors=").append(onlyErrors);
    sb.append(")");
    return sb.toString();
  }

  private ReviewSessionResponse toResponse(
      StudySession session,
      List<ReviewSessionQuestion> rows,
      Map<Long, Question> byId,
      Map<Long, QuestionAttempt> latest,
      Map<Long, ReviewItem> meta,
      boolean created) {
    List<ReviewSessionItem> items = new ArrayList<>(rows.size());
    long answered = 0;
    boolean metaMissing = false;
    for (ReviewSessionQuestion row : rows) {
      Question q = requirePresent(byId, row.getQuestion().getId());
      QuestionAttempt last = latest.get(q.getId());
      ReviewItem m = meta.get(q.getId());
      if (m == null) {
        metaMissing = true;
      }
      boolean wasAnswered = last != null;
      if (wasAnswered) {
        answered++;
      }
      boolean wasAnnulled = last != null ? last.isAnnulled() : q.isAnnulled();
      items.add(new ReviewSessionItem(
          row.getPosition().intValue(),
          q.getId(),
          q.getDiscipline().getCode(),
          q.getDiscipline().getName(),
          q.getSourceYear() == null ? null : q.getSourceYear().intValue(),
          q.getSourceQuestionNumber() == null ? null : q.getSourceQuestionNumber().intValue(),
          m == null ? null : m.topicCode(),
          m == null ? null : m.topicName(),
          m == null ? null : m.reviewCategory(),
          m == null ? null : m.reason(),
          wasAnswered,
          last == null ? null : last.getSelectedOption(),
          // REVISAO nunca oculta: feedback imediato (diferença da TASK 5.3, só PROVA).
          last == null || wasAnnulled ? null : last.getCorrect(),
          wasAnnulled));
    }
    List<String> notes = responseNotes(session, created, metaMissing);
    return new ReviewSessionResponse(
        session.getId(), session.getMode(), session.getStatus(),
        session.getStartedAt(), session.getFinishedAt(),
        rows.size(), answered, rows.size() - answered,
        List.copyOf(items), List.copyOf(notes));
  }

  private ReviewSessionResultResponse toResult(
      StudySession session,
      List<ReviewSessionQuestion> rows,
      Map<Long, Question> byId,
      Map<Long, QuestionAttempt> latest) {
    List<ResultItem> items = new ArrayList<>(rows.size());
    long answered = 0;
    long scored = 0;
    long correct = 0;
    long annulled = 0;
    for (ReviewSessionQuestion row : rows) {
      Question q = requirePresent(byId, row.getQuestion().getId());
      QuestionAttempt last = latest.get(q.getId());
      boolean unanswered = last == null;
      boolean wasAnnulled;
      Boolean isCorrect = null;
      if (last != null) {
        answered++;
        wasAnnulled = last.isAnnulled();
        if (wasAnnulled) {
          annulled++;
        } else {
          scored++;
          isCorrect = last.getCorrect();
          if (Boolean.TRUE.equals(isCorrect)) {
            correct++;
          }
        }
      } else {
        // Sem resposta: anulada hoje conta no resumo como anulada (nunca como
        // erro inventado); pontuável sem resposta conta como pendente.
        wasAnnulled = q.isAnnulled() || "X".equals(q.getAnswerKey());
        if (wasAnnulled) {
          annulled++;
        }
      }
      items.add(new ResultItem(
          row.getPosition().intValue(),
          q.getId(),
          q.getDiscipline().getCode(),
          q.getSourceYear() == null ? null : q.getSourceYear().intValue(),
          q.getSourceQuestionNumber() == null ? null : q.getSourceQuestionNumber().intValue(),
          last == null ? null : last.getSelectedOption(),
          isCorrect,
          wasAnnulled,
          unanswered));
    }
    long total = rows.size();
    long unanswered = total - answered;
    long incorrect = Math.max(0L, scored - correct);
    Double accuracy = scored == 0 ? null : correct / (double) scored;
    List<String> notes = resultNotes(session, unanswered, annulled, scored);
    return new ReviewSessionResultResponse(
        session.getId(), session.getMode(), session.getStatus(),
        total, answered, unanswered, scored, correct, incorrect, annulled, accuracy,
        List.copyOf(items), List.copyOf(notes));
  }

  private static List<String> responseNotes(
      StudySession session, boolean created, boolean metaMissing) {
    List<String> notes = new ArrayList<>();
    if (created) {
      notes.add("Caderno congelado do top-N da fila de revisão (ordem determinística da TASK 4.5) "
          + "no momento da criação.");
    } else if ("IN_PROGRESS".equals(session.getStatus())) {
      notes.add("Sessão em andamento: responda via POST /attempts "
          + "(mode=REVISAO, studySessionId=" + session.getId() + "); "
          + "o resultado de cada resposta é imediato (REVISAO nunca oculta).");
    } else {
      notes.add("Sessão encerrada (" + session.getStatus() + "): o placar completo está em "
          + "GET /api/v1/review/sessions/" + session.getId() + "/result.");
    }
    if (metaMissing) {
      notes.add("Questão fora do top-100 vigente: mantém posição e progresso sem categoria/motivo "
          + "(nunca motivo inventado; a fila completa vive em GET /api/v1/review/queue).");
    }
    notes.add("Correção contra o gabarito vigente via fato persistido (a revisão não congela "
        + "gabarito — diferença intencional para o simulado da TASK 5.1).");
    notes.add("Anuladas ficam fora do aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4).");
    notes.add("Enunciados em GET /api/v1/questions/{id} (este caderno referencia, nunca duplica).");
    return notes;
  }

  private static List<String> resultNotes(
      StudySession session, long unanswered, long annulled, long scored) {
    List<String> notes = new ArrayList<>();
    notes.add("Sessão " + session.getStatus() + ": vale a última tentativa REVISAO por questão "
        + "vinculada a esta sessão.");
    if (unanswered > 0) {
      notes.add(unanswered + " questão(ões) sem resposta: contam como não respondidas, "
          + "nunca como erro inventado.");
    }
    if (annulled > 0) {
      notes.add("Anuladas (" + annulled + ") ficam fora do aproveitamento; "
          + "regra de pontuação DESCONHECIDA (TASK 1.3 §4).");
    }
    if (scored == 0) {
      notes.add("Sem tentativas pontuáveis: aproveitamento NULL (nunca zero inventado).");
    }
    notes.add("Modo REVISAO: o diagnóstico (TASK 4.2) e a fila (TASK 4.5) "
        + "já incorporam estas respostas via question_attempts.");
    return notes;
  }
}
