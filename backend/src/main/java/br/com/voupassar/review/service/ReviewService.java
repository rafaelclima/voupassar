package br.com.voupassar.review.service;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.review.dto.ReviewQueueResponse;
import br.com.voupassar.review.dto.ReviewQueueResponse.ReviewItem;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Modo revisão do aluno (TASK 4.5) — fila determinística, transparente e
 * auditável sobre o fato imutável {@code question_attempts}.
 *
 * <p>Prioriza, nesta ordem (pesos 0–5, menor = mais urgente):
 * <ol>
 *   <li>{@code ERRO_SEM_ACERTO} (0) — questão pontuável tentada e nunca
 *       acertada (erro persistente / questão ainda não dominada);</li>
 *   <li>{@code ERRO_RECENTE} (1) — última pontuável incorreta, com acerto
 *       anterior (regressão);</li>
 *   <li>{@code TOPICO_FRAGIL} (2) — última correta, mas o assunto segue
 *       FRÁGIL (aproveitamento &lt; 50% com sinal suficiente);</li>
 *   <li>{@code REFORCO} (3) — última correta, assunto em desenvolvimento,
 *       em observação ou sem classificação vigente;</li>
 *   <li>{@code MANUTENCAO} (4) — última correta, assunto dominado, mas
 *       exposição única (risco de baixa retenção);</li>
 *   <li>{@code CONSOLIDADO} (5) — última correta, assunto dominado, com
 *       mais de uma exposição.</li>
 * </ol>
 *
 * <p>Desempates sempre determinísticos (sem aleatoriedade): dentro do mesmo
 * balde, erros ordenam por persistência e recência; acertos ordenam pelo
 * aproveitamento do assunto (pior primeiro) e pela antiguidade da última
 * tentativa (mais antiga primeiro — risco de esquecimento); o id da questão
 * fecha o desempate.
 *
 * <p>Anuladas ficam fora da fila (pontuação DESCONHECIDA, TASK 1.3 §4).
 * Questões nunca tentadas ficam fora da fila (pertencem ao diagnóstico,
 * TASK 4.2, e ao roteiro, TASK 4.4). Dificuldade estimada NÃO é usada
 * (palpite global BAIXA, sem calibração).
 */
@Service
public class ReviewService {

  /** Teto de itens por chamada: protege banco e VPS modesta. */
  public static final int MAX_LIMIT = 100;

  /** Limite padrão quando o cliente não informa. */
  public static final int DEFAULT_LIMIT = 20;

  /**
   * Limiares de domínio por assunto — os mesmos da TASK 4.2
   * ({@code DiagnosisService}: sinal mínimo 3 pontuáveis; DOMINADO ≥ 70%;
   * FRÁGIL &lt; 50%). Duplicados aqui para não acoplar os serviços: qualquer
   * mudança de limiar precisa atualizar os dois lugares e a documentação.
   */
  public static final int MIN_SCORED_FOR_SIGNAL = 3;
  public static final double DOMINANCE_THRESHOLD = 0.7;
  public static final double FRAGILITY_THRESHOLD = 0.5;

  private final UserRepository users;
  private final QuestionAttemptRepository attempts;
  private final QuestionClassificationRepository classifications;
  private final TopicRepository topics;
  private final DisciplineRepository disciplines;

  public ReviewService(
      UserRepository users,
      QuestionAttemptRepository attempts,
      QuestionClassificationRepository classifications,
      TopicRepository topics,
      DisciplineRepository disciplines) {
    this.users = users;
    this.attempts = attempts;
    this.classifications = classifications;
    this.topics = topics;
    this.disciplines = disciplines;
  }

