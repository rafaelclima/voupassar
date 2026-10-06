package br.com.voupassar.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Observabilidade do banco sem idas ao banco (decisão de produto 2026-10-06).
 *
 * <p>Sem fila de revisão nem carimbo humano: cobre inconsistências e
 * métricas (totais, sem mapas por status). Autorização por papel é coberta
 * no slice web ({@code AdminSecurityTest}).
 */
@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

  @Mock QuestionRepository questions;
  @Mock QuestionClassificationRepository classifications;

  @InjectMocks AdminService service;

  @Test
  void inconsistenciesDelegateToRepositories() {
    when(questions.findIdsWithOptionCountMismatch()).thenReturn(List.of());
    when(questions.findIdsWithAnswerNotInOptions()).thenReturn(List.of(5L));
    when(questions.findIdsWithEmptyStatement()).thenReturn(List.of());
    when(classifications.findIdsWithoutTopic()).thenReturn(List.of());

    var checks = service.inconsistencies();

    assertThat(checks).hasSize(4);
    assertThat(checks.get(1).count()).isEqualTo(1);
    assertThat(checks.get(1).sampleIds()).containsExactly(5L);
    verify(questions).findIdsWithOptionCountMismatch();
    verify(classifications).findIdsWithoutTopic();
  }

  @Test
  void metricsAssemblesTotals() {
    when(questions.count()).thenReturn(240L);
    when(questions.countByAnnulledTrue()).thenReturn(5L);
    when(questions.countByHasFigureTrue()).thenReturn(36L);
    when(classifications.count()).thenReturn(240L);

    var metrics = service.metrics();

    assertThat(metrics.questionsTotal()).isEqualTo(240L);
    assertThat(metrics.questionsAnnulled()).isEqualTo(5L);
    assertThat(metrics.questionsWithFigure()).isEqualTo(36L);
    assertThat(metrics.classificationsTotal()).isEqualTo(240L);
    verify(questions).countByHasFigureTrue();
  }
}
