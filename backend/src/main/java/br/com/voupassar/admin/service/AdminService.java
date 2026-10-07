package br.com.voupassar.admin.service;

import br.com.voupassar.admin.dto.AdminMetricsResponse;
import br.com.voupassar.admin.dto.DiagnosticsResponse;
import br.com.voupassar.admin.dto.InconsistencyResponse;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import java.lang.management.ManagementFactory;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Casos de uso da área administrativa.
 *
 * <p>Somente gestão/observabilidade (o controller exige {@code CURATOR} ou
 * {@code ADMIN}): inconsistências vivas e métricas do banco de questões.
 * Desde a decisão de produto 2026-10-06 não há fila de revisão nem carimbo
 * humano — a confiança vem do veredito do pipeline (checksum estável +
 * vínculo ao gabarito + páginas válidas). Nenhuma escrita por aqui.
 */
@Service
public class AdminService {

  private static final int SAMPLE_LIMIT = 20;

  private final QuestionRepository questions;
  private final QuestionClassificationRepository classifications;
  private final JdbcTemplate jdbc;
  private final TechMetrics metrics;

  @Value("${app.version:DESCONHECIDA}")
  private String version;

  public AdminService(
      QuestionRepository questions,
      QuestionClassificationRepository classifications,
      JdbcTemplate jdbc,
      TechMetrics metrics) {
    this.questions = questions;
    this.classifications = classifications;
    this.jdbc = jdbc;
    this.metrics = metrics;
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

  /** Fotografia do banco para o gestor (sem PII, sem conteúdo). */
  @Transactional(readOnly = true)
  public AdminMetricsResponse metrics() {
    return new AdminMetricsResponse(
        questions.count(),
        questions.countByAnnulledTrue(),
        questions.countByHasFigureTrue(),
        classifications.count());
  }

  /**
   * Diagnóstico técnico (TASK 22.2): serviço, versão, banco, migração, uptime
   * e contadores. Sem PII. O banco é checado com `SELECT 1`; a migração é o
   * maior `version` da `flyway_schema_history` (DESCONHECIDA se ilegível).
   */
  public DiagnosticsResponse diagnostics() {
    String dbStatus;
    try {
      Integer one = jdbc.queryForObject("SELECT 1", Integer.class);
      dbStatus = (one != null && one == 1) ? "UP" : "DOWN";
    } catch (Exception e) {
      dbStatus = "DOWN";
    }
    String lastMigration;
    try {
      lastMigration = jdbc.queryForObject(
          "SELECT max(version) FROM flyway_schema_history", String.class);
      if (lastMigration == null) {
        lastMigration = "DESCONHECIDA";
      }
    } catch (Exception e) {
      lastMigration = "DESCONHECIDA";
    }
    return new DiagnosticsResponse(
        "voupassar-backend",
        version != null ? version : "DESCONHECIDA",
        dbStatus,
        lastMigration,
        ManagementFactory.getRuntimeMXBean().getUptime(),
        metrics.snapshot());
  }

  private static InconsistencyResponse sample(String check, String description, List<Long> ids) {
    List<Long> sample = ids.size() > SAMPLE_LIMIT ? ids.subList(0, SAMPLE_LIMIT) : List.copyOf(ids);
    return new InconsistencyResponse(check, description, ids.size(), sample);
  }
}
