package br.com.voupassar.performance.service;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.performance.dto.PerformanceEvolutionResponse;
import br.com.voupassar.performance.dto.PerformanceEvolutionResponse.BucketResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse.DisciplinePerformanceResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse.SubtopicPerformanceResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse.TopicPerformanceResponse;
import br.com.voupassar.performance.dto.RebuildResponse;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.entity.StudentTopicPerformance;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cálculo de desempenho (TASK 4.1) — determinístico, transparente e auditável.
 *
 * <p>Única fonte: o fato imutável {@code question_attempts} (+ a classificação
 * vigente não-rejeitada mais recente por questão para tópico/subassunto).
 * Nenhum número é inventado: sem tentativas pontuáveis, {@code accuracy} sai
 * NULL com nota explícita.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Anuladas ({@code wasAnnulled=true}, {@code isCorrect} NULL) contam
 *       como conteúdo respondido e ficam fora do aproveitamento — regra de
 *       pontuação DESCONHECIDA (TASK 1.3 §4).</li>
 *   <li>Por disciplina usa {@code questions.discipline_id} (factual); por
 *       assunto/subassunto usa a vigente não-rejeitada (derivada, revisão
 *       humana PENDENTE — TASK 12.2). Sem vigente, a tentativa conta no
 *       geral/disciplina e em {@code unclassifiedAttempts}.</li>
 *   <li>O agregado {@code student_topic_performance} guarda só pontuáveis
 *       ({@code attempts=scored}, {@code hits=correct}) para que a coluna
 *       gerada {@code accuracy=hits/attempts} coincida com o aproveitamento
 *       ao vivo. Anuladas por conteúdo seguem visíveis no overview (total vs
 *       scored), mas não no materializado — limitação documentada.</li>
 *   <li>Evolução em baldes de calendário UTC (DAY/WEEK segunda-feira/MONTH
 *       dia 1), só baldes com tentativas, sem interpolação.</li>
 * </ul>
 */
@Service
public class PerformanceService {

  /** Granularidades aceitas na evolução (normalizadas para maiúsculas). */
  public static final Set<String> GRANULARITIES = Set.of("DAY", "WEEK", "MONTH");

  private static final Logger log = LoggerFactory.getLogger(PerformanceService.class);

  private final UserRepository users;
  private final QuestionAttemptRepository attempts;
  private final QuestionClassificationRepository classifications;
  private final StudentTopicPerformanceRepository performance;

  public PerformanceService(
      UserRepository users,
      QuestionAttemptRepository attempts,
      QuestionClassificationRepository classifications,
      StudentTopicPerformanceRepository performance) {
    this.users = users;
    this.attempts = attempts;
    this.classifications = classifications;
    this.performance = performance;
  }

  /** Retrato do desempenho: geral + por disciplina/assunto/subassunto. */
  @Transactional(readOnly = true)
  public PerformanceOverviewResponse getOverview(long userId) {
    requireActiveUser(userId);
    List<QuestionAttempt> all = attempts.findAllByUserIdWithQuestion(userId);
    Map<Long, QuestionClassification> vigente = vigenteByQuestion(all);

    long total = all.size();
    long scored = 0;
    long correct = 0;
    long annulled = 0;
    long unclassified = 0;
    OffsetDateTime last = null;

    Map<String, Acc> byDiscipline = new HashMap<>();
    Map<Long, TopicAcc> byTopic = new HashMap<>();
    Map<Long, SubtopicAcc> bySubtopic = new HashMap<>();

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
      if (a.getAnsweredAt() != null && (last == null || a.getAnsweredAt().isAfter(last))) {
        last = a.getAnsweredAt();
      }

      String dCode = a.getQuestion().getDiscipline().getCode();
      String dName = a.getQuestion().getDiscipline().getName();
      byDiscipline
          .computeIfAbsent(dCode, k -> new Acc(dCode, dName))
          .add(isScored, isCorrect);

      QuestionClassification c = vigente.get(a.getQuestion().getId());
      if (c == null || c.getTopic() == null) {
        unclassified++;
        continue;
      }
      Topic t = c.getTopic();
      byTopic
          .computeIfAbsent(t.getId(), k -> new TopicAcc(t))
          .add(isScored, isCorrect);
      Subtopic s = c.getSubtopic();
      if (s != null) {
        bySubtopic
            .computeIfAbsent(s.getId(), k -> new SubtopicAcc(s))
            .add(isScored, isCorrect);
      }
    }
    long incorrect = Math.max(0L, scored - correct);
    Double accuracy = scored == 0 ? null : correct / (double) scored;

    List<DisciplinePerformanceResponse> disciplines = new ArrayList<>(byDiscipline.size());
    for (Acc acc : byDiscipline.values()) {
      disciplines.add(new DisciplinePerformanceResponse(
          acc.code, acc.name, acc.total, acc.scored, acc.correct, acc.accuracy()));
    }
    disciplines.sort((x, y) -> x.disciplineCode().compareTo(y.disciplineCode()));

