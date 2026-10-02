package br.com.voupassar.admin.service;

import br.com.voupassar.admin.dto.AdminMetricsResponse;
import br.com.voupassar.admin.dto.ClassificationReviewResponse;
import br.com.voupassar.admin.dto.InconsistencyResponse;
import br.com.voupassar.admin.dto.ReviewQueueItemResponse;
import br.com.voupassar.admin.dto.UpdateClassificationRequest;
import br.com.voupassar.admin.dto.UpdateQuestionStatusRequest;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.questions.dto.PageResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso da área administrativa (TASK 12.1).
 *
 * <p>Somente curadoria/gestão (o controller exige {@code CURATOR} ou
 * {@code ADMIN}). Leituras reaproveitam as projeções imutáveis existentes;
 * escritas usam UPDATEs dirigidos e auditáveis (o DDL registra
 * {@code updated_at}; a revisão de classificação carimba
 * {@code reviewed_by/at}). Nenhuma deleção, nenhuma criação de questão por
 * aqui — curadoria revisa, não inventa conteúdo (AGENTS.md §4).
 */
@Service
public class AdminService {

  private static final Set<String> QUESTION_VALIDATION =
      Set.of("PENDING", "REVIEWED", "APPROVED", "REJECTED");
  private static final Set<String> QUESTION_PUBLICATION =
      Set.of("PUBLICAVEL", "NAO_PUBLICAVEL", "PENDENTE_REVISAO", "SOMENTE_REFERENCIA");
  private static final Set<String> CLASSIFICATION_REVIEW = Set.of("REVIEWED", "APPROVED", "REJECTED");
  private static final Set<String> CLASSIFICATION_STATUSES =
      Set.of("PENDING", "REVIEWED", "APPROVED", "REJECTED");

  private static final int SAMPLE_LIMIT = 20;

  private final QuestionRepository questions;
  private final QuestionClassificationRepository classifications;

  public AdminService(
      QuestionRepository questions, QuestionClassificationRepository classifications) {
    this.questions = questions;
    this.classifications = classifications;
  }

  /**
   * Fila de revisão: questões por status de validação (padrão PENDING) e,
   * opcionalmente, por status da classificação ativa.
   *
   * <p>Ordem fixa (ano-fonte, número, id) — a mesma da API pública.
   */
  @Transactional(readOnly = true)
  public PageResponse<ReviewQueueItemResponse> reviewQueue(
      String validationStatus, String classificationStatus, int page, int size) {
    String status = validationStatus == null ? "PENDING" : validationStatus.trim().toUpperCase();
    if (!QUESTION_VALIDATION.contains(status)) {
      throw new BadRequestException("validationStatus deve ser PENDING, REVIEWED, APPROVED ou REJECTED.");
    }
    String classification =
        classificationStatus == null || classificationStatus.isBlank()
            ? null
            : classificationStatus.trim().toUpperCase();
    if (classification != null && !CLASSIFICATION_STATUSES.contains(classification)) {
      throw new BadRequestException(
          "classificationStatus deve ser PENDING, REVIEWED, APPROVED ou REJECTED.");
    }
    Page<Question> result =
        questions.findByValidationStatus(
            status,
            classification,
            PageRequest.of(page, size, Sort.by("sourceYear").ascending()
                .and(Sort.by("sourceQuestionNumber").ascending())
                .and(Sort.by("id").ascending())));
    List<Long> ids = result.getContent().stream().map(Question::getId).toList();
    Map<Long, QuestionClassification> active = activeByQuestion(ids);
    List<ReviewQueueItemResponse> items =
        result.getContent().stream().map(q -> toItem(q, active.get(q.getId()))).toList();
    return new PageResponse<>(
        items,
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.isFirst(),
        result.isLast());
  }

  /** Atualiza os status de curadoria de uma questão (CURATOR+). */
  @Transactional
  public ReviewQueueItemResponse updateQuestionStatus(
      long id, UpdateQuestionStatusRequest req, long reviewerId) {
    if (req.validationStatus() == null && req.publicationStatus() == null) {
      throw new BadRequestException("Informe ao menos validationStatus ou publicationStatus.");
    }
    Question current = questions
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Questão inexistente."));
    String validation = req.validationStatus() == null
        ? current.getValidationStatus()
        : req.validationStatus().trim().toUpperCase();
    String publication = req.publicationStatus() == null
        ? current.getPublicationStatus()
        : req.publicationStatus().trim().toUpperCase();
    if (!QUESTION_VALIDATION.contains(validation) || !QUESTION_PUBLICATION.contains(publication)) {
      throw new BadRequestException("Status inválido.");
    }
    if (current.getValidationStatus().equals("APPROVED") && !validation.equals("APPROVED")
        && !validation.equals("REJECTED")) {
      throw new BadRequestException(
          "Questão APPROVED só pode sair desse estado para REJECTED (sem regressão a PENDING/REVIEWED).");
    }
    if (publication.equals("PUBLICAVEL") && !validation.equals("APPROVED")) {
      throw new BadRequestException("PUBLICAVEL exige questão APPROVED (nunca publicar sem revisão).");
    }
    int updated = questions.updateStatuses(id, validation, publication);
    if (updated == 0) {
      throw new ResourceNotFoundException("Questão inexistente.");
    }
    Question reloaded = questions.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Questão inexistente."));
    QuestionClassification active = firstActive(reloaded.getId());
    return toItem(reloaded, active);
  }