  /**
   * Monta a fila de revisão do dono do token.
   *
   * @param userId dono do token (nunca de outro usuário)
   * @param limit máximo de itens {@code [1, 100]} (NULL = padrão 20)
   * @param disciplineCode filtro opcional (ex. {@code MATEMATICA})
   * @param topicId filtro opcional por assunto
   * @param onlyErrors quando {@code true}, só baldes 0–1 (erros)
   */
  @Transactional(readOnly = true)
  public ReviewQueueResponse getQueue(
      long userId, Integer limit, String disciplineCode, Long topicId, boolean onlyErrors) {
    requireActiveUser(userId);
    int lim = requireLimit(limit);
    String discNorm = requireDisciplineFilter(disciplineCode);
    Long topicNorm = requireTopicFilter(topicId);

    List<QuestionAttempt> all = attempts.findAllByUserIdWithQuestion(userId);
    Map<Long, QuestionClassification> vigente = vigenteByQuestion(all);

    long total = all.size();
    long scored = 0;
    long correct = 0;
    long annulled = 0;
    long unclassified = 0;

    // Agregado por assunto (só pontuáveis com classificação vigente).
    Map<Long, TopicAcc> byTopic = new HashMap<>();
    // Agregado por questão (só pontuáveis entram na fila).
    Map<Long, QuestionAcc> byQuestion = new LinkedHashMap<>();

    for (QuestionAttempt a : all) {
      boolean isAnnulled = a.isAnnulled();
      boolean isScored = !isAnnulled;
      boolean isCorrect = Boolean.TRUE.equals(a.getCorrect());
      if (isScored) {
        scored++;
        if (isCorrect) {
          correct++;
        }
      } else {
        annulled++;
      }

      Question q = a.getQuestion();
      QuestionClassification c = vigente.get(q.getId());
      if (c == null || c.getTopic() == null) {
        unclassified++;
      } else if (isScored) {
        byTopic.computeIfAbsent(c.getTopic().getId(), k -> new TopicAcc())
            .add(isCorrect);
      }

      if (isScored) {
        QuestionAcc acc = byQuestion.computeIfAbsent(q.getId(), k -> new QuestionAcc(q, c));
        acc.add(isCorrect, a.getAnsweredAt());
      }
    }
    long incorrect = Math.max(0L, scored - correct);
    Double overall = scored == 0 ? null : correct / (double) scored;

    List<ScoredItem> scoredItems = new ArrayList<>(byQuestion.size());
    for (QuestionAcc acc : byQuestion.values()) {
      Question q = acc.question;
      if (discNorm != null && !discNorm.equals(q.getDiscipline().getCode())) {
        continue;
      }
      QuestionClassification c = acc.classification;
      Long cTopicId = (c != null && c.getTopic() != null) ? c.getTopic().getId() : null;
      if (topicNorm != null && !topicNorm.equals(cTopicId)) {
        continue;
      }

      String mastery;
      Long topicScored = null;
      Double topicAccuracy = null;
      if (c != null && c.getTopic() != null) {
        TopicAcc t = byTopic.get(c.getTopic().getId());
        long ts = t == null ? 0L : t.scored;
        long tc = t == null ? 0L : t.correct;
        topicScored = ts;
        topicAccuracy = ts == 0 ? null : tc / (double) ts;
        mastery = masteryLevel(ts, topicAccuracy);
      } else {
        mastery = "NAO_CLASSIFICADO";
      }

      String category = categorize(acc.correct == 0, !acc.lastCorrect, mastery, acc.attempts);
      if (onlyErrors && weight(category) > 1) {
        continue;
      }
      scoredItems.add(new ScoredItem(acc, mastery, topicScored, topicAccuracy, category));
    }

    scoredItems.sort(reviewOrder());

    List<ReviewItem> items = new ArrayList<>(Math.min(lim, scoredItems.size()));
    for (int i = 0; i < scoredItems.size() && items.size() < lim; i++) {
      ScoredItem s = scoredItems.get(i);
      Question q = s.acc.question;
      QuestionClassification c = s.acc.classification;
      Long days = daysSince(s.acc.lastAttemptAt);
      items.add(new ReviewItem(
          items.size() + 1,
          q.getId(),
          q.getDiscipline().getCode(),
          q.getDiscipline().getName(),
          q.getSourceYear() == null ? null : q.getSourceYear().intValue(),
          q.getSourceQuestionNumber() == null ? null : q.getSourceQuestionNumber().intValue(),
          c != null && c.getTopic() != null ? c.getTopic().getId() : null,
          c != null && c.getTopic() != null ? c.getTopic().getCode() : null,
          c != null && c.getTopic() != null ? c.getTopic().getName() : null,
          c != null && c.getSubtopic() != null ? c.getSubtopic().getId() : null,
          c != null && c.getSubtopic() != null ? c.getSubtopic().getCode() : null,
          c != null && c.getSubtopic() != null ? c.getSubtopic().getName() : null,
          c == null ? null : c.getStatus(),
          c == null ? null : c.getConfidence(),
          c == null ? null : c.getTaxonomyVersion(),
          s.acc.attempts,
          s.acc.correct,
          s.acc.attempts - s.acc.correct,
          s.acc.lastCorrect,
          s.acc.lastAttemptAt,
          days,
          s.mastery,
          s.topicScored,
          s.topicAccuracy,
          s.category,
          itemReason(s, overall, days)));
    }

    List<String> notes = new ArrayList<>();
    if (total == 0) {
      notes.add("Nenhuma tentativa registrada: a fila de revisão começa após as primeiras respostas (modo estudo ou simulado).");
    }
    if (annulled > 0) {
      notes.add("Anuladas (" + annulled + ") ficam fora da fila; contam no resumo, sem pontuar (regra de pontuação DESCONHECIDA, TASK 1.3 §4).");
    }
    if (unclassified > 0) {
      notes.add(unclassified + " tentativa(s) sem classificação vigente: entram na fila sem assunto (NECESSITA REVISÃO, TASK 12.2).");
    }
    notes.add("Ordem determinística: erros persistentes e regressões primeiro; depois acertos em assuntos frágeis; por fim manutenção do consolidado.");
    notes.add("Questões nunca tentadas ficam fora da revisão (ver diagnóstico TASK 4.2 e roteiro TASK 4.4).");
    notes.add("Assuntos usam a classificação vigente não-rejeitada (derivada, revisão humana PENDENTE — TASK 12.2), nunca verdade oficial do IFRN.");
    notes.add("Dificuldade estimada NÃO usada na revisão (palpite global BAIXA, sem calibração por desempenho).");

    return new ReviewQueueResponse(
        total, scored, correct, incorrect, annulled, overall, unclassified,
        byQuestion.size(), items.size(), lim, onlyErrors, discNorm, topicNorm,
        List.copyOf(items), List.copyOf(notes));
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

  static int requireLimit(Integer limit) {
    int lim = (limit == null) ? DEFAULT_LIMIT : limit;
    if (lim < 1 || lim > MAX_LIMIT) {
      throw new BadRequestException("Limite inválido: " + limit + " (permitido 1–" + MAX_LIMIT + ").");
    }
    return lim;
  }

  /**
   * Nível de domínio do assunto — mesmos limiares da TASK 4.2
   * (duplicados para manter os serviços desacoplados).
   */
  static String masteryLevel(long scored, Double accuracy) {
    if (scored == 0 || accuracy == null) {
      return "NAO_AVALIADO";
    }
    if (scored < MIN_SCORED_FOR_SIGNAL) {
      return "EM_OBSERVACAO";
    }
    if (accuracy >= DOMINANCE_THRESHOLD) {
      return "DOMINADO";
    }
    if (accuracy < FRAGILITY_THRESHOLD) {
      return "FRAGIL";
    }
    return "EM_DESENVOLVIMENTO";
  }

  private String requireDisciplineFilter(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    String normalized = code.trim().toUpperCase();
    if (!normalized.matches("[A-Z_]{1,64}")) {
      throw new BadRequestException("Código de disciplina inválido: " + code + ".");
    }
    disciplines.findByCode(normalized)
        .orElseThrow(() -> new ResourceNotFoundException(
            "DISCIPLINE_NOT_FOUND", "Disciplina " + normalized + " não encontrada."));
    return normalized;
  }

  private Long requireTopicFilter(Long topicId) {
    if (topicId == null) {
      return null;
    }
    if (topicId <= 0) {
      throw new BadRequestException("ID de assunto inválido: " + topicId + ".");
    }
    topics.findByIdWithDiscipline(topicId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "TOPIC_NOT_FOUND", "Assunto " + topicId + " não encontrado."));
    return topicId;
  }

