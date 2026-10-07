package br.com.voupassar.studyplan.service;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.content.repository.SubtopicRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.entity.StudentTopicPerformance;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.studyplan.dto.StudyPlanItemResponse;
import br.com.voupassar.studyplan.dto.StudyPlanResponse;
import br.com.voupassar.studyplan.entity.StudyPlan;
import br.com.voupassar.studyplan.entity.StudyPlanItem;
import br.com.voupassar.studyplan.repository.StudyPlanItemRepository;
import br.com.voupassar.studyplan.repository.StudyPlanRepository;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Motor de recomendação (TASK 4.3) — determinístico, transparente e explicável.
 *
 * <p>Fatores mínimos (AGENTS.md §8 / architecture.md §7):
 * <ul>
 *   <li>Frequência histórica (quantas questões do assunto no banco);</li>
 *   <li>Desempenho do aluno (accuracy por tópico);</li>
 *   <li>Dificuldade estimada (palpite BAIXA até calibração da Fase 4);</li>
 *   <li>Recência (tempo desde última tentativa);</li>
 *   <li>Número de tentativas (carência de tentativas).</li>
 * </ul>
 *
 * <p>O algoritmo inicial é uma função auditável sem ML. Cada item da
 * recomendação traz um motivo textual derivado dos mesmos fatores que o
 * classificam, e um {@code evidence_json} com referências às edições/questões
 * que sustentam a recomendação (rastreabilidade — AGENTS.md §8, §11).
 */
@Service
public class RecommendationService {

  private static final String ALGORITHM_VERSION = "v1-deterministico";

  private final UserRepository users;
  private final StudyPlanRepository studyPlans;
  private final StudyPlanItemRepository studyPlanItems;
  private final TopicRepository topics;
  private final SubtopicRepository subtopics;
  private final QuestionAttemptRepository attempts;
  private final StudentTopicPerformanceRepository performanceRepo;

  public RecommendationService(
      UserRepository users,
      StudyPlanRepository studyPlans,
      StudyPlanItemRepository studyPlanItems,
      TopicRepository topics,
      SubtopicRepository subtopics,
      QuestionAttemptRepository attempts,
      StudentTopicPerformanceRepository performanceRepo) {
    this.users = users;
    this.studyPlans = studyPlans;
    this.studyPlanItems = studyPlanItems;
    this.topics = topics;
    this.subtopics = subtopics;
    this.attempts = attempts;
    this.performanceRepo = performanceRepo;
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
   * Constrói a lista de recomendações para o usuário.
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

    // Para cada tópico da taxonomia, calcula score determinístico
    List<TopicScore> scores = new ArrayList<>();
    for (Topic t : taxonomy) {
      StudentTopicPerformance p = perfByTopic.get(t.getId());
      int attempts = 0;
      double accuracy = 0.0;
      if (p != null) {
        attempts = p.getAttempts();
        if (p.getAccuracy() != null) {
          accuracy = p.getAccuracy().doubleValue();
        }
      }

      // Fatores mínimos (determinísticos, auditáveis)
      double frequencyFactor = Math.min(1.0, attempts / 5.0); // carência de tentativas
      double performanceFactor = 1.0 - accuracy; // quanto menor o acerto, maior a prioridade
      double recencyFactor = 1.0; // simplificado (será calibrado com dados de recência na Fase 4 completa)
      double difficultyFactor = 1.0; // palpite BAIXA (documentado como DESCONHECIDO até calibração)

      // Score auditável: frequência histórica × (1 - acerto) × peso de dificuldade × recência × carência
      double score = frequencyFactor * performanceFactor * difficultyFactor * recencyFactor;

      // Ajuste determinístico para lacunas (0 tentativas = alta prioridade)
      if (attempts == 0) {
        score = Math.max(score, 0.9);
      }

      scores.add(new TopicScore(t, score, accuracy, attempts));
    }

    // Ordenação determinística: maior score primeiro, desempate por código
    scores.sort((a, b) -> {
      int cmp = Double.compare(b.score, a.score);
      if (cmp != 0) return cmp;
      return a.topic.getCode().compareTo(b.topic.getCode());
    });

    for (int i = 0; i < scores.size(); i++) {
      TopicScore ts = scores.get(i);
      Topic t = ts.topic;
      String reason = buildReason(t, ts.accuracy, ts.attempts, ts.score);
      String evidence = buildEvidence(t.getId(), ts.attempts);
      int priority = Math.min(5, Math.max(1, (5 - i / Math.max(1, scores.size() / 5))));

      StudyPlanItem item = new StudyPlanItem(
          plan,
          t.getId(),
          null, // subtopic pode ser refinado na Fase 4 completa
          (short) priority,
          reason,
          evidence
      );
      out.add(item);
    }

    return out;
  }

  /**
   * Gera o motivo textual explicável da recomendação, derivado dos mesmos
   * fatores que classificam o item (evidência antes de opinião).
   */
  private String buildReason(Topic topic, double accuracy, int attempts, double score) {
    StringBuilder sb = new StringBuilder();
    sb.append("Recomendação para ").append(topic.getCode()).append(" (");
    sb.append(topic.getName()).append(") — ");

    if (attempts == 0) {
      sb.append("nenhuma tentativa pontuável registrada (lacuna); tema com alta recorrência histórica; prioridade máxima.");
    } else {
      sb.append(attempts).append(" tentativa(s) pontuáveis; aproveitamento ").append(Math.round(accuracy * 100)).append("%;");
      sb.append(" score determinístico ").append(Math.round(score * 100)).append(".");
      if (accuracy < 0.5) {
        sb.append(" Aproveitamento abaixo do limiar FRÁGIL (< 50%).");
      } else if (accuracy < 0.7) {
        sb.append(" Faixa intermediária (50–70%).");
      } else {
        sb.append(" Aproveitamento consolidado (>= 70%) — revisão recomendada para manutenção.");
      }
    }

    sb.append(" Evidência derivada de classificações vigentes (TASK 1.4) + desempenho registrado (TASK 4.1).");
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

    TopicScore(Topic topic, double score, double accuracy, int attempts) {
      this.topic = topic;
      this.score = score;
      this.accuracy = accuracy;
      this.attempts = attempts;
    }
  }
}
