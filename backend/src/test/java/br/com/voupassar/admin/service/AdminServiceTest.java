package br.com.voupassar.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.admin.dto.ReviewQueueItemResponse;
import br.com.voupassar.admin.dto.UpdateClassificationRequest;
import br.com.voupassar.admin.dto.UpdateQuestionStatusRequest;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.questions.dto.PageResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/**
 * Regras de curadoria sem banco (TASK 12.1).
 *
 * <p>Cobre fila, transições proibidas (regressão de APPROVED, PUBLICAVEL sem
 * APPROVED), 404 e o recibo de revisão. Autorização por papel é coberta no
 * slice web ({@code AdminSecurityTest}).
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock QuestionRepository questions;
  @Mock QuestionClassificationRepository classifications;

  @InjectMocks AdminService service;

  private static Question question(String validation, String publication) {
    // Leniente de propósito: cada teste usa só parte dos getters.
    Question q = org.mockito.Mockito.mock(Question.class);
    org.mockito.Mockito.lenient().when(q.getId()).thenReturn(1L);
    org.mockito.Mockito.lenient().when(q.getSourceType()).thenReturn("OFFICIAL");
    org.mockito.Mockito.lenient().when(q.getSourceYear()).thenReturn((short) 2026);
    org.mockito.Mockito.lenient().when(q.getSourceQuestionNumber()).thenReturn((short) 21);
    Discipline d = org.mockito.Mockito.mock(Discipline.class);
    org.mockito.Mockito.lenient().when(d.getCode()).thenReturn("MATEMATICA");
    org.mockito.Mockito.lenient().when(d.getName()).thenReturn("Matemática");
    org.mockito.Mockito.lenient().when(q.getDiscipline()).thenReturn(d);
    org.mockito.Mockito.lenient().when(q.getAnswerKey()).thenReturn("A");
    org.mockito.Mockito.lenient().when(q.isAnnulled()).thenReturn(false);
    org.mockito.Mockito.lenient().when(q.isHasFigure()).thenReturn(false);
    org.mockito.Mockito.lenient().when(q.getValidationStatus()).thenReturn(validation);
    org.mockito.Mockito.lenient().when(q.getPublicationStatus()).thenReturn(publication);
    return q;
  }

  @Test
  void reviewQueueDefaultsToPendingAndMapsActiveClassification() {
    Question q = question("PENDING", "PENDENTE_REVISAO");
    when(questions.findByValidationStatus(eq("PENDING"), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(q)));
    QuestionClassification c = org.mockito.Mockito.mock(QuestionClassification.class);
    Topic t = org.mockito.Mockito.mock(Topic.class);
    when(t.getCode()).thenReturn("ARITMETICA");
    Subtopic s = org.mockito.Mockito.mock(Subtopic.class);
    when(s.getCode()).thenReturn("SISTEMAS_NUMERACAO");
    when(c.getId()).thenReturn(10L);
    when(c.getQuestion()).thenReturn(q);
    when(c.getTopic()).thenReturn(t);
    when(c.getSubtopic()).thenReturn(s);
    when(c.getConfidence()).thenReturn("ALTA");
    when(c.getStatus()).thenReturn("PENDING");
    when(classifications.findActiveByQuestionIds(List.of(1L))).thenReturn(List.of(c));

    PageResponse<ReviewQueueItemResponse> page = service.reviewQueue(null, 0, 20);

    assertThat(page.totalElements()).isEqualTo(1);
    ReviewQueueItemResponse item = page.content().get(0);
    assertThat(item.topicCode()).isEqualTo("ARITMETICA");
    assertThat(item.classificationId()).isEqualTo(10L);
  }

  @Test
  void reviewQueueRejectsUnknownStatus() {
    assertThatThrownBy(() -> service.reviewQueue("LIXO", 0, 20))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void updateQuestionRequiresAtLeastOneField() {
    assertThatThrownBy(
            () -> service.updateQuestionStatus(1L, new UpdateQuestionStatusRequest(null, null), 7L))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void publicavelRequiresApproved() {
    Question reviewed = question("REVIEWED", "PENDENTE_REVISAO");
    when(questions.findById(1L)).thenReturn(Optional.of(reviewed));

    assertThatThrownBy(
            () -> service.updateQuestionStatus(
                1L, new UpdateQuestionStatusRequest(null, "PUBLICAVEL"), 7L))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("APPROVED");
  }

  @Test
  void approvedCannotRegressToPending() {
    Question approved = question("APPROVED", "SOMENTE_REFERENCIA");
    when(questions.findById(1L)).thenReturn(Optional.of(approved));

    assertThatThrownBy(
            () -> service.updateQuestionStatus(
                1L, new UpdateQuestionStatusRequest("PENDING", null), 7L))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void updateQuestionHappyPathWritesAndReturnsItem() {
    Question q = question("PENDING", "PENDENTE_REVISAO");
    when(questions.findById(1L)).thenReturn(Optional.of(q));
    when(questions.updateStatuses(1L, "REVIEWED", "PENDENTE_REVISAO")).thenReturn(1);
    when(classifications.findActiveByQuestionId(1L)).thenReturn(List.of());

    ReviewQueueItemResponse item =
        service.updateQuestionStatus(1L, new UpdateQuestionStatusRequest("REVIEWED", null), 7L);

    verify(questions).updateStatuses(1L, "REVIEWED", "PENDENTE_REVISAO");
    assertThat(item.validationStatus()).isEqualTo("PENDING"); // projeção imutável de leitura
  }

  @Test
  void updateQuestionPropagatesNotFound() {
    when(questions.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () -> service.updateQuestionStatus(
                99L, new UpdateQuestionStatusRequest("REVIEWED", null), 7L))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void approvedClassificationCanOnlyBeRejected() {
    QuestionClassification c = org.mockito.Mockito.mock(QuestionClassification.class);
    when(c.getStatus()).thenReturn("APPROVED");
    when(classifications.findById(10L)).thenReturn(Optional.of(c));

    assertThatThrownBy(
            () -> service.reviewClassification(10L, new UpdateClassificationRequest("REVIEWED", null), 7L))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  void reviewClassificationHappyPathStampsReviewer() {
    Question q = question("PENDING", "PENDENTE_REVISAO");
    QuestionClassification c = org.mockito.Mockito.mock(QuestionClassification.class);
    when(c.getStatus()).thenReturn("PENDING");
    when(c.getQuestion()).thenReturn(q);
    when(classifications.findById(10L)).thenReturn(Optional.of(c));
    when(classifications.review(eq(10L), eq("APPROVED"), any(), eq(7L))).thenReturn(1);

    var receipt = service.reviewClassification(
        10L, new UpdateClassificationRequest("APPROVED", "Confere com o caderno."), 7L);

    verify(classifications).review(10L, "APPROVED", "Confere com o caderno.", 7L);
    assertThat(receipt.reviewedBy()).isEqualTo(7L);
    assertThat(receipt.status()).isEqualTo("APPROVED");
  }

  @Test
  void inconsistenciesAndMetricsDelegateToRepositories() {
    when(questions.findIdsWithOptionCountMismatch()).thenReturn(List.of());
    when(questions.findIdsWithAnswerNotInOptions()).thenReturn(List.of(5L));
    when(questions.findIdsWithEmptyStatement()).thenReturn(List.of());
    when(classifications.findIdsWithoutTopic()).thenReturn(List.of());

    var checks = service.inconsistencies();

    assertThat(checks).hasSize(4);
    assertThat(checks.get(1).count()).isEqualTo(1);
    assertThat(checks.get(1).sampleIds()).containsExactly(5L);
    verify(questions).findIdsWithOptionCountMismatch();
  }

  @Test
  void metricsAssemblesDistributions() {
    when(questions.count()).thenReturn(240L);
    when(questions.countByValidationStatus(anyString())).thenReturn(240L, 0L, 0L, 0L);
    when(questions.countByPublicationStatus(anyString())).thenReturn(0L, 0L, 240L, 0L);
    when(questions.countByAnnulledTrue()).thenReturn(5L);
    when(questions.countByHasFigureTrue()).thenReturn(36L);
    when(classifications.count()).thenReturn(240L);
    when(classifications.countByStatus(anyString())).thenReturn(240L, 0L, 0L, 0L);

    var metrics = service.metrics();

    assertThat(metrics.questionsTotal()).isEqualTo(240L);
    assertThat(metrics.questionsByValidation()).containsEntry("PENDING", 240L);
    assertThat(metrics.questionsAnnulled()).isEqualTo(5L);
    assertThat(metrics.classificationsByStatus()).containsEntry("PENDING", 240L);
    verify(questions).countByHasFigureTrue();
    // sanity: 4 status × chamadas de contagem
    verify(questions, org.mockito.Mockito.times(4)).countByValidationStatus(anyString());
    verify(classifications, org.mockito.Mockito.times(4)).countByStatus(anyString());
  }
}