    List<TopicPerformanceResponse> topics = new ArrayList<>(byTopic.size());
    for (TopicAcc acc : byTopic.values()) {
      topics.add(new TopicPerformanceResponse(
          acc.topic.getId(), acc.topic.getCode(), acc.topic.getName(),
          acc.topic.getDiscipline().getCode(), acc.topic.getDiscipline().getName(),
          acc.total, acc.scored, acc.correct, acc.accuracy()));
    }
    topics.sort((x, y) -> {
      int cmp = nullsLast(x.accuracy(), y.accuracy());
      return cmp != 0 ? cmp : x.topicCode().compareTo(y.topicCode());
    });

    List<SubtopicPerformanceResponse> subtopics = new ArrayList<>(bySubtopic.size());
    for (SubtopicAcc acc : bySubtopic.values()) {
      subtopics.add(new SubtopicPerformanceResponse(
          acc.subtopic.getId(), acc.subtopic.getCode(), acc.subtopic.getName(),
          acc.subtopic.getTopic().getId(), acc.subtopic.getTopic().getCode(),
          acc.subtopic.getTopic().getName(),
          acc.subtopic.getTopic().getDiscipline().getCode(),
          acc.total, acc.scored, acc.correct, acc.accuracy()));
    }
    subtopics.sort((x, y) -> {
      int cmp = nullsLast(x.accuracy(), y.accuracy());
      return cmp != 0 ? cmp : x.subtopicCode().compareTo(y.subtopicCode());
    });

    List<String> notes = new ArrayList<>();
    if (total == 0) {
      notes.add("Nenhuma tentativa registrada: aproveitamento ainda DESCONHECIDO (responda questões ou faça um simulado).");
    }
    if (annulled > 0) {
      notes.add("Anuladas contam como conteúdo respondido e ficam fora do aproveitamento; regra de pontuação DESCONHECIDA.");
    }
    notes.add("Assuntos/subassuntos usam a classificação vigente não-rejeitada (derivada, revisão humana PENDENTE — TASK 12.2), nunca verdade oficial do IFRN.");
    if (unclassified > 0) {
      notes.add(unclassified + " tentativa(s) sem classificação vigente: contam no geral/por disciplina, fora de assunto/subassunto (NECESSITA REVISÃO).");
    }
    notes.add("Diagnóstico, roteiro e recomendações entram nas TASKs 4.2–4.4 — aqui só desempenho calculado.");