  /**
   * Vigente por questão: mais recente não-rejeitada (maior id). A query já
   * ordena {@code (question ASC, id DESC)} — a primeira por questão vence
   * (mesmo critério das TASKs 4.1–4.2).
   */
  private Map<Long, QuestionClassification> vigenteByQuestion(List<QuestionAttempt> all) {
    Set<Long> ids = new HashSet<>();
    for (QuestionAttempt a : all) {
      ids.add(a.getQuestion().getId());
    }
    Map<Long, QuestionClassification> out = new HashMap<>(ids.size());
    if (ids.isEmpty()) {
      return out;
    }
    for (QuestionClassification c : classifications.findActiveByQuestionIds(new ArrayList<>(ids))) {
      out.putIfAbsent(c.getQuestion().getId(), c);
    }
    return out;
  }

  static String categorize(boolean neverCorrect, boolean lastWrong, String mastery, long attempts) {    if (neverCorrect) {
      return "ERRO_SEM_ACERTO";
    }
    if (lastWrong) {
      return "ERRO_RECENTE";
    }
    return switch (mastery) {
      case "FRAGIL" -> "TOPICO_FRAGIL";
      case "EM_DESENVOLVIMENTO", "EM_OBSERVACAO", "NAO_AVALIADO", "NAO_CLASSIFICADO" -> "REFORCO";
      case "DOMINADO" -> (attempts <= 1 ? "MANUTENCAO" : "CONSOLIDADO");
      default -> "REFORCO";
    };
  }