  /** Revisão humana de uma classificação (CURATOR+), carimbando revisor. */
  @Transactional
  public ClassificationReviewResponse reviewClassification(
      long id, UpdateClassificationRequest req, long reviewerId) {
    String status = req.status().trim().toUpperCase();
    if (!CLASSIFICATION_REVIEW.contains(status)) {
      throw new BadRequestException("status deve ser REVIEWED, APPROVED ou REJECTED.");
    }
    QuestionClassification current = classifications
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Classificação inexistente."));
    if (current.getStatus().equals("APPROVED") && !status.equals("REJECTED")) {
      throw new BadRequestException(
          "Classificação APPROVED só pode sair desse estado para REJECTED (revogação).");
    }
    String observation = req.observation() == null ? null : req.observation().trim();
    if (observation != null && observation.isEmpty()) {
      observation = null;
    }
    int updated = classifications.review(id, status, observation, reviewerId);
    if (updated == 0) {
      throw new ResourceNotFoundException("Classificação inexistente.");
    }
    return new ClassificationReviewResponse(
        id, current.getQuestion().getId(), status, observation, reviewerId);
  }

  /** Checagens vivas de inconsistência (contagem + amostra de ids). */
  @Transactional(readOnly = true)
  public List<InconsistencyResponse> inconsistencies() {
    return List.of(
        sample(
            "OPTIONS_COUNT",
            "Objetivas com número de alternativas diferente de 4.",
            questions.findIdsWithOptionCountMismatch()),
        sample(
            "ANSWER_NOT_IN_OPTIONS",
            "Não-anuladas cuja resposta não está entre as alternativas.",
            questions.findIdsWithAnswerNotInOptions()),
        sample(
            "EMPTY_STATEMENT",
            "Enunciados vazios ou só-espaço.",
            questions.findIdsWithEmptyStatement()),
        sample(
            "CLASSIFICATION_WITHOUT_TOPIC",
            "Classificações ativas sem tópico.",
            classifications.findIdsWithoutTopic()));
  }

  /** Fotografia do banco para o gestor/curador (sem PII, sem conteúdo). */
  @Transactional(readOnly = true)
  public AdminMetricsResponse metrics() {
    Map<String, Long> byValidation = new LinkedHashMap<>();
    for (String s : List.of("PENDING", "REVIEWED", "APPROVED", "REJECTED")) {
      byValidation.put(s, questions.countByValidationStatus(s));
    }
    Map<String, Long> byPublication = new LinkedHashMap<>();
    for (String s : List.of("PUBLICAVEL", "NAO_PUBLICAVEL", "PENDENTE_REVISAO", "SOMENTE_REFERENCIA")) {
      byPublication.put(s, questions.countByPublicationStatus(s));
    }
    Map<String, Long> byClassification = new LinkedHashMap<>();
    for (String s : List.of("PENDING", "REVIEWED", "APPROVED", "REJECTED")) {
      byClassification.put(s, classifications.countByStatus(s));
    }
    return new AdminMetricsResponse(
        questions.count(),
        byValidation,
        byPublication,
        questions.countByAnnulledTrue(),
        questions.countByHasFigureTrue(),
        classifications.count(),
        byClassification);
  }

  /**
   * Classificação de triagem: a mais recente da questão, <b>inclusive
   * REJECTED</b>. A fila do curador precisa mostrar o que foi rejeitado e
   * por quê; usar só as não-rejeitadas faria o item parecer sem
   * classificação (falsa inconsistência na tela). Só a ausência real de
   * qualquer classificação é reportada como inconsistência.
   */
  private Map<Long, QuestionClassification> activeByQuestion(List<Long> ids) {
    Map<Long, QuestionClassification> map = new LinkedHashMap<>();
    if (ids.isEmpty()) {
      return map;
    }
    for (QuestionClassification c : classifications.findLatestByQuestionIds(ids)) {
      map.putIfAbsent(c.getQuestion().getId(), c);
    }
    return map;
  }

  private QuestionClassification firstActive(long questionId) {
    List<QuestionClassification> latest =
        classifications.findLatestByQuestionIds(List.of(questionId));
    return latest.isEmpty() ? null : latest.get(0);
  }

  private static ReviewQueueItemResponse toItem(Question q, QuestionClassification c) {
    return new ReviewQueueItemResponse(
        q.getId(),
        q.getSourceType(),
        q.getSourceYear(),
        q.getSourceQuestionNumber(),
        q.getDiscipline().getCode(),
        q.getDiscipline().getName(),
        q.getAnswerKey(),
        q.isAnnulled(),
        q.isHasFigure(),
        q.getValidationStatus(),
        q.getPublicationStatus(),
        c == null ? null : c.getId(),
        c == null || c.getTopic() == null ? null : c.getTopic().getCode(),
        c == null || c.getSubtopic() == null ? null : c.getSubtopic().getCode(),
        c == null ? null : c.getConfidence(),
        c == null ? null : c.getStatus(),
        c == null ? null : c.getObservation(),
        c == null ? null : c.getEvidence());
  }

  private static InconsistencyResponse sample(String check, String description, List<Long> ids) {
    List<Long> sample = ids.size() > SAMPLE_LIMIT ? ids.subList(0, SAMPLE_LIMIT) : List.copyOf(ids);
    return new InconsistencyResponse(check, description, ids.size(), sample);
  }
}
