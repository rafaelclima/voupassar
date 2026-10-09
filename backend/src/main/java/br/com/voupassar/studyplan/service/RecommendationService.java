package br.com.voupassar.studyplan.service;

import br.com.voupassar.admin.service.TechMetrics;
import br.com.voupassar.auth.entity.StudentProfile;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.StudentProfileRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.content.repository.SubtopicRepository;
import br.com.voupassar.profile.entity.StudentTopicPerformance;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.studyplan.dto.StudyPlanItemResponse;
import br.com.voupassar.studyplan.dto.StudyPlanResponse;
import br.com.voupassar.studyplan.entity.StudyPlan;
import br.com.voupassar.studyplan.entity.StudyPlanItem;
import br.com.voupassar.studyplan.repository.StudyPlanItemRepository;
import br.com.voupassar.studyplan.repository.StudyPlanRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Motor de recomendação (TASK 4.3, score v2 na TASK 18.1) — determinístico,
 * transparente e explicável.
 *
 * <p>Fatores (AGENTS.md §8 / architecture.md §7), fórmula e pesos em
 * {@code docs/api-recomendacao.md} (score v2-deterministico):
 * <ul>
 *   <li>Frequência histórica real ({@code countByTopic + editionsByTopic},
 *       mesma fonte do diagnóstico {@code DiagnosisService.java:147-154});</li>
 *   <li>Desempenho do aluno ({@code 1 - accuracy} por tópico);</li>
 *   <li>Dificuldade do assunto (mix de {@code difficulty_estimate} do banco —
 *       palpite global BAIXA, peso documentado);</li>
 *   <li>Recência ({@code StudentTopicPerformance.lastAttemptAt} — quanto mais
 *       parado, maior a prioridade, com teto);</li>
 *   <li>Lacuna ({@code attempts == 0}: piso 0.9 + ordenação por frequência);</li>
 *   <li>Carga ({@code target_year} + {@code study_goal} do perfil: só alarga
 *       ou concentra as faixas de prioridade, nunca filtra assunto).</li>
 * </ul>
 *
 * <p>Cada item traz motivo textual derivado dos mesmos fatores que o
 * classificam, e {@code evidence_json} rastreável (AGENTS.md §8, §11).
 */
@Service
public class RecommendationService {

  static final String ALGORITHM_VERSION = "v2-deterministico";

  /**
   * Roteiro escopado por processo (TASK E.2): mesma fórmula v2 (frequência ×
   * desempenho × recência × dificuldade, lacuna com piso, carga só no
   * banding), mas frequência, desempenho e recência calculados só na trilha
   * ({@code institution}). Desempenho/recência vêm do fato
   * {@code question_attempts} filtrado (não do agregado materializado global);
   * valores idênticos ao materializado quando o rebuild está em dia e só há
   * dados daquele processo. Evidência cita edições/questões do processo
   * (ex. {@code "EAJ 2022 Q12"}).
   */
  static final String ALGORITHM_VERSION_INSTITUTION = "v2.1-institution";

  /** Peso da contagem histórica dentro do fator frequência (resto = amplitude em edições). */
  static final double WEIGHT_HIST_COUNT = 0.7;

  /** Teto do bônus de dificuldade (assunto 100% MEDIA/DIFICIL → 1 + teto). */
  static final double DIFFICULTY_BONUS_CAP = 0.5;

  /** Teto do bônus de recência (parado há 90+ dias → 1 + teto). */
  static final double RECENCY_BONUS_CAP = 0.5;

  /** Dias para atingir o teto de recência. */
  static final double RECENCY_FULL_DAYS = 90.0;

  /** Piso de score de lacuna (0 tentativas); +0.1 × frequência ordena entre lacunas. */
  static final double GAP_FLOOR = 0.9;

  /** Quantas questões oficiais de amostra a evidência carrega por assunto. */
  static final int EVIDENCE_SAMPLE_LIMIT = 5;

  private final UserRepository users;
  private final StudyPlanRepository studyPlans;
  private final StudyPlanItemRepository studyPlanItems;
  private final TopicRepository topics;
  private final SubtopicRepository subtopics;
  private final QuestionAttemptRepository attempts;
  private final StudentTopicPerformanceRepository performanceRepo;
  private final QuestionClassificationRepository classifications;
  private final StudentProfileRepository profiles;
  private final TechMetrics metrics;

  public RecommendationService(
      UserRepository users,
      StudyPlanRepository studyPlans,
      StudyPlanItemRepository studyPlanItems,
      TopicRepository topics,
      SubtopicRepository subtopics,
      QuestionAttemptRepository attempts,
      StudentTopicPerformanceRepository performanceRepo,
      QuestionClassificationRepository classifications,
      StudentProfileRepository profiles,
      TechMetrics metrics) {
    this.users = users;
    this.studyPlans = studyPlans;
    this.studyPlanItems = studyPlanItems;
    this.topics = topics;
    this.subtopics = subtopics;
    this.attempts = attempts;
    this.performanceRepo = performanceRepo;
    this.classifications = classifications;
    this.profiles = profiles;
    this.metrics = metrics;
  }