  static int weight(String category) {
    return switch (category) {
      case "ERRO_SEM_ACERTO" -> 0;
      case "ERRO_RECENTE" -> 1;
      case "TOPICO_FRAGIL" -> 2;
      case "REFORCO" -> 3;
      case "MANUTENCAO" -> 4;
      default -> 5;
    };
  }

  /**
   * Ordem determinística da fila: balde primeiro; dentro do balde, erros por
   * persistência e recência, acertos por fragilidade do assunto e antiguidade;
   * id da questão fecha o desempate.
   */
  static Comparator<ScoredItem> reviewOrder() {
    return (a, b) -> {
      int wa = weight(a.category);
      int wb = weight(b.category);
      if (wa != wb) {
        return Integer.compare(wa, wb);
      }
      if (wa <= 1) {
        // Erros: mais tentativas (persistência) primeiro; depois a mais
        // recente; depois menor aproveitamento na questão; depois id.
        if (a.acc.attempts != b.acc.attempts) {
          return Long.compare(b.acc.attempts, a.acc.attempts);
        }
        int cmp = compareLastDesc(a.acc.lastAttemptAt, b.acc.lastAttemptAt);
        if (cmp != 0) {
          return cmp;
        }
        double aa = a.acc.correct / (double) a.acc.attempts;
        double ba = b.acc.correct / (double) b.acc.attempts;
        if (Double.compare(aa, ba) != 0) {
          return Double.compare(aa, ba);
        }
        return Long.compare(a.acc.question.getId(), b.acc.question.getId());
      }
      // Acertos: assunto mais fraco primeiro; depois a mais antiga (risco de
      // esquecimento); depois id.
      int cmp = nullsLast(a.topicAccuracy, b.topicAccuracy);
      if (cmp != 0) {
        return cmp;
      }
      int cmpLast = compareLastAsc(a.acc.lastAttemptAt, b.acc.lastAttemptAt);
      if (cmpLast != 0) {
        return cmpLast;
      }
      return Long.compare(a.acc.question.getId(), b.acc.question.getId());
    };
  }

  private static int compareLastDesc(OffsetDateTime x, OffsetDateTime y) {
    if (x == null && y == null) {
      return 0;
    }
    if (x == null) {
      return 1;
    }
    if (y == null) {
      return -1;
    }
    int cmp = y.compareTo(x);
    return cmp;
  }

