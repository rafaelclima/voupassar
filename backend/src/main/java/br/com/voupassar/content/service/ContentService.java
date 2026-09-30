package br.com.voupassar.content.service;

import br.com.voupassar.content.dto.ConfidenceBreakdownResponse;
import br.com.voupassar.content.dto.ContentDisciplineStatsResponse;
import br.com.voupassar.content.dto.ContentStatsResponse;
import br.com.voupassar.content.dto.ContentTopicStatsResponse;
import br.com.voupassar.content.dto.DisciplineDetailResponse;
import br.com.voupassar.content.dto.DisciplineSummaryResponse;
import br.com.voupassar.content.dto.EditionCountResponse;
import br.com.voupassar.content.dto.SubtopicDetailResponse;
import br.com.voupassar.content.dto.SubtopicSummaryResponse;
import br.com.voupassar.content.dto.TopicDetailResponse;
import br.com.voupassar.content.dto.TopicSummaryResponse;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.SubtopicRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas de disciplinas e conteúdos (TASK 3.3) — somente leitura.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Disciplinas/tópicos/subtópicos vêm do seed V2 (taxonomia v1.1
 *       observada nas provas, {@code docs/content-map.md}) — nada inventado.</li>
 *   <li>Contagens de disciplinas usam {@code questions.discipline_id}
 *       (factual); contagens de assuntos/subassuntos usam
 *       {@code question_classifications} não-rejeitadas (derivado).</li>
 *   <li>Anuladas ({@code X}) contam como conteúdo que apareceu na prova;
 *       regra de pontuação DESCONHECIDA — aqui só contamos.</li>
 *   <li>Classificações têm revisão humana PENDENTE (TASK 12.2): frequências
 *       são derivadas, nunca verdade oficial do IFRN.</li>
 *   <li>Enunciados, alternativas e gabaritos não saem aqui (TASK 3.4).</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ContentService {

  private final DisciplineRepository disciplines;
  private final TopicRepository topics;
  private final SubtopicRepository subtopics;
  private final QuestionRepository questions;
  private final QuestionClassificationRepository classifications;

  public ContentService(
      DisciplineRepository disciplines,
      TopicRepository topics,
      SubtopicRepository subtopics,
      QuestionRepository questions,
      QuestionClassificationRepository classifications) {
    this.disciplines = disciplines;
    this.topics = topics;
    this.subtopics = subtopics;
    this.questions = questions;
    this.classifications = classifications;
  }

  /** Lista as disciplinas observadas nas provas, em ordem de código. */
  public List<DisciplineSummaryResponse> listDisciplines() {
    List<DisciplineSummaryResponse> out = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      out.add(
          new DisciplineSummaryResponse(
              d.getCode(),
              d.getName(),
              topics.countByDisciplineCode(d.getCode()),
              questions.countByDisciplineCode(d.getCode())));
    }
    return out;
  }

  /** Detalhe de uma disciplina com seus assuntos. */
  public DisciplineDetailResponse getDiscipline(String code) {
    Discipline d = requireDiscipline(code);
    Map<Long, Long> topicCounts = topicCountMap();
    List<TopicSummaryResponse> topicDtos = new ArrayList<>();
    for (Topic t : topics.findByDisciplineCodeOrdered(d.getCode())) {
      topicDtos.add(toTopicSummary(t, topicCounts));
    }
    return new DisciplineDetailResponse(
        d.getCode(),
        d.getName(),
        topicDtos.size(),
        questions.countByDisciplineCode(d.getCode()),
        List.copyOf(topicDtos));
  }

  /**
   * Lista assuntos; com {@code disciplineCode} filtra por disciplina.
   *
   * @param disciplineCode opcional (ex. {@code MATEMATICA}); inexistente → 404
   */
  public List<TopicSummaryResponse> listTopics(String disciplineCode) {
    List<Topic> all;
    if (disciplineCode == null || disciplineCode.isBlank()) {
      all = topics.findAllOrdered();
    } else {
      Discipline d = requireDiscipline(disciplineCode);
      all = topics.findByDisciplineCodeOrdered(d.getCode());
    }
    Map<Long, Long> counts = topicCountMap();
    List<TopicSummaryResponse> out = new ArrayList<>(all.size());
    for (Topic t : all) {
      out.add(toTopicSummary(t, counts));
    }
    return out;
  }

  /** Detalhe de um assunto com série histórica por edição. */
  public TopicDetailResponse getTopic(long id) {
    Topic t = requireTopic(id);
    long questionCount = classifications.countByTopicId(t.getId());
    List<Short> editionYears = classifications.editionsByTopic(t.getId());
    List<Integer> editions = editionYears.stream().map(Short::intValue).toList();

    List<EditionCountResponse> perEdition = new ArrayList<>();
    for (Object[] row : classifications.countTopicByYear(t.getId())) {
      perEdition.add(
          new EditionCountResponse(
              ((Number) row[0]).intValue(), toLong(row[1]), toLong(row[2])));
    }

    Map<Long, Long> subCounts = subtopicCountMap();
    List<SubtopicSummaryResponse> subDtos = new ArrayList<>();
    for (Subtopic s : subtopics.findByTopicIdOrdered(t.getId())) {
      subDtos.add(toSubtopicSummary(s, subCounts));
    }

    return new TopicDetailResponse(
        t.getId(),
        t.getCode(),
        t.getName(),
        t.getDiscipline().getCode(),
        t.getDiscipline().getName(),
        t.isActive(),
        questionCount,
        editions,
        List.copyOf(perEdition),
        toConfidence(classifications.confidenceByTopic(t.getId())),
        List.copyOf(subDtos));
  }

  /**
   * Lista subassuntos; com {@code topicId} filtra por assunto.
   *
   * @param topicId opcional; inexistente → 404
   */
  public List<SubtopicSummaryResponse> listSubtopics(Long topicId) {
    List<Subtopic> all;
    if (topicId == null) {
      all = subtopics.findAllOrdered();
    } else {
      requireTopic(topicId);
      all = subtopics.findByTopicIdOrdered(topicId);
    }
    Map<Long, Long> counts = subtopicCountMap();
    List<SubtopicSummaryResponse> out = new ArrayList<>(all.size());
    for (Subtopic s : all) {
      out.add(toSubtopicSummary(s, counts));
    }
    return out;
  }

  /** Detalhe de um subassunto com série histórica por edição. */
  public SubtopicDetailResponse getSubtopic(long id) {
    Subtopic s = requireSubtopic(id);
    long questionCount = classifications.countBySubtopicId(s.getId());
    List<Integer> editions =
        classifications.editionsBySubtopic(s.getId()).stream().map(Short::intValue).toList();

    List<EditionCountResponse> perEdition = new ArrayList<>();
    for (Object[] row : classifications.countSubtopicByYear(s.getId())) {
      perEdition.add(
          new EditionCountResponse(
              ((Number) row[0]).intValue(), toLong(row[1]), toLong(row[2])));
    }

    return new SubtopicDetailResponse(
        s.getId(),
        s.getCode(),
        s.getName(),
        s.getTopic().getId(),
        s.getTopic().getCode(),
        s.getTopic().getName(),
        s.getTopic().getDiscipline().getCode(),
        s.getTopic().getDiscipline().getName(),
        s.isActive(),
        questionCount,
        editions,
        List.copyOf(perEdition),
        toConfidence(classifications.confidenceBySubtopic(s.getId())));
  }

  /** Panorama histórico global (frequências derivadas, revisão pendente). */
  public ContentStatsResponse getContentStats() {
    long totalQuestions = questions.count();
    long totalClassified = classifications.countClassified();
    List<String> versions = classifications.distinctTaxonomyVersions();
    boolean reviewPending = classifications.existsByStatus("PENDING");

    Map<Long, long[]> topicStats = new HashMap<>();
    for (Object[] row : classifications.statsByTopic()) {
      topicStats.put(((Number) row[0]).longValue(), new long[] {toLong(row[1]), toLong(row[2])});
    }

    List<ContentDisciplineStatsResponse> perDiscipline = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      perDiscipline.add(
          new ContentDisciplineStatsResponse(
              d.getCode(),
              d.getName(),
              topics.countByDisciplineCode(d.getCode()),
              questions.countByDisciplineCode(d.getCode())));
    }

    List<ContentTopicStatsResponse> perTopic = new ArrayList<>();
    for (Topic t : topics.findAllOrdered()) {
      long[] st = topicStats.getOrDefault(t.getId(), new long[] {0L, 0L});
      double percent =
          totalClassified == 0 ? 0.0 : Math.round((st[0] * 1000.0 / totalClassified)) / 10.0;
      int editionsCount = classifications.editionsByTopic(t.getId()).size();
      perTopic.add(
          new ContentTopicStatsResponse(
              t.getId(),
              t.getCode(),
              t.getName(),
              t.getDiscipline().getCode(),
              t.getDiscipline().getName(),
              st[0],
              percent,
              editionsCount,
              st[1]));
    }

    List<String> notes = new ArrayList<>();
    if (totalQuestions == 0) {
      notes.add("Nenhuma questão importada para este banco (TASK 2.3 ainda não executada).");
    }
    if (totalClassified == 0) {
      notes.add("Nenhuma classificação no banco — estatísticas históricas indisponíveis.");
    }
    if (reviewPending) {
      notes.add(
          "Classificações com revisão humana PENDENTE (TASK 12.2): frequências são derivadas, não verdade oficial do IFRN.");
    }
    notes.add("Anuladas contam como conteúdo que apareceu na prova; regra de pontuação DESCONHECIDA.");
    notes.add("Tendência não calculada (6 edições, sem teste estatístico; oscilações de 1–2 questões são ruído).");

    return new ContentStatsResponse(
        totalQuestions,
        totalClassified,
        List.copyOf(versions),
        reviewPending,
        List.copyOf(perDiscipline),
        List.copyOf(perTopic),
        List.copyOf(notes));
  }

  private Discipline requireDiscipline(String code) {
    if (code == null || code.isBlank()) {
      throw new BadRequestException("Código de disciplina inválido.");
    }
    String normalized = code.trim().toUpperCase();
    if (!normalized.matches("[A-Z_]{1,64}")) {
      throw new BadRequestException("Código de disciplina inválido: " + code + ".");
    }
    return disciplines
        .findByCode(normalized)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "DISCIPLINE_NOT_FOUND", "Disciplina " + normalized + " não encontrada."));
  }

  private Topic requireTopic(long id) {
    if (id <= 0) {
      throw new BadRequestException("ID de assunto inválido: " + id + ".");
    }
    return topics
        .findByIdWithDiscipline(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "TOPIC_NOT_FOUND", "Assunto " + id + " não encontrado."));
  }

  private Subtopic requireSubtopic(long id) {
    if (id <= 0) {
      throw new BadRequestException("ID de subassunto inválido: " + id + ".");
    }
    return subtopics
        .findByIdWithTopic(id)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "SUBTOPIC_NOT_FOUND", "Subassunto " + id + " não encontrado."));
  }

  private Map<Long, Long> topicCountMap() {
    Map<Long, Long> out = new HashMap<>();
    for (Object[] row : classifications.countByTopic()) {
      out.put(((Number) row[0]).longValue(), toLong(row[1]));
    }
    return out;
  }

  private Map<Long, Long> subtopicCountMap() {
    Map<Long, Long> out = new HashMap<>();
    for (Object[] row : classifications.countBySubtopic()) {
      out.put(((Number) row[0]).longValue(), toLong(row[1]));
    }
    return out;
  }

  private TopicSummaryResponse toTopicSummary(Topic t, Map<Long, Long> counts) {
    return new TopicSummaryResponse(
        t.getId(),
        t.getCode(),
        t.getName(),
        t.getDiscipline().getCode(),
        t.getDiscipline().getName(),
        t.isActive(),
        counts.getOrDefault(t.getId(), 0L),
        subtopics.countByTopicId(t.getId()));
  }

  private SubtopicSummaryResponse toSubtopicSummary(Subtopic s, Map<Long, Long> counts) {
    return new SubtopicSummaryResponse(
        s.getId(),
        s.getCode(),
        s.getName(),
        s.getTopic().getId(),
        s.getTopic().getCode(),
        s.getTopic().getName(),
        s.getTopic().getDiscipline().getCode(),
        s.getTopic().getDiscipline().getName(),
        s.isActive(),
        counts.getOrDefault(s.getId(), 0L));
  }

  private static ConfidenceBreakdownResponse toConfidence(List<Object[]> rows) {
    long alta = 0;
    long media = 0;
    long baixa = 0;
    for (Object[] row : rows) {
      long n = toLong(row[1]);
      switch (String.valueOf(row[0])) {
        case "ALTA" -> alta = n;
        case "MEDIA" -> media = n;
        default -> baixa += n;
      }
    }
    return new ConfidenceBreakdownResponse(alta, media, baixa);
  }

  private static long toLong(Object v) {
    return v == null ? 0L : ((Number) v).longValue();
  }
}