  /**
   * Gera (ou regenera) o roteiro de estudos do aluno.
   *
   * <p>Fluxo:
   * <ol>
   *   <li>Verifica se já existe um plano ativo para o usuário;</li>
  *   <li>Se existir, desativa o anterior e cria um novo (não apaga histórico);</li>
  *   <li>Conta tentativas pontuáveis: &lt;3 → plano {@code PROVISORIO}
  *       (só frequência histórica, TASK 20.1); ≥3 → {@code PESSOAL};</li>
  *   <li>Calcula prioridades para todos os tópicos via algoritmo determinístico;</li>
  *   <li>Cria itens do roteiro com motivo explicável e evidência rastreável.</li>
   * </ol>
   */
  /**
   * Gera (ou regenera) o roteiro da trilha de um processo (TASK E.2).
   *
   * @param institution {@code IFRN} ou {@code EAJ}; {@code null}/em-branco =
   *     roteiro global legado (ambos os processos, comportamento pré-E.2,
   *     um vigente por usuário). Informado = trilha isolada (um vigente por
   *     {@code (usuário, processo)} — gerar EAJ não desativa o IFRN).
   */
  @Transactional
  public StudyPlanResponse generatePlan(long userId, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    if (instNorm == null) {
      return generatePlan(userId);
    }
    return generatePlanForInstitution(userId, instNorm);
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
   * Geração escopada por processo (TASK E.2): histórico D.2 do processo +
   * desempenho do fato filtrado + evidência citando edições/questões do
   * processo existentes no banco.
   */
  private StudyPlanResponse generatePlanForInstitution(long userId, String institution) {
    User user = users.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));

    studyPlans.findByUserIdAndInstitutionAndIsActiveTrue(userId, institution)
        .ifPresent(plan -> {
          plan.setIsActive(false);
          studyPlans.saveAndFlush(plan);
        });

    long scored = attempts.countScoredByUserIdAndInstitution(userId, institution);
    boolean provisorio = scored < br.com.voupassar.diagnosis.service.DiagnosisService.MIN_SCORED_FOR_SIGNAL;

    StudyPlan newPlan = new StudyPlan(userId, ALGORITHM_VERSION_INSTITUTION, institution);
    newPlan.setStatus(provisorio ? "PROVISORIO" : "PESSOAL");
    StudyPlan savedPlan = studyPlans.save(newPlan);
    if (savedPlan == null) {
      savedPlan = newPlan;
    }

    List<StudyPlanItem> items = buildRecommendationsForInstitution(
        userId, savedPlan, provisorio, institution);
    studyPlanItems.saveAll(items);