  private static int compareLastAsc(OffsetDateTime x, OffsetDateTime y) {
    if (x == null && y == null) {
      return 0;
    }
    if (x == null) {
      return 1;
    }
    if (y == null) {
      return -1;
    }
    return x.compareTo(y);
  }

  private static int nullsLast(Double x, Double y) {
    if (x == null && y == null) {
      return 0;
    }
    if (x == null) {
      return 1;
    }
    if (y == null) {
      return -1;
    }
    return Double.compare(x, y);
  }

  private static Long daysSince(OffsetDateTime last) {
    if (last == null) {
      return null;
    }
    return Math.max(0L, Duration.between(last, OffsetDateTime.now()).toDays());
  }

  private static String pct(Double accuracy) {
    if (accuracy == null) {
      return "—";
    }
    return Math.round(accuracy * 100) + "%";
  }

  private static String itemReason(ScoredItem s, Double overall, Long days) {
    Question q = s.acc.question;
    String where = q.getDiscipline().getCode()
        + (q.getSourceYear() != null ? " " + q.getSourceYear() : "")
        + (q.getSourceQuestionNumber() != null ? " Q" + q.getSourceQuestionNumber() : "");
    String base = s.acc.correct + "/" + s.acc.attempts
        + " (" + pct(s.acc.attempts == 0 ? null : s.acc.correct / (double) s.acc.attempts) + ")"
        + " na questão " + q.getId() + " (" + where + "); última "
        + (s.acc.lastCorrect ? "correta" : "incorreta")
        + (days != null ? " há " + days + " dia(s)" : "");
    String topic = (s.acc.classification != null && s.acc.classification.getTopic() != null)
        ? "; assunto " + s.acc.classification.getTopic().getCode()
            + " (" + s.mastery
            + (s.topicScored != null ? ", " + s.topicScored + " pontuáveis" : "")
            + (s.topicAccuracy != null ? ", " + pct(s.topicAccuracy) : "")
            + (overall != null ? ", sua média geral " + pct(overall) : "") + ")"
        : "; sem classificação vigente (NECESSITA REVISÃO)";
    String hint = switch (s.category) {
      case "ERRO_SEM_ACERTO" -> " — prioridade máxima: você ainda não acertou esta questão.";
      case "ERRO_RECENTE" -> " — regressão: reveja antes que o erro se fixe.";
      case "TOPICO_FRAGIL" -> " — acertou a questão, mas o assunto segue frágil.";
      case "REFORCO" -> " — reforço para consolidar o assunto.";
      case "MANUTENCAO" -> " — manutenção: exposição única, risco de baixa retenção.";
      default -> " — manutenção do consolidado.";
    };
    return base + topic + hint + ".";
  }

  private static final class TopicAcc {
    long scored;
    long correct;

    void add(boolean isCorrect) {
      scored++;
      if (isCorrect) {
        correct++;
      }
    }
  }

  private static final class QuestionAcc {
    final Question question;
    final QuestionClassification classification;
    long attempts;
    long correct;
    boolean lastCorrect;
    OffsetDateTime lastAttemptAt;

    QuestionAcc(Question question, QuestionClassification classification) {
      this.question = question;
      this.classification = classification;
    }

    void add(boolean isCorrect, OffsetDateTime answeredAt) {
      attempts++;
      if (isCorrect) {
        correct++;
      }
      // findAllByUserIdWithQuestion ordena (answeredAt ASC, id ASC): a
      // última processada é a mais recente.
      lastCorrect = isCorrect;
      if (answeredAt != null && (lastAttemptAt == null || !answeredAt.isBefore(lastAttemptAt))) {
        lastAttemptAt = answeredAt;
      }
    }
  }

  static final class ScoredItem {
    final QuestionAcc acc;
    final String mastery;
    final Long topicScored;
    final Double topicAccuracy;
    final String category;

    ScoredItem(QuestionAcc acc, String mastery, Long topicScored, Double topicAccuracy, String category) {
      this.acc = acc;
      this.mastery = mastery;
      this.topicScored = topicScored;
      this.topicAccuracy = topicAccuracy;
      this.category = category;
    }
  }
}