    return new PerformanceOverviewResponse(
        total, scored, correct, incorrect, annulled, accuracy, last, unclassified,
        List.copyOf(disciplines), List.copyOf(topics), List.copyOf(subtopics),
        List.copyOf(notes));
  }

  /**
   * Série temporal do aproveitamento.
   *
   * @param granularity {@code DAY}, {@code WEEK} (segunda-feira) ou {@code
   *     MONTH} (dia 1); case-insensitive
   */
  @Transactional(readOnly = true)
  public PerformanceEvolutionResponse getEvolution(long userId, String granularity) {
    requireActiveUser(userId);
    String g = granularity == null ? "WEEK" : granularity.trim().toUpperCase();
    if (!GRANULARITIES.contains(g)) {
      throw new BadRequestException(
          "INVALID_GRANULARITY", "Granularidade deve ser DAY, WEEK ou MONTH.");
    }
    List<QuestionAttempt> all = attempts.findAllByUserIdWithQuestion(userId);

    Map<LocalDate, Acc> buckets = new TreeMap<>();
    for (QuestionAttempt a : all) {
      if (a.getAnsweredAt() == null) {
        continue;
      }
      LocalDate day = a.getAnsweredAt().atZoneSameInstant(ZoneOffset.UTC).toLocalDate();
      LocalDate key = switch (g) {
        case "DAY" -> day;
        case "MONTH" -> day.withDayOfMonth(1);
        default -> day.with(WeekFields.ISO.dayOfWeek(), 1);
      };
      boolean isScored = !a.isAnnulled();
      boolean isCorrect = Boolean.TRUE.equals(a.getCorrect());
      buckets.computeIfAbsent(key, k -> new Acc(k.toString(), k.toString()))
          .add(isScored, isCorrect);
    }

    List<BucketResponse> out = new ArrayList<>(buckets.size());
    for (Map.Entry<LocalDate, Acc> e : buckets.entrySet()) {
      Acc acc = e.getValue();
      out.add(new BucketResponse(
          e.getKey(), acc.total, acc.scored, acc.correct, acc.accuracy()));
    }

    List<String> notes = new ArrayList<>();
    if (all.isEmpty()) {
      notes.add("Nenhuma tentativa registrada: evolução ainda DESCONHECIDA.");
    }
    notes.add("Baldes de calendário UTC (DAY/WEEK segunda-feira/MONTH dia 1); só baldes com tentativas, sem interpolação.");
    notes.add("Anuladas contam como conteúdo e ficam fora do aproveitamento; regra de pontuação DESCONHECIDA.");

    return new PerformanceEvolutionResponse(g, List.copyOf(out), List.copyOf(notes));
  }

  /**
   * Recalcula o agregado {@code student_topic_performance} do aluno a partir
   * do fato imutável (delete + insert, idempotente).
   *
   * @return resumo do rebuild
   */
  @Transactional
  public RebuildResponse rebuildTopicPerformance(long userId) {
    User user = requireActiveUser(userId);
    List<QuestionAttempt> all = attempts.findAllByUserIdWithQuestion(userId);
    Map<Long, QuestionClassification> vigente = vigenteByQuestion(all);

    Map<Long, RebuildAcc> topicLevel = new HashMap<>();
    Map<String, RebuildAcc> subtopicLevel = new HashMap<>();

    for (QuestionAttempt a : all) {
      if (a.isAnnulled()) {
        continue;
      }
      QuestionClassification c = vigente.get(a.getQuestion().getId());
      if (c == null || c.getTopic() == null) {
        continue;
      }
      boolean hit = Boolean.TRUE.equals(a.getCorrect());
      topicLevel
          .computeIfAbsent(c.getTopic().getId(), k -> new RebuildAcc(c.getTopic(), null))
          .add(hit, a.getAnsweredAt());
      if (c.getSubtopic() != null) {
        String key = c.getTopic().getId() + ":" + c.getSubtopic().getId();
        subtopicLevel
            .computeIfAbsent(key, k -> new RebuildAcc(c.getTopic(), c.getSubtopic()))
            .add(hit, a.getAnsweredAt());
      }
    }

    performance.deleteByUserId(userId);
    // Garante a ordem delete → insert no banco (o bulk DELETE acima já
    // executou; o flush descarrega eventual INSERT pendente da tentativa na
    // mesma transação antes dos INSERTs do agregado abaixo).
    performance.flush();

    List<StudentTopicPerformance> rows = new ArrayList<>(topicLevel.size() + subtopicLevel.size());
    for (RebuildAcc acc : topicLevel.values()) {
      rows.add(toRow(user, acc));
    }
    for (RebuildAcc acc : subtopicLevel.values()) {
      rows.add(toRow(user, acc));
    }
    performance.saveAll(rows);

    log.info("desempenho rebuild user_id={} rows={}", userId, rows.size());
    return new RebuildResponse(rows.size(), OffsetDateTime.now());
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
   * Vigente por questão: mais recente não-rejeitada (maior id). A query já
   * ordena {@code (question ASC, id DESC)} — a primeira por questão vence.
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

  private static StudentTopicPerformance toRow(User user, RebuildAcc acc) {
    StudentTopicPerformance p = new StudentTopicPerformance();
    p.setUser(user);
    p.setTopic(acc.topic);
    p.setSubtopic(acc.subtopic);
    p.setAttempts(acc.scored);
    p.setHits(acc.hits);
    p.setLastAttemptAt(acc.last);
    return p;
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

  private static final class Acc {
    final String code;
    final String name;
    long total;
    long scored;
    long correct;

    Acc(String code, String name) {
      this.code = code;
      this.name = name;
    }

    void add(boolean isScored, boolean isCorrect) {
      total++;
      if (isScored) {
        scored++;
        if (isCorrect) {
          correct++;
        }
      }
    }

    Double accuracy() {
      return scored == 0 ? null : correct / (double) scored;
    }
  }

  private static final class TopicAcc {
    final Topic topic;
    long total;
    long scored;
    long correct;

    TopicAcc(Topic topic) {
      this.topic = topic;
    }

    void add(boolean isScored, boolean isCorrect) {
      total++;
      if (isScored) {
        scored++;
        if (isCorrect) {
          correct++;
        }
      }
    }

    Double accuracy() {
      return scored == 0 ? null : correct / (double) scored;
    }
  }

  private static final class SubtopicAcc {
    final Subtopic subtopic;
    long total;
    long scored;
    long correct;

    SubtopicAcc(Subtopic subtopic) {
      this.subtopic = subtopic;
    }

    void add(boolean isScored, boolean isCorrect) {
      total++;
      if (isScored) {
        scored++;
        if (isCorrect) {
          correct++;
        }
      }
    }

    Double accuracy() {
      return scored == 0 ? null : correct / (double) scored;
    }
  }

  private static final class RebuildAcc {
    final Topic topic;
    final Subtopic subtopic;
    int scored;
    int hits;
    OffsetDateTime last;

    RebuildAcc(Topic topic, Subtopic subtopic) {
      this.topic = topic;
      this.subtopic = subtopic;
    }

    void add(boolean hit, OffsetDateTime answeredAt) {
      scored++;
      if (hit) {
        hits++;
      }
      if (answeredAt != null && (last == null || answeredAt.isAfter(last))) {
        last = answeredAt;
      }
    }
  }
}