    savedPlan.setItems(items);
    metrics.plansGenerated();
    return toResponse(savedPlan, items);
  }

  @Transactional
  public StudyPlanResponse generatePlan(long userId) {
    User user = users.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));

    // Desativa plano anterior (se existir) — preserva histórico.
    // saveAndFlush força o UPDATE antes do INSERT abaixo: sem o flush o
    // Hibernate pode descarregar o INSERT primeiro e violar a UNIQUE parcial
    // uq_study_plans_active (detectado ao vivo na TASK 6.4, segundo POST).
    studyPlans.findByUserIdAndIsActiveTrue(userId)
        .ifPresent(plan -> {
          plan.setIsActive(false);
          studyPlans.saveAndFlush(plan);
        });

    // TASK 20.1: sem ≥3 pontuáveis (mesmo limiar do diagnóstico,
    // DiagnosisService.MIN_SCORED_FOR_SIGNAL), o plano é PROVISORIO —
    // ordenado só por frequência histórica. A regeneração após o
    // diagnóstico marca PESSOAL (a flag viaja no plano, sem nova tabela).
    long scored = attempts.countByUserIdAndAnnulledFalse(userId);
    boolean provisorio = scored < br.com.voupassar.diagnosis.service.DiagnosisService.MIN_SCORED_FOR_SIGNAL;

    StudyPlan newPlan = new StudyPlan(userId, ALGORITHM_VERSION);
    newPlan.setStatus(provisorio ? "PROVISORIO" : "PESSOAL");
    StudyPlan savedPlan = studyPlans.save(newPlan);
    if (savedPlan == null) {
      savedPlan = newPlan;
    }

    // Calcula prioridades determinísticas
    List<StudyPlanItem> items = buildRecommendations(userId, savedPlan, provisorio);
    studyPlanItems.saveAll(items);

    savedPlan.setItems(items);
    metrics.plansGenerated();
    return toResponse(savedPlan, items);
  }

  /**
   * Constrói a lista de recomendações para o usuário (score v2, TASK 18.1).
   */
  private List<StudyPlanItem> buildRecommendations(long userId, StudyPlan plan, boolean provisorio) {
    List<Topic> taxonomy = topics.findAllOrdered();
    List<StudyPlanItem> out = new ArrayList<>();

    // Performance materializada por usuário (rebuild automático na 4.1):
    // nível tópico + nível subassunto (linhas com subtopic não-nulo).
    Map<Long, StudentTopicPerformance> perfByTopic = new HashMap<>();
    Map<Long, List<StudentTopicPerformance>> subPerfByTopic = new HashMap<>();
    for (StudentTopicPerformance p : performanceRepo.findProgressByUserId(userId)) {
      if (p.getTopic() != null) {
        if (p.getSubtopic() == null) {
          perfByTopic.put(p.getTopic().getId(), p);
        } else if (p.getAttempts() > 0) {
          subPerfByTopic.computeIfAbsent(p.getTopic().getId(), k -> new ArrayList<>()).add(p);
        }
      }
    }

    // Frequência histórica real: mesma fonte do diagnóstico
    // (DiagnosisService.java:147-154 — countByTopic + editionsByTopic).
    Map<Long, Long> histByTopic = new HashMap<>();
    for (Object[] row : classifications.countByTopic()) {
      histByTopic.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
    Map<Long, Integer> editionsByTopic = new HashMap<>();
    Map<Long, List<Integer>> editionYearsByTopic = new HashMap<>();
    long maxHist = 0;
    int maxEditions = 0;
    for (Topic t : taxonomy) {
      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      List<Integer> years = classifications.editionsByTopic(t.getId()).stream()
          .map(Short::intValue).sorted().toList();
      editionYearsByTopic.put(t.getId(), years);
      editionsByTopic.put(t.getId(), years.size());
      if (hist > maxHist) {
        maxHist = hist;
      }
      if (years.size() > maxEditions) {
        maxEditions = years.size();
      }
    }

    // Mix de dificuldade por assunto (difficulty_estimate do banco — palpite
    // global BAIXA; tópico sem sinal fica neutro, nunca inventado).
    Map<Long, long[]> difficultyMixByTopic = new HashMap<>();
    for (Object[] row : classifications.difficultyByTopic()) {
      Long topicId = ((Number) row[0]).longValue();
      String difficulty = row[1] == null ? null : row[1].toString();
      long total = ((Number) row[2]).longValue();
      long[] mix = difficultyMixByTopic.computeIfAbsent(topicId, k -> new long[3]);
      if ("FACIL".equals(difficulty)) {
        mix[0] += total;
      } else if ("MEDIA".equals(difficulty)) {
        mix[1] += total;
      } else if ("DIFICIL".equals(difficulty)) {
        mix[2] += total;
      }
    }

    // Carga: urgência (target_year) + porte (study_goal) — só banding, nunca filtro.
    StudentProfile profile = profiles.findById(userId).orElse(null);
    int currentYear = OffsetDateTime.now().getYear();
    double loadMultiplier = loadMultiplier(
        profile == null ? null : profile.getTargetYear(),
        profile == null ? null : profile.getStudyGoal(),
        currentYear);

    // Para cada tópico da taxonomia, calcula score determinístico
    List<TopicScore> scores = new ArrayList<>();
    OffsetDateTime now = OffsetDateTime.now();
    for (Topic t : taxonomy) {
      StudentTopicPerformance p = perfByTopic.get(t.getId());
      int topicAttempts = 0;
      double accuracy = 0.0;
      OffsetDateTime lastAttemptAt = null;
      if (p != null) {
        topicAttempts = p.getAttempts();
        BigDecimal acc = p.getAccuracy();
        if (acc != null) {
          accuracy = acc.doubleValue();
        }
        lastAttemptAt = p.getLastAttemptAt();
      }

      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      int editions = editionsByTopic.getOrDefault(t.getId(), 0);
      double frequencyFactor = frequencyFactor(hist, maxHist, editions, maxEditions);
      double performanceFactor = 1.0 - accuracy; // quanto menor o acerto, maior a prioridade
      double recencyFactor = recencyFactor(lastAttemptAt, now);
      double difficultyFactor = difficultyFactor(difficultyMixByTopic.get(t.getId()));

      // Score auditável: frequência histórica × (1 − acerto) × dificuldade × recência.
      double score = frequencyFactor * performanceFactor * difficultyFactor * recencyFactor;

      // Lacuna (0 tentativas): piso + ordenação por frequência entre lacunas.
      if (topicAttempts == 0) {
        score = GAP_FLOOR + 0.1 * frequencyFactor;
      }

      scores.add(new TopicScore(t, score, accuracy, topicAttempts,
          hist, editions, frequencyFactor, performanceFactor, recencyFactor,
          difficultyFactor, lastAttemptAt));
    }

    // Ordenação determinística: maior score primeiro, desempate por código
    scores.sort((a, b) -> {
      int cmp = Double.compare(b.score, a.score);
      if (cmp != 0) return cmp;
      return a.topic.getCode().compareTo(b.topic.getCode());
    });

    // Faixas de prioridade: rank 0 → P1 (menor número = maior prioridade,
    // como o frontend ordena). A carga (urgência/porte) concentra ou alarga
    // as faixas sem filtrar assunto.
    int bandSize = priorityBandSize(scores.size(), loadMultiplier);
    for (int i = 0; i < scores.size(); i++) {
      TopicScore ts = scores.get(i);
      Topic t = ts.topic;
      String reason = buildReason(t, ts, loadMultiplier, provisorio);
      List<Integer> years = editionYearsByTopic.getOrDefault(t.getId(), List.of());
      List<Long> sampleIds = classifications.officialQuestionIdsByTopic(t.getId()).stream()
          .limit(EVIDENCE_SAMPLE_LIMIT).toList();
      String evidence = buildEvidence(t.getId(), ts, years, sampleIds);
      int priority = Math.min(5, 1 + i / bandSize);
      Long subtopicId = weakestSubtopicId(t.getId(), subPerfByTopic.get(t.getId()));

      StudyPlanItem item = new StudyPlanItem(
          plan,
          t.getId(),
          subtopicId,
          (short) priority,
          reason,
          evidence
      );
      out.add(item);
    }

    return out;
  }

  /**
   * Recomendações escopadas por processo (TASK E.2): mesma fórmula v2, mas
   * frequência (D.2 do processo), desempenho e recência calculados só na
   * trilha. Desempenho/recência vêm do fato {@code question_attempts}
   * filtrado (autorais sem edição contam em ambas); evidência cita edições
   * e questões OFICIAIS do processo existentes no banco.
   */
  private List<StudyPlanItem> buildRecommendationsForInstitution(
      long userId, StudyPlan plan, boolean provisorio, String institution) {
    List<Topic> taxonomy = topics.findAllOrdered();
    List<StudyPlanItem> out = new ArrayList<>();

    // Vigente por questão (mesmo critério das TASKs 4.1–4.2).
    List<QuestionAttempt> fetched = attempts.findAllByUserIdWithQuestionAndExam(userId);
    List<QuestionAttempt> scoped = new ArrayList<>(fetched.size());
    for (QuestionAttempt a : fetched) {
      String examInstitution = a.getQuestion() != null && a.getQuestion().getExam() != null
          ? a.getQuestion().getExam().getInstitution()
          : null;
      if (examInstitution == null || institution.equals(examInstitution)) {
        scoped.add(a);
      }
    }
    Map<Long, QuestionClassification> vigente = vigenteByQuestionForInstitution(scoped);

    // Desempenho da trilha a partir do fato (só pontuáveis com tópico).
    Map<Long, TrackAcc> perfByTopic = new HashMap<>();
    Map<Long, List<SubAcc>> subPerfByTopic = new HashMap<>();
    for (QuestionAttempt a : scoped) {
      if (a.isAnnulled()) {
        continue;
      }
      QuestionClassification c = vigente.get(a.getQuestion().getId());
      if (c == null || c.getTopic() == null) {
        continue;
      }
      boolean hit = Boolean.TRUE.equals(a.getCorrect());
      perfByTopic.computeIfAbsent(c.getTopic().getId(), k -> new TrackAcc())
          .add(hit, a.getAnsweredAt());
      if (c.getSubtopic() != null) {
        subPerfByTopic.computeIfAbsent(c.getTopic().getId(), k -> new ArrayList<>());
        List<SubAcc> bucket = subPerfByTopic.get(c.getTopic().getId());
        SubAcc existing = null;
        for (SubAcc s : bucket) {
          if (s.subtopicId.equals(c.getSubtopic().getId())) {
            existing = s;
            break;
          }
        }
        if (existing == null) {
          existing = new SubAcc(c.getSubtopic().getId(), c.getSubtopic().getCode());
          bucket.add(existing);
        }
        existing.add(hit);
      }
    }

    // Frequência histórica do processo (D.2).
    Map<Long, Long> histByTopic = new HashMap<>();
    for (Object[] row : classifications.countByTopicForInstitution(institution)) {
      histByTopic.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
    Map<Long, Integer> editionsByTopic = new HashMap<>();
    Map<Long, List<Integer>> editionYearsByTopic = new HashMap<>();
    long maxHist = 0;
    int maxEditions = 0;
    for (Topic t : taxonomy) {
      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      List<Integer> years = classifications
          .editionsByTopicForInstitution(t.getId(), institution).stream()
          .map(Short::intValue).sorted().toList();
      editionYearsByTopic.put(t.getId(), years);
      editionsByTopic.put(t.getId(), years.size());
      if (hist > maxHist) {
        maxHist = hist;
      }
      if (years.size() > maxEditions) {
        maxEditions = years.size();
      }
    }

    Map<Long, long[]> difficultyMixByTopic = new HashMap<>();
    for (Object[] row : classifications.difficultyByTopicForInstitution(institution)) {
      Long topicId = ((Number) row[0]).longValue();
      String difficulty = row[1] == null ? null : row[1].toString();
      long total = ((Number) row[2]).longValue();
      long[] mix = difficultyMixByTopic.computeIfAbsent(topicId, k -> new long[3]);
      if ("FACIL".equals(difficulty)) {
        mix[0] += total;
      } else if ("MEDIA".equals(difficulty)) {
        mix[1] += total;
      } else if ("DIFICIL".equals(difficulty)) {
        mix[2] += total;
      }
    }

    StudentProfile profile = profiles.findById(userId).orElse(null);
    int currentYear = OffsetDateTime.now().getYear();
    double loadMultiplier = loadMultiplier(
        profile == null ? null : profile.getTargetYear(),
        profile == null ? null : profile.getStudyGoal(),
        currentYear);

    List<TopicScore> scores = new ArrayList<>();
    OffsetDateTime now = OffsetDateTime.now();
    for (Topic t : taxonomy) {
      TrackAcc p = perfByTopic.get(t.getId());
      int topicAttempts = p == null ? 0 : p.attempts;
      double accuracy = p == null ? 0.0 : p.accuracy();
      OffsetDateTime lastAttemptAt = p == null ? null : p.last;

      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      int editions = editionsByTopic.getOrDefault(t.getId(), 0);
      double frequencyFactor = frequencyFactor(hist, maxHist, editions, maxEditions);
      double performanceFactor = 1.0 - accuracy;
      double recencyFactor = recencyFactor(lastAttemptAt, now);
      double difficultyFactor = difficultyFactor(difficultyMixByTopic.get(t.getId()));

      double score = frequencyFactor * performanceFactor * difficultyFactor * recencyFactor;
      if (topicAttempts == 0) {
        score = GAP_FLOOR + 0.1 * frequencyFactor;
      }

      scores.add(new TopicScore(t, score, accuracy, topicAttempts,
          hist, editions, frequencyFactor, performanceFactor, recencyFactor,
          difficultyFactor, lastAttemptAt));
    }

    scores.sort((a, b) -> {
      int cmp = Double.compare(b.score, a.score);
      if (cmp != 0) return cmp;
      return a.topic.getCode().compareTo(b.topic.getCode());
    });

    int bandSize = priorityBandSize(scores.size(), loadMultiplier);
    for (int i = 0; i < scores.size(); i++) {
      TopicScore ts = scores.get(i);
      Topic t = ts.topic;
      String reason = buildReasonForInstitution(t, ts, loadMultiplier, provisorio, institution,
          editionYearsByTopic.getOrDefault(t.getId(), List.of()));
      List<Object[]> evidenceRows = classifications
          .officialQuestionEvidenceByTopicForInstitution(t.getId(), institution);
      List<Long> sampleIds = new ArrayList<>();
      List<String> sampleLabels = new ArrayList<>();
      for (Object[] row : evidenceRows) {
        if (sampleIds.size() >= EVIDENCE_SAMPLE_LIMIT) {
          break;
        }
        Long qid = ((Number) row[0]).longValue();
        String inst = row[1] == null ? institution : String.valueOf(row[1]);
        Object yearObj = row[2];
        Object numObj = row[3];
        String year = yearObj == null ? "?" : String.valueOf(((Number) yearObj).intValue());
        String num = numObj == null ? "?" : String.valueOf(((Number) numObj).intValue());
        sampleIds.add(qid);
        sampleLabels.add(inst + " " + year + " Q" + num);
      }
      List<Integer> years = editionYearsByTopic.getOrDefault(t.getId(), List.of());
      String evidence = buildEvidenceForInstitution(
          t.getId(), ts, years, sampleIds, sampleLabels, institution);
      int priority = Math.min(5, 1 + i / bandSize);
      Long subtopicId = weakestSubtopicIdForInstitution(
          t.getId(), subPerfByTopic.get(t.getId()));

      StudyPlanItem item = new StudyPlanItem(
          plan,
          t.getId(),
          subtopicId,
          (short) priority,
          reason,
          evidence
      );
      out.add(item);
    }

    return out;
  }

  /**
   * Vigente por questão no recorte da trilha (mesmo critério das TASKs
   * 4.1–4.2: maior id por questão).
   */
  private Map<Long, QuestionClassification> vigenteByQuestionForInstitution(
      List<QuestionAttempt> scoped) {
    Map<Long, QuestionClassification> out = new HashMap<>();
    if (scoped.isEmpty()) {
      return out;
    }
    java.util.Set<Long> ids = new java.util.HashSet<>();
    for (QuestionAttempt a : scoped) {
      ids.add(a.getQuestion().getId());
    }
    for (QuestionClassification c
        : classifications.findActiveByQuestionIds(new ArrayList<>(ids))) {
      out.putIfAbsent(c.getQuestion().getId(), c);
    }
    return out;
  }

  /**
   * Subassunto mais fraco na trilha (TASK E.2): menor aproveitamento no fato
   * filtrado, desempate por mais tentativas, depois código. Coerência checada
   * no {@code SubtopicRepository}; sem sinal ou incoerente → NULL.
   */
  private Long weakestSubtopicIdForInstitution(Long topicId, List<SubAcc> candidates) {
    if (candidates == null || candidates.isEmpty()) {
      return null;
    }
    List<SubAcc> ordered = new ArrayList<>(candidates);
    ordered.sort((a, b) -> {
      int cmp = Double.compare(a.accuracy(), b.accuracy());
      if (cmp != 0) return cmp;
      cmp = Integer.compare(b.attempts, a.attempts);
      if (cmp != 0) return cmp;
      return String.valueOf(a.code).compareTo(String.valueOf(b.code));
    });
    for (SubAcc s : ordered) {
      boolean coherent = subtopics.findByIdWithTopic(s.subtopicId)
          .map(t -> t.getTopic() != null && topicId.equals(t.getTopic().getId()))
          .orElse(false);
      if (coherent) {
        return s.subtopicId;
      }
    }
    return null;
  }

  /**
   * Motivo textual na trilha (mesmos fatores do score + rótulo do processo e
   * anos da trilha — ex. EAJ 2021/2022/2025).
   */
  private String buildReasonForInstitution(
      Topic topic, TopicScore ts, double loadMultiplier, boolean provisorio,
      String institution, List<Integer> years) {
    StringBuilder sb = new StringBuilder();
    sb.append("Recomendação [trilha ").append(institution).append("] para ")
        .append(topic.getCode()).append(" (").append(topic.getName()).append(") — ");

    if (ts.attempts == 0) {
      sb.append("nenhuma tentativa pontuável registrada nesta trilha (lacuna); ");
    } else {
      sb.append(ts.attempts).append(" tentativa(s) pontuáveis nesta trilha; aproveitamento ")
          .append(Math.round(ts.accuracy * 100)).append("%; ");
      if (ts.accuracy < 0.5) {
        sb.append("abaixo do limiar FRÁGIL (< 50%). ");
      } else if (ts.accuracy < 0.7) {
        sb.append("faixa intermediária (50–70%). ");
      } else {
        sb.append("consolidado (>= 70%) — revisão para manutenção. ");
      }
      if (ts.lastAttemptAt != null) {
        long days = Math.max(0, ChronoUnit.DAYS.between(ts.lastAttemptAt, OffsetDateTime.now()));
        sb.append("Última tentativa nesta trilha há ").append(days).append(" dia(s). ");
      }
    }

    sb.append("Caiu em ").append(ts.hist).append(" questão(ões) de ")
        .append(ts.editions).append(" edição(ões) do ").append(institution);
    if (years != null && !years.isEmpty()) {
      sb.append(" (");
      for (int i = 0; i < years.size(); i++) {
        if (i > 0) sb.append(", ");
        sb.append(years.get(i));
      }
      sb.append(")");
    }
    sb.append(" (freq ").append(Math.round(ts.frequencyFactor * 100)).append("). ");
    sb.append("Score v2.1 determinístico ").append(Math.round(ts.score * 100)).append(".");
    if (loadMultiplier > 1.0) {
      sb.append(" Carga ajustada pela sua meta/ano-alvo (só concentra o topo, sem filtrar assunto).");
    }

    sb.append(" Evidência derivada de classificações vigentes da trilha + desempenho registrado nela.");
    if (provisorio) {
      sb.append(" Plano provisório: comece pelo que mais cai — vira pessoal após o diagnóstico (3+ pontuáveis na trilha).");
    }
    return sb.toString();
  }

  /**
   * Evidência rastreável da trilha (TASK E.2): edições e questões OFICIAIS do
   * processo existentes no banco (ex. {@code "EAJ 2022 Q12"} em
   * {@code sampleLabels}). Sem questão oficial na trilha =
   * {@code sampleQuestionIds: []} honesto, nunca id inventado.
   */
  private String buildEvidenceForInstitution(
      Long topicId, TopicScore ts, List<Integer> years, List<Long> sampleIds,
      List<String> sampleLabels, String institution) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"topic_id\":").append(topicId);
    sb.append(",\"institution\":\"").append(institution).append("\"");
    sb.append(",\"historicalQuestions\":").append(ts.hist);
    sb.append(",\"editionsCount\":").append(years == null ? 0 : years.size());
    sb.append(",\"editions\":[");
    if (years != null) {
      for (int i = 0; i < years.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append(years.get(i));
      }
    }
    sb.append("],\"sampleQuestionIds\":[");
    if (sampleIds != null) {
      for (int i = 0; i < sampleIds.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append(sampleIds.get(i));
      }
    }
    sb.append("],\"sampleLabels\":[");
    if (sampleLabels != null) {
      for (int i = 0; i < sampleLabels.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append("\"").append(sampleLabels.get(i).replace("\"", "")).append("\"");
      }
    }
    sb.append("],\"accuracy\":").append(ts.accuracy);
    sb.append(",\"attempts\":").append(ts.attempts);
    sb.append(",\"lastAttemptAt\":");
    if (ts.lastAttemptAt == null) {
      sb.append("null");
    } else {
      sb.append("\"").append(ts.lastAttemptAt).append("\"");
    }
    sb.append(",\"algorithmVersion\":\"").append(ALGORITHM_VERSION_INSTITUTION).append("\"}");
    return sb.toString();
  }

  private static final class TrackAcc {
    int attempts;
    int hits;
    OffsetDateTime last;

    void add(boolean hit, OffsetDateTime answeredAt) {
      attempts++;
      if (hit) {
        hits++;
      }
      if (answeredAt != null && (last == null || answeredAt.isAfter(last))) {
        last = answeredAt;
      }
    }

    double accuracy() {
      return attempts == 0 ? 0.0 : hits / (double) attempts;
    }
  }

  private static final class SubAcc {
    final Long subtopicId;
    final String code;
    int attempts;
    int hits;

    SubAcc(Long subtopicId, String code) {
      this.subtopicId = subtopicId;
      this.code = code;
    }

    void add(boolean hit) {
      attempts++;
      if (hit) {
        hits++;
      }
    }

    double accuracy() {
      return attempts == 0 ? 1.0 : hits / (double) attempts;
    }
  }

  /**
   * Subassunto mais fraco do tópico com sinal do aluno (TASK 18.2): menor
   * accuracy, desempate por mais tentativas, depois por código (determinístico).
   * A coerência (subassunto pertence ao tópico) é checada no
   * {@code SubtopicRepository} (antes ocioso); sem sinal ou incoerente → NULL.
   */
  Long weakestSubtopicId(Long topicId, List<StudentTopicPerformance> candidates) {
    if (candidates == null || candidates.isEmpty()) {
      return null;
    }
    List<StudentTopicPerformance> ordered = new ArrayList<>(candidates);
    ordered.sort((a, b) -> {
      double accA = accuracyOrOne(a);
      double accB = accuracyOrOne(b);
      int cmp = Double.compare(accA, accB);
      if (cmp != 0) return cmp;
      cmp = Integer.compare(b.getAttempts(), a.getAttempts());
      if (cmp != 0) return cmp;
      String codeA = a.getSubtopic() == null ? "" : String.valueOf(a.getSubtopic().getCode());
      String codeB = b.getSubtopic() == null ? "" : String.valueOf(b.getSubtopic().getCode());
      return codeA.compareTo(codeB);
    });
    for (StudentTopicPerformance p : ordered) {
      if (p.getSubtopic() == null || p.getSubtopic().getId() == null) {
        continue;
      }
      boolean coherent = subtopics.findByIdWithTopic(p.getSubtopic().getId())
          .map(s -> s.getTopic() != null && topicId.equals(s.getTopic().getId()))
          .orElse(false);
      if (coherent) {
        return p.getSubtopic().getId();
      }
    }
    return null;
  }

  private static double accuracyOrOne(StudentTopicPerformance p) {
    BigDecimal acc = p.getAccuracy();
    return acc == null ? 1.0 : acc.doubleValue();
  }

  /**
   * Frequência histórica normalizada [0,1]: 70% contagem + 30% amplitude em
   * edições (pesos em {@code docs/api-recomendacao.md}). Banco vazio → 0.
   */
  static double frequencyFactor(long hist, long maxHist, int editions, int maxEditions) {
    double countShare = maxHist <= 0 ? 0.0 : (double) hist / maxHist;
    double editionShare = maxEditions <= 0 ? 0.0 : (double) editions / maxEditions;
    return WEIGHT_HIST_COUNT * countShare + (1.0 - WEIGHT_HIST_COUNT) * editionShare;
  }

  /**
   * Recência [1.0, 1.5]: sem sinal → neutro; senão +dias/90 até o teto.
   * Quanto mais parado o assunto, maior a prioridade (revisão espaçada).
   */
  static double recencyFactor(OffsetDateTime lastAttemptAt, OffsetDateTime now) {
    if (lastAttemptAt == null || now == null) {
      return 1.0;
    }
    long days = ChronoUnit.DAYS.between(lastAttemptAt, now);
    if (days < 0) {
      days = 0;
    }
    return 1.0 + Math.min(RECENCY_BONUS_CAP, days / RECENCY_FULL_DAYS);
  }

  /**
   * Dificuldade [1.0, 1.5] pelo mix do assunto: 1 + 0.5 × (MEDIA+DIFICIL)/total.
   * Sem questões com dificuldade marcada → neutro (DESCONHECIDO, nunca inventado).
   *
   * @param mix {@code [facil, media, dificil]} ou {@code null} sem sinal
   */
  static double difficultyFactor(long[] mix) {
    if (mix == null) {
      return 1.0;
    }
    long total = mix[0] + mix[1] + mix[2];
    if (total <= 0) {
      return 1.0;
    }
    double hardShare = (double) (mix[1] + mix[2]) / total;
    return 1.0 + DIFFICULTY_BONUS_CAP * hardShare;
  }

  /**
   * Carga por urgência/porte (só banding, nunca filtro): prova neste ano ou
   * atrasada → 1.2; no próximo → 1.1; demais/ausente → 1.0. Meta declarada
   * (study_goal não-branco) soma 0.05. Tudo documentado como chute explícito.
   */
  static double loadMultiplier(Short targetYear, String studyGoal, int currentYear) {
    double urgency = 1.0;
    if (targetYear != null) {
      int yearsLeft = targetYear - currentYear;
      if (yearsLeft <= 0) {
        urgency = 1.2;
      } else if (yearsLeft == 1) {
        urgency = 1.1;
      }
    }
    double goal = (studyGoal != null && !studyGoal.isBlank()) ? 1.05 : 1.0;
    return urgency * goal;
  }

  /** Tamanho da faixa de prioridade: quintis ÷ carga (mínimo 1). */
  static int priorityBandSize(int size, double loadMultiplier) {
    if (size <= 0) {
      return 1;
    }
    double safeLoad = loadMultiplier <= 0 ? 1.0 : loadMultiplier;
    return Math.max(1, (int) Math.round(size / (5.0 * safeLoad)));
  }

  /**
   * Gera o motivo textual explicável da recomendação, derivado dos mesmos
   * fatores que classificam o item (evidência antes de opinião).
   */
  private String buildReason(Topic topic, TopicScore ts, double loadMultiplier, boolean provisorio) {
    StringBuilder sb = new StringBuilder();
    sb.append("Recomendação para ").append(topic.getCode()).append(" (");
    sb.append(topic.getName()).append(") — ");

    if (ts.attempts == 0) {
      sb.append("nenhuma tentativa pontuável registrada (lacuna); ");
    } else {
      sb.append(ts.attempts).append(" tentativa(s) pontuáveis; aproveitamento ")
          .append(Math.round(ts.accuracy * 100)).append("%; ");
      if (ts.accuracy < 0.5) {
        sb.append("abaixo do limiar FRÁGIL (< 50%). ");
      } else if (ts.accuracy < 0.7) {
        sb.append("faixa intermediária (50–70%). ");
      } else {
        sb.append("consolidado (>= 70%) — revisão para manutenção. ");
      }
      if (ts.lastAttemptAt != null) {
        long days = Math.max(0, ChronoUnit.DAYS.between(ts.lastAttemptAt, OffsetDateTime.now()));
        sb.append("Última tentativa há ").append(days).append(" dia(s). ");
      }
    }

    sb.append("Caiu em ").append(ts.hist).append(" questão(ões) de ")
        .append(ts.editions).append(" edição(ões) ");
    sb.append("(freq ").append(Math.round(ts.frequencyFactor * 100)).append("). ");
    sb.append("Score v2 determinístico ").append(Math.round(ts.score * 100)).append(".");
    if (loadMultiplier > 1.0) {
      sb.append(" Carga ajustada pela sua meta/ano-alvo (só concentra o topo, sem filtrar assunto).");
    }

    sb.append(" Evidência derivada de classificações vigentes + desempenho registrado.");
    if (provisorio) {
      sb.append(" Plano provisório: comece pelo que mais cai — vira pessoal após o diagnóstico (3+ pontuáveis).");
    }
    return sb.toString();
  }

  /**
   * Gera o JSON de evidência rastreável (TASK 18.2), apontando para edições e
   * questões OFICIAIS que sustentam a recomendação (AGENTS.md §8, §11).
   * Autorais/adaptadas nunca aparecem em {@code sampleQuestionIds} (regra Fase 15).
   */
  private String buildEvidence(Long topicId, TopicScore ts, List<Integer> years, List<Long> sampleIds) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"topic_id\":").append(topicId);
    sb.append(",\"historicalQuestions\":").append(ts.hist);
    sb.append(",\"editionsCount\":").append(years == null ? 0 : years.size());
    sb.append(",\"editions\":[");
    if (years != null) {
      for (int i = 0; i < years.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append(years.get(i));
      }
    }
    sb.append("],\"sampleQuestionIds\":[");
    if (sampleIds != null) {
      for (int i = 0; i < sampleIds.size(); i++) {
        if (i > 0) sb.append(",");
        sb.append(sampleIds.get(i));
      }
    }
    sb.append("],\"accuracy\":").append(ts.accuracy);
    sb.append(",\"attempts\":").append(ts.attempts);
    sb.append(",\"lastAttemptAt\":");
    if (ts.lastAttemptAt == null) {
      sb.append("null");
    } else {
      sb.append("\"").append(ts.lastAttemptAt).append("\"");
    }
    sb.append(",\"algorithmVersion\":\"").append(ALGORITHM_VERSION).append("\"}");
    return sb.toString();
  }

  /**
   * Recupera o roteiro vigente da trilha (TASK E.2).
   *
   * @param institution {@code IFRN} ou {@code EAJ}; {@code null}/em-branco =
   *     roteiro global legado (comportamento pré-E.2). Informado = trilha
   *     isolada (sem plano na trilha → {@code 404 NO_ACTIVE_PLAN}, nunca
   *     plano de outro processo disfarçado).
   */
  @Transactional(readOnly = true)
  public StudyPlanResponse getPlan(long userId, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    if (instNorm == null) {
      return getPlan(userId);
    }
    StudyPlan plan = studyPlans.findByUserIdAndInstitutionAndIsActiveTrue(userId, instNorm)
        .orElseThrow(() -> new ResourceNotFoundException("NO_ACTIVE_PLAN",
            "Nenhum roteiro vigente na trilha " + instNorm + ". Gere um roteiro primeiro."));
    List<StudyPlanItem> items = studyPlanItems.findByStudyPlanIdOrderByPriorityAsc(plan.getId());
    return toResponse(plan, items);
  }

  /**
   * Recupera o roteiro vigente do aluno.
   */
  @Transactional(readOnly = true)
  public StudyPlanResponse getPlan(long userId) {
    StudyPlan plan = studyPlans.findByUserIdAndIsActiveTrue(userId)
        .orElseThrow(() -> new ResourceNotFoundException("NO_ACTIVE_PLAN",
            "Nenhum roteiro vigente. Gere um roteiro primeiro."));
    List<StudyPlanItem> items = studyPlanItems.findByStudyPlanIdOrderByPriorityAsc(plan.getId());
    return toResponse(plan, items);
  }

  /**
   * Recupera os itens do roteiro ordenados por prioridade.
   */
  @Transactional(readOnly = true)
  public List<StudyPlanItem> getPlanItems(Long studyPlanId) {
    return studyPlanItems.findByStudyPlanIdOrderByPriorityAsc(studyPlanId);
  }

  /**
   * Registra uma atualização de status de item do roteiro pelo dono do token.
   *
   * <p>TASK 16.1 (P1 — IDOR): o {@code userId} é sempre o autenticado
   * (o controller não aceita mais {@code ?userId}). Item de plano alheio
   * responde {@code 403} via {@link AccessDeniedException} (nunca 404
   * silencioso nem 200 cruzado).
   */
  @Transactional
  public StudyPlanItemResponse updateItemStatus(Long itemId, String status, long principalUserId) {
    if (status == null
        || (!status.equals("TODO") && !status.equals("DOING")
            && !status.equals("DONE") && !status.equals("SKIPPED"))) {
      throw new BadRequestException("INVALID_STATUS",
          "Status inválido. Use TODO, DOING, DONE ou SKIPPED.");
    }
    StudyPlanItem item = studyPlanItems.findById(itemId)
        .orElseThrow(() -> new ResourceNotFoundException("ITEM_NOT_FOUND",
            "Item do roteiro não encontrado."));
    StudyPlan plan = item.getStudyPlan();
    Long ownerId = (plan != null) ? plan.getUserId() : null;
    if (ownerId == null && plan != null && plan.getId() != null) {
      // Plano pode chegar como proxy lazy sem userId carregado: recarrega o dono.
      ownerId = studyPlans.findById(plan.getId())
          .map(StudyPlan::getUserId)
          .orElse(null);
    }
    if (ownerId == null || ownerId.longValue() != principalUserId) {
      throw new AccessDeniedException("Item de roteiro de outro aluno.");
    }
    item.setStatus(status);
    return toResponse(studyPlanItems.save(item));
  }

  /**
   * Converte entidade em DTO (TASK 16.1 — nunca vazar JPA no JSON).
   */
  private static StudyPlanItemResponse toResponse(StudyPlanItem item) {
    return new StudyPlanItemResponse(
        item.getId(),
        item.getTopicId(),
        item.getSubtopicId(),
        item.getPriority(),
        item.getReason(),
        item.getEvidenceJson(),
        item.getStatus(),
        item.getCreatedAt(),
        item.getUpdatedAt());
  }

  private static StudyPlanResponse toResponse(StudyPlan plan, List<StudyPlanItem> items) {
    List<StudyPlanItemResponse> dtoItems = (items == null) ? List.of()
        : items.stream().map(RecommendationService::toResponse).toList();
    return new StudyPlanResponse(
        plan.getId(),
        plan.getUserId(),
        plan.getIsActive(),
        plan.getAlgorithmVersion(),
        plan.getStatus(),
        plan.getGeneratedAt(),
        plan.getCreatedAt(),
        plan.getUpdatedAt(),
        dtoItems);
  }

  /**
   * Classe auxiliar interna para calcular score determinístico por tópico.
   */
  private static class TopicScore {
    final Topic topic;
    final double score;
    final double accuracy;
    final int attempts;
    final long hist;
    final int editions;
    final double frequencyFactor;
    final double performanceFactor;
    final double recencyFactor;
    final double difficultyFactor;
    final OffsetDateTime lastAttemptAt;

    TopicScore(Topic topic, double score, double accuracy, int attempts,
        long hist, int editions, double frequencyFactor, double performanceFactor,
        double recencyFactor, double difficultyFactor, OffsetDateTime lastAttemptAt) {
      this.topic = topic;
      this.score = score;
      this.accuracy = accuracy;
      this.attempts = attempts;
      this.hist = hist;
      this.editions = editions;
      this.frequencyFactor = frequencyFactor;
      this.performanceFactor = performanceFactor;
      this.recencyFactor = recencyFactor;
      this.difficultyFactor = difficultyFactor;
      this.lastAttemptAt = lastAttemptAt;
    }
  }
}
