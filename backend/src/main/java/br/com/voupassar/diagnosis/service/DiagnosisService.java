package br.com.voupassar.diagnosis.service;

import br.com.voupassar.admin.service.TechMetrics;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.diagnosis.dto.DiagnosisResponse;
import br.com.voupassar.diagnosis.dto.DiagnosisResponse.DisciplineDiagnosisItem;
import br.com.voupassar.diagnosis.dto.DiagnosisResponse.PriorityItem;
import br.com.voupassar.diagnosis.dto.DiagnosisResponse.TopicDiagnosisItem;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Diagnóstico inicial do aluno (TASK 4.2) — determinístico, transparente e
 * auditável.
 *
 * <p>Entrada: as respostas do aluno (fato imutável {@code question_attempts}).
 * Saída: pontos fortes, pontos fracos, lacunas, fila de assuntos prioritários
 * e nível de domínio estimado — cada item com motivo textual gerado dos mesmos
 * fatores que o classificam (desempenho próprio + recorrência histórica).
 *
 * <p>Limiares explícitos (documentados também em {@code docs/api-diagnostico.md}):
 * <ul>
 *   <li>Sinal mínimo por assunto: {@value #MIN_SCORED_FOR_SIGNAL} tentativas
 *       pontuáveis. Abaixo disso não há classificação de domínio: {@code 0} =
 *       {@code NAO_AVALIADO} (lacuna), {@code 1–2} = {@code EM_OBSERVACAO}
 *       (sinal insuficiente).</li>
 *   <li>Com sinal suficiente: {@code accuracy >= 0.7} = {@code DOMINADO}
 *       (ponto forte); {@code accuracy < 0.5} = {@code FRAGIL} (ponto fraco);
 *       entre {@code 0.5} e {@code 0.7} = {@code EM_DESENVOLVIMENTO}.</li>
 *   <li>Nível geral: sem pontuáveis = {@code DESCONHECIDO}; {@code < 0.5} =
 *       {@code INICIAL}; {@code 0.5–0.7} = {@code EM_DESENVOLVIMENTO};
 *       {@code >= 0.7} = {@code CONSOLIDADO}.</li>
 * </ul>
 *
 * <p>Fila de prioridades (só assuntos não-dominados): primeiro os frágeis
 * (accuracy asc), depois intermediários abaixo da média geral, depois lacunas
 * e sinais insuficientes (recorrência histórica desc), por fim intermediários
 * na média ou acima. Desempates sempre por código, para ordem determinística.
 */
@Service
public class DiagnosisService {

  /** Tentativas pontuáveis mínimas por assunto para classificar domínio. */
  public static final int MIN_SCORED_FOR_SIGNAL = 3;

  /** Aproveitamento mínimo para considerar um assunto dominado. */
  public static final double DOMINANCE_THRESHOLD = 0.7;

  /** Abaixo deste aproveitamento (com sinal suficiente) o assunto é frágil. */
  public static final double FRAGILITY_THRESHOLD = 0.5;

  private final UserRepository users;
  private final QuestionAttemptRepository attempts;
  private final QuestionClassificationRepository classifications;
  private final TopicRepository topics;
  private final DisciplineRepository disciplines;
  private final QuestionRepository questions;
  private final TechMetrics metrics;

  public DiagnosisService(
      UserRepository users,
      QuestionAttemptRepository attempts,
      QuestionClassificationRepository classifications,
      TopicRepository topics,
      DisciplineRepository disciplines,
      QuestionRepository questions,
      TechMetrics metrics) {
    this.users = users;
    this.attempts = attempts;
    this.classifications = classifications;
    this.topics = topics;
    this.disciplines = disciplines;
    this.questions = questions;
    this.metrics = metrics;
  }

  /**
   * Monta o diagnóstico da trilha de um processo (TASK E.2).
   *
   * @param institution {@code IFRN} ou {@code EAJ}; {@code null}/em-branco =
   *     panorama global legado (ambos os processos, comportamento pré-E.2).
   *     Clientes novos devem informar sempre: ano sozinho nunca decide, e
   *     frequência global mistura 2022/2025 entre processos.
   */
  @Transactional(readOnly = true)
  public DiagnosisResponse getDiagnosis(long userId, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    if (instNorm == null) {
      return getDiagnosis(userId);
    }
    return getDiagnosisForInstitution(userId, instNorm);
  }

  /**
   * Normaliza o filtro {@code ?institution=} (TASK E.2, mesmo vocabulário da
   * E.1): {@code null}/em-branco = sem filtro (global legado); senão
   * {@code IFRN} ou {@code EAJ}, senão 400.
   */
  static String normalizeInstitutionFilter(String institution) {
    if (institution == null || institution.isBlank()) {
      return null;
    }
    String normalized = institution.trim().toUpperCase();
    if (!"IFRN".equals(normalized) && !"EAJ".equals(normalized)) {
      throw new BadRequestException(
          "Processo seletivo inválido: " + institution + " (permitido IFRN, EAJ).");
    }
    return normalized;
  }

  /**
   * Diagnóstico escopado por processo (TASK E.2): só tentativas de questões
   * daquela {@code institution} (autorais sem edição contam em ambas as
   * trilhas — não-oficiais, sem frequência histórica) + frequência histórica
   * daquele processo (D.2-EAJ na trilha EAJ, sem contaminar o IFRN).
   */
  private DiagnosisResponse getDiagnosisForInstitution(long userId, String institution) {
    requireActiveUser(userId);
    List<QuestionAttempt> all = attempts.findAllByUserIdWithQuestionAndExam(userId);
    List<QuestionAttempt> scoped = new ArrayList<>(all.size());
    for (QuestionAttempt a : all) {
      String examInstitution = a.getQuestion() != null && a.getQuestion().getExam() != null
          ? a.getQuestion().getExam().getInstitution()
          : null;
      if (examInstitution == null || institution.equals(examInstitution)) {
        scoped.add(a);
      }
    }
    all = scoped;
    Map<Long, QuestionClassification> vigente = vigenteByQuestion(all);

    long total = all.size();
    long scored = 0;
    long correct = 0;
    long annulled = 0;
    long unclassified = 0;
    OffsetDateTime last = null;

    Map<Long, TopicAcc> byTopic = new HashMap<>();
    Map<String, DiscAcc> byDiscipline = new HashMap<>();

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

      DiscAcc disc = byDiscipline.computeIfAbsent(
          a.getQuestion().getDiscipline().getCode(),
          k -> new DiscAcc(a.getQuestion().getDiscipline()));
      disc.add(isScored, isCorrect);

      QuestionClassification c = vigente.get(a.getQuestion().getId());
      if (c == null || c.getTopic() == null) {
        unclassified++;
        continue;
      }
      byTopic.computeIfAbsent(c.getTopic().getId(), k -> new TopicAcc(c.getTopic()))
          .add(isScored, isCorrect);
    }

    long incorrect = Math.max(0L, scored - correct);
    Double overall = scored == 0 ? null : correct / (double) scored;
    String overallLevel = overallLevel(overall);

    // ---- histórico do processo (derivado, revisão pendente) ----
    List<Topic> taxonomy = topics.findAllOrdered();
    Map<Long, Long> histByTopic = new HashMap<>();
    for (Object[] row : classifications.countByTopicForInstitution(institution)) {
      histByTopic.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
    long totalClassified = classifications.countClassifiedForInstitution(institution);
    Map<Long, Integer> editionsByTopic = new HashMap<>(taxonomy.size());
    for (Topic t : taxonomy) {
      editionsByTopic.put(
          t.getId(), classifications.editionsByTopicForInstitution(t.getId(), institution).size());
    }

    List<TopicDiagnosisItem> allTopics = new ArrayList<>(taxonomy.size());
    for (Topic t : taxonomy) {
      TopicAcc acc = byTopic.get(t.getId());
      long tAttempts = acc == null ? 0L : acc.total;
      long tScored = acc == null ? 0L : acc.scored;
      long tCorrect = acc == null ? 0L : acc.correct;
      Double tAccuracy = tScored == 0 ? null : tCorrect / (double) tScored;
      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      double percent = totalClassified == 0 ? 0.0
          : Math.round(hist * 1000.0 / totalClassified) / 10.0;
      int editions = editionsByTopic.getOrDefault(t.getId(), 0);
      String mastery = masteryLevel(tScored, tAccuracy);
      allTopics.add(new TopicDiagnosisItem(
          t.getId(), t.getCode(), t.getName(),
          t.getDiscipline().getCode(), t.getDiscipline().getName(),
          tAttempts, tScored, tCorrect, tAccuracy,
          hist, percent, editions, mastery,
          topicReason(t.getCode(), tScored, tCorrect, tAccuracy, overall,
              hist, percent, editions, mastery)));
    }

    List<TopicDiagnosisItem> strengths = filter(allTopics, "DOMINADO");
    strengths.sort(Comparator.comparing(TopicDiagnosisItem::accuracy,
        Comparator.nullsLast(Double::compareTo)).reversed()
        .thenComparing(TopicDiagnosisItem::topicCode));
    List<TopicDiagnosisItem> weaknesses = filter(allTopics, "FRAGIL");
    weaknesses.sort(Comparator.comparing(TopicDiagnosisItem::accuracy,
        Comparator.nullsLast(Double::compareTo))
        .thenComparing(TopicDiagnosisItem::historicalQuestions, Comparator.reverseOrder())
        .thenComparing(TopicDiagnosisItem::topicCode));
    List<TopicDiagnosisItem> gaps = filter(allTopics, "NAO_AVALIADO");
    gaps.sort(Comparator.comparing(TopicDiagnosisItem::historicalQuestions).reversed()
        .thenComparing(TopicDiagnosisItem::topicCode));
    List<TopicDiagnosisItem> lowSignal = filter(allTopics, "EM_OBSERVACAO");
    lowSignal.sort(Comparator.comparing(TopicDiagnosisItem::historicalQuestions).reversed()
        .thenComparing(TopicDiagnosisItem::topicCode));

    List<TopicDiagnosisItem> byTopicSorted = new ArrayList<>(allTopics);
    byTopicSorted.sort(Comparator.comparing(TopicDiagnosisItem::accuracy,
        Comparator.nullsLast(Double::compareTo))
        .thenComparing(TopicDiagnosisItem::topicCode));

    List<PriorityItem> priorities = prioritize(allTopics, overall);

    List<DisciplineDiagnosisItem> byDisciplineList = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      DiscAcc discAcc = byDiscipline.get(d.getCode());
      long dAttempts = discAcc == null ? 0L : discAcc.total;
      long dScored = discAcc == null ? 0L : discAcc.scored;
      long dCorrect = discAcc == null ? 0L : discAcc.correct;
      Double dAccuracy = dScored == 0 ? null : dCorrect / (double) dScored;
      long hist = questions.countByInstitutionAndDisciplineCode(institution, d.getCode());
      String mastery = masteryLevel(dScored, dAccuracy);
      byDisciplineList.add(new DisciplineDiagnosisItem(
          d.getCode(), d.getName(), dAttempts, dScored, dCorrect, dAccuracy,
          hist, mastery, disciplineReason(dScored, dCorrect, dAccuracy, overall, hist, mastery)));
    }
    byDisciplineList.sort(Comparator.comparing(DisciplineDiagnosisItem::disciplineCode));

    List<String> notes = new ArrayList<>();
    if (total == 0) {
      notes.add("Nenhuma tentativa registrada na trilha " + institution
          + ": diagnóstico ainda DESCONHECIDO (responda questões ou faça um simulado deste processo).");
    } else if (scored > 0 && scored < 5) {
      notes.add("Sinal inicial na trilha " + institution + ": apenas " + scored
          + " tentativa(s) pontuável(is) — o diagnóstico estabiliza com mais respostas.");
    }
    if (annulled > 0) {
      notes.add("Anuladas contam como conteúdo respondido e ficam fora do aproveitamento; regra de pontuação DESCONHECIDA.");
    }
    notes.add("Trilha " + institution + ": só tentativas de questões " + institution
        + " (autorias sem edição contam em ambas — sem frequência histórica);"
        + " assuntos usam a classificação vigente não-rejeitada (derivada, revisão humana PENDENTE — TASK 12.2),"
        + " nunca verdade oficial do processo.");
    if (unclassified > 0) {
      notes.add(unclassified
          + " tentativa(s) sem classificação vigente: contam no geral/por disciplina, fora de assunto (NECESSITA REVISÃO).");
    }
    notes.add("Limiares: DOMINADO ≥ 70%, FRÁGIL < 50% (intermediário 50–70%), sinal mínimo "
        + MIN_SCORED_FOR_SIGNAL + " pontuáveis por assunto; lacuna = 0 pontuáveis.");
    notes.add("Dificuldade estimada NÃO usada no diagnóstico (palpite global BAIXA, sem calibração por desempenho).");
    if ("EAJ".equals(institution)) {
      notes.add("Frequências históricas da trilha EAJ sobre 3 edições (2021, 2022, 2025; 130 classificações D.1/D.2 — sem interpolar edições inexistentes).");
    } else {
      notes.add("Frequências históricas da trilha IFRN sobre 6 edições (2020, 2022–2026; 2021 ausente no dataset, sem interpolação).");
    }
    notes.add("Diagnóstico é estimativa inicial e explicável; o roteiro e a recomendação final entram nas TASKs 4.3–4.4 (trilha " + institution + ").");

    metrics.diagnosesGenerated();
    return new DiagnosisResponse(
        total, scored, correct, incorrect, annulled, overall, overallLevel, last,
        unclassified, List.copyOf(strengths), List.copyOf(weaknesses),
        List.copyOf(gaps), List.copyOf(lowSignal), List.copyOf(priorities),
        List.copyOf(byTopicSorted), List.copyOf(byDisciplineList), List.copyOf(notes));
  }

  /** Monta o diagnóstico inicial do dono do token. */
  @Transactional(readOnly = true)
  public DiagnosisResponse getDiagnosis(long userId) {
    requireActiveUser(userId);
    List<QuestionAttempt> all = attempts.findAllByUserIdWithQuestion(userId);
    Map<Long, QuestionClassification> vigente = vigenteByQuestion(all);

    long total = all.size();
    long scored = 0;
    long correct = 0;
    long annulled = 0;
    long unclassified = 0;
    OffsetDateTime last = null;

    Map<Long, TopicAcc> byTopic = new HashMap<>();
    Map<String, DiscAcc> byDiscipline = new HashMap<>();

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

      DiscAcc disc = byDiscipline.computeIfAbsent(
          a.getQuestion().getDiscipline().getCode(),
          k -> new DiscAcc(a.getQuestion().getDiscipline()));
      disc.add(isScored, isCorrect);

      QuestionClassification c = vigente.get(a.getQuestion().getId());
      if (c == null || c.getTopic() == null) {
        unclassified++;
        continue;
      }
      byTopic.computeIfAbsent(c.getTopic().getId(), k -> new TopicAcc(c.getTopic()))
          .add(isScored, isCorrect);
    }

    long incorrect = Math.max(0L, scored - correct);
    Double overall = scored == 0 ? null : correct / (double) scored;
    String overallLevel = overallLevel(overall);

    // ---- histórico (derivado, revisão pendente) ----
    List<Topic> taxonomy = topics.findAllOrdered();
    Map<Long, Long> histByTopic = new HashMap<>();
    for (Object[] row : classifications.countByTopic()) {
      histByTopic.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
    long totalClassified = classifications.countClassified();
    Map<Long, Integer> editionsByTopic = new HashMap<>(taxonomy.size());
    for (Topic t : taxonomy) {
      editionsByTopic.put(t.getId(), classifications.editionsByTopic(t.getId()).size());
    }

    List<TopicDiagnosisItem> allTopics = new ArrayList<>(taxonomy.size());
    for (Topic t : taxonomy) {
      TopicAcc acc = byTopic.get(t.getId());
      long tAttempts = acc == null ? 0L : acc.total;
      long tScored = acc == null ? 0L : acc.scored;
      long tCorrect = acc == null ? 0L : acc.correct;
      Double tAccuracy = tScored == 0 ? null : tCorrect / (double) tScored;
      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      double percent = totalClassified == 0 ? 0.0
          : Math.round(hist * 1000.0 / totalClassified) / 10.0;
      int editions = editionsByTopic.getOrDefault(t.getId(), 0);
      String mastery = masteryLevel(tScored, tAccuracy);
      allTopics.add(new TopicDiagnosisItem(
          t.getId(), t.getCode(), t.getName(),
          t.getDiscipline().getCode(), t.getDiscipline().getName(),
          tAttempts, tScored, tCorrect, tAccuracy,
          hist, percent, editions, mastery,
          topicReason(t.getCode(), tScored, tCorrect, tAccuracy, overall,
              hist, percent, editions, mastery)));
    }

    List<TopicDiagnosisItem> strengths = filter(allTopics, "DOMINADO");
    strengths.sort(Comparator.comparing(TopicDiagnosisItem::accuracy,
        Comparator.nullsLast(Double::compareTo)).reversed()
        .thenComparing(TopicDiagnosisItem::topicCode));
    List<TopicDiagnosisItem> weaknesses = filter(allTopics, "FRAGIL");
    weaknesses.sort(Comparator.comparing(TopicDiagnosisItem::accuracy,
        Comparator.nullsLast(Double::compareTo))
        .thenComparing(TopicDiagnosisItem::historicalQuestions, Comparator.reverseOrder())
        .thenComparing(TopicDiagnosisItem::topicCode));
    List<TopicDiagnosisItem> gaps = filter(allTopics, "NAO_AVALIADO");
    gaps.sort(Comparator.comparing(TopicDiagnosisItem::historicalQuestions).reversed()
        .thenComparing(TopicDiagnosisItem::topicCode));
    List<TopicDiagnosisItem> lowSignal = filter(allTopics, "EM_OBSERVACAO");
    lowSignal.sort(Comparator.comparing(TopicDiagnosisItem::historicalQuestions).reversed()
        .thenComparing(TopicDiagnosisItem::topicCode));

    List<TopicDiagnosisItem> byTopicSorted = new ArrayList<>(allTopics);
    byTopicSorted.sort(Comparator.comparing(TopicDiagnosisItem::accuracy,
        Comparator.nullsLast(Double::compareTo))
        .thenComparing(TopicDiagnosisItem::topicCode));

    List<PriorityItem> priorities = prioritize(allTopics, overall);

    List<DisciplineDiagnosisItem> byDisciplineList = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      DiscAcc discAcc = byDiscipline.get(d.getCode());
      long dAttempts = discAcc == null ? 0L : discAcc.total;
      long dScored = discAcc == null ? 0L : discAcc.scored;
      long dCorrect = discAcc == null ? 0L : discAcc.correct;
      Double dAccuracy = dScored == 0 ? null : dCorrect / (double) dScored;
      long hist = questions.countByDisciplineCode(d.getCode());
      String mastery = masteryLevel(dScored, dAccuracy);
      byDisciplineList.add(new DisciplineDiagnosisItem(
          d.getCode(), d.getName(), dAttempts, dScored, dCorrect, dAccuracy,
          hist, mastery, disciplineReason(dScored, dCorrect, dAccuracy, overall, hist, mastery)));
    }
    byDisciplineList.sort(Comparator.comparing(DisciplineDiagnosisItem::disciplineCode));

    List<String> notes = new ArrayList<>();
    if (total == 0) {
      notes.add("Nenhuma tentativa registrada: diagnóstico ainda DESCONHECIDO (responda questões ou faça um simulado).");
    } else if (scored > 0 && scored < 5) {
      notes.add("Sinal inicial: apenas " + scored
          + " tentativa(s) pontuável(is) — o diagnóstico estabiliza com mais respostas.");
    }
    if (annulled > 0) {
      notes.add("Anuladas contam como conteúdo respondido e ficam fora do aproveitamento; regra de pontuação DESCONHECIDA.");
    }
    notes.add("Assuntos usam a classificação vigente não-rejeitada (derivada, revisão humana PENDENTE — TASK 12.2), nunca verdade oficial do IFRN.");
    if (unclassified > 0) {
      notes.add(unclassified
          + " tentativa(s) sem classificação vigente: contam no geral/por disciplina, fora de assunto (NECESSITA REVISÃO).");
    }
    notes.add("Limiares: DOMINADO ≥ 70%, FRÁGIL < 50% (intermediário 50–70%), sinal mínimo "
        + MIN_SCORED_FOR_SIGNAL + " pontuáveis por assunto; lacuna = 0 pontuáveis.");
    notes.add("Dificuldade estimada NÃO usada no diagnóstico (palpite global BAIXA, sem calibração por desempenho).");
    notes.add("Frequências históricas sobre 6 edições (2020, 2022–2026; 2021 ausente no dataset, sem interpolação).");
    notes.add("Diagnóstico é estimativa inicial e explicável; o roteiro e a recomendação final entram nas TASKs 4.3–4.4.");

    metrics.diagnosesGenerated();
    return new DiagnosisResponse(
        total, scored, correct, incorrect, annulled, overall, overallLevel, last,
        unclassified, List.copyOf(strengths), List.copyOf(weaknesses),
        List.copyOf(gaps), List.copyOf(lowSignal), List.copyOf(priorities),
        List.copyOf(byTopicSorted), List.copyOf(byDisciplineList), List.copyOf(notes));
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
   * ordena {@code (question ASC, id DESC)} — a primeira por questão vence
   * (mesmo critério da TASK 4.1).
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

  private static String overallLevel(Double overall) {
    if (overall == null) {
      return "DESCONHECIDO";
    }
    if (overall < FRAGILITY_THRESHOLD) {
      return "INICIAL";
    }
    if (overall < DOMINANCE_THRESHOLD) {
      return "EM_DESENVOLVIMENTO";
    }
    return "CONSOLIDADO";
  }

  private static List<TopicDiagnosisItem> filter(List<TopicDiagnosisItem> all, String mastery) {
    List<TopicDiagnosisItem> out = new ArrayList<>();
    for (TopicDiagnosisItem t : all) {
      if (t.masteryLevel().equals(mastery)) {
        out.add(t);
      }
    }
    return out;
  }

  /**
   * Fila de atenção: só assuntos não-dominados, em ordem determinística.
   * Pesos: 0 frágil, 1 intermediário abaixo da média geral, 2 lacuna,
   * 3 sinal insuficiente, 4 intermediário na média ou acima (sem média geral,
   * intermediários caem no peso 1).
   */
  private static List<PriorityItem> prioritize(List<TopicDiagnosisItem> all, Double overall) {
    List<TopicDiagnosisItem> pending = new ArrayList<>();
    for (TopicDiagnosisItem t : all) {
      if (!t.masteryLevel().equals("DOMINADO")) {
        pending.add(t);
      }
    }
    pending.sort((a, b) -> {
      int wa = weight(a, overall);
      int wb = weight(b, overall);
      if (wa != wb) {
        return Integer.compare(wa, wb);
      }
      if (wa <= 1) {
        int cmp = nullsLast(a.accuracy(), b.accuracy());
        if (cmp != 0) {
          return cmp;
        }
        if (a.historicalQuestions() != b.historicalQuestions()) {
          return Long.compare(b.historicalQuestions(), a.historicalQuestions());
        }
        return a.topicCode().compareTo(b.topicCode());
      }
      if (a.historicalQuestions() != b.historicalQuestions()) {
        return Long.compare(b.historicalQuestions(), a.historicalQuestions());
      }
      return a.topicCode().compareTo(b.topicCode());
    });
    List<PriorityItem> out = new ArrayList<>(pending.size());
    for (int i = 0; i < pending.size(); i++) {
      TopicDiagnosisItem t = pending.get(i);
      out.add(new PriorityItem(
          i + 1, t.topicId(), t.topicCode(), t.topicName(),
          t.disciplineCode(), t.disciplineName(),
          t.accuracy(), t.scored(), t.historicalQuestions(), t.editionsCount(),
          t.reason()));
    }
    return out;
  }

  private static int weight(TopicDiagnosisItem t, Double overall) {
    return switch (t.masteryLevel()) {
      case "FRAGIL" -> 0;
      case "EM_DESENVOLVIMENTO" -> {
        if (overall == null) {
          yield 1;
        }
        yield t.accuracy() != null && t.accuracy() < overall ? 1 : 4;
      }
      case "NAO_AVALIADO" -> 2;
      case "EM_OBSERVACAO" -> 3;
      default -> 5;
    };
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

  private static String pct(Double accuracy) {
    if (accuracy == null) {
      return "—";
    }
    return Math.round(accuracy * 100) + "%";
  }

  private static String topicReason(
      String topicCode, long scored, long correct, Double accuracy, Double overall,
      long hist, double percent, int editions, String mastery) {
    String base = switch (mastery) {
      case "NAO_AVALIADO" -> "Nenhuma tentativa pontuável";
      case "EM_OBSERVACAO" -> "Apenas " + scored + " tentativa(s) pontuável(is)"
          + " (" + correct + "/" + scored + ", " + pct(accuracy) + ") — sinal insuficiente (mínimo "
          + MIN_SCORED_FOR_SIGNAL + ")";
      default -> correct + "/" + scored + " (" + pct(accuracy) + ") em " + scored
          + " tentativas pontuáveis" + overallCmp(accuracy, overall);
    };
    return base + "; tema " + topicCode + " com " + hist + " questões em " + editions
        + " edições (" + percent + "% do banco)" + masteryHint(mastery) + ".";
  }

  private static String overallCmp(Double accuracy, Double overall) {
    if (accuracy == null || overall == null) {
      return " (sem média geral; ainda DESCONHECIDA)";
    }
    if (accuracy < overall) {
      return ", abaixo da sua média geral " + pct(overall);
    }
    if (accuracy > overall) {
      return ", acima da sua média geral " + pct(overall);
    }
    return ", igual à sua média geral " + pct(overall);
  }

  private static String masteryHint(String mastery) {
    return switch (mastery) {
      case "DOMINADO" -> " — ponto forte";
      case "FRAGIL" -> " — atenção prioritária";
      case "EM_DESENVOLVIMENTO" -> " — faixa intermediária";
      case "EM_OBSERVACAO" -> " — lacuna parcial";
      default -> " — lacuna não avaliada";
    };
  }

  private static String disciplineReason(
      long scored, long correct, Double accuracy, Double overall, long hist, String mastery) {
    if (scored == 0 || accuracy == null) {
      return "Nenhuma tentativa pontuável; disciplina com " + hist
          + " questões no banco — ainda não avaliada.";
    }
    if (scored < MIN_SCORED_FOR_SIGNAL) {
      return "Apenas " + scored + " tentativa(s) pontuável(is) (" + correct + "/" + scored
          + ", " + pct(accuracy) + ") — sinal insuficiente (mínimo " + MIN_SCORED_FOR_SIGNAL
          + "); disciplina com " + hist + " questões no banco.";
    }
    return correct + "/" + scored + " (" + pct(accuracy) + ")" + overallCmp(accuracy, overall)
        + "; disciplina com " + hist + " questões no banco" + masteryHint(mastery) + ".";
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
  }

  private static final class DiscAcc {
    final Discipline discipline;
    long total;
    long scored;
    long correct;

    DiscAcc(Discipline discipline) {
      this.discipline = discipline;
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
  }
}
