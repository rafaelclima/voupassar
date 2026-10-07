package br.com.voupassar.studyplan.service;

import br.com.voupassar.auth.entity.StudentProfile;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.StudentProfileRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
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

  private final UserRepository users;
  private final StudyPlanRepository studyPlans;
  private final StudyPlanItemRepository studyPlanItems;
  private final TopicRepository topics;
  private final SubtopicRepository subtopics;
  private final QuestionAttemptRepository attempts;
  private final StudentTopicPerformanceRepository performanceRepo;
  private final QuestionClassificationRepository classifications;
  private final StudentProfileRepository profiles;

  public RecommendationService(
      UserRepository users,
      StudyPlanRepository studyPlans,
      StudyPlanItemRepository studyPlanItems,
      TopicRepository topics,
      SubtopicRepository subtopics,
      QuestionAttemptRepository attempts,
      StudentTopicPerformanceRepository performanceRepo,
      QuestionClassificationRepository classifications,
      StudentProfileRepository profiles) {
    this.users = users;
    this.studyPlans = studyPlans;
    this.studyPlanItems = studyPlanItems;
    this.topics = topics;
    this.subtopics = subtopics;
    this.attempts = attempts;
    this.performanceRepo = performanceRepo;
    this.classifications = classifications;
    this.profiles = profiles;
  }

  /**
   * Gera (ou regenera) o roteiro de estudos do aluno.
   *
   * <p>Fluxo:
   * <ol>
   *   <li>Verifica se já existe um plano ativo para o usuário;</li>
 *   <li>Se existir, desativa o anterior e cria um novo (não apaga histórico);</li>
 *   <li>Calcula prioridades para todos os tópicos via algoritmo determinístico;</li>
 *   <li>Cria itens do roteiro com motivo explicável e evidência rastreável.</li>
 * </ol>
   */
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

    StudyPlan newPlan = new StudyPlan(userId, ALGORITHM_VERSION);
    StudyPlan savedPlan = studyPlans.save(newPlan);
    if (savedPlan == null) {
      savedPlan = newPlan;
    }

    // Calcula prioridades determinísticas
    List<StudyPlanItem> items = buildRecommendations(userId, savedPlan);
    studyPlanItems.saveAll(items);

    savedPlan.setItems(items);
    return toResponse(savedPlan, items);
  }

  /**
   * Constrói a lista de recomendações para o usuário (score v2, TASK 18.1).
   */
  private List<StudyPlanItem> buildRecommendations(long userId, StudyPlan plan) {
    List<Topic> taxonomy = topics.findAllOrdered();
    List<StudyPlanItem> out = new ArrayList<>();

    // Performance materializada por usuário (rebuild automático na 4.1)
    Map<Long, StudentTopicPerformance> perfByTopic = new HashMap<>();
    for (StudentTopicPerformance p : performanceRepo.findProgressByUserId(userId)) {
      if (p.getTopic() != null) {
        perfByTopic.put(p.getTopic().getId(), p);
      }
    }

    // Frequência histórica real: mesma fonte do diagnóstico
    // (DiagnosisService.java:147-154 — countByTopic + editionsByTopic).
    Map<Long, Long> histByTopic = new HashMap<>();
    for (Object[] row : classifications.countByTopic()) {
      histByTopic.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
    Map<Long, Integer> editionsByTopic = new HashMap<>();
    long maxHist = 0;
    int maxEditions = 0;
    for (Topic t : taxonomy) {
      long hist = histByTopic.getOrDefault(t.getId(), 0L);
      int editions = classifications.editionsByTopic(t.getId()).size();
      editionsByTopic.put(t.getId(), editions);
      if (hist > maxHist) {
        maxHist = hist;
      }
      if (editions > maxEditions) {
        maxEditions = editions;
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
      String reason = buildReason(t, ts, loadMultiplier);
      String evidence = buildEvidence(t.getId(), ts.attempts);
      int priority = Math.min(5, 1 + i / bandSize);

      StudyPlanItem item = new StudyPlanItem(
          plan,
          t.getId(),
          null, // subtopic refinado na TASK 18.2 quando houver sinal
          (short) priority,
          reason,
          evidence
      );
      out.add(item);
    }

    return out;
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
  private String buildReason(Topic topic, TopicScore ts, double loadMultiplier) {
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
    return sb.toString();
  }

  /**
   * Gera o JSON de evidência rastreável, apontando para edições/questões
   * que sustentam a recomendação (AGENTS.md §8, §11).
   */
  private String buildEvidence(Long topicId, int attempts) {
    // Versão inicial simplificada: evidencia a existência do tópico e a quantidade de tentativas.
    // Na Fase 4 completa, deve referenciar edições/questões concretas via question_classifications.
    return "{\"topic_id\":" + topicId
        + ",\"historical_evidence\":\"taxonomia_v1.1\",\"source_type\":\"DERIVADO_EVIDENCIA\","
        + "\"attempts\":" + attempts
        + ",\"note\":\"Evidência completa requer vinculação a edições (TASK 1.5, 11.2).\"}";
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
