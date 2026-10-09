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
    return listDisciplines(null);
  }

  /**
   * Disciplinas com contagem de questões do processo (TASK E.2).
   *
   * @param institution {@code IFRN} ou {@code EAJ}; {@code null}/em-branco =
   *     panorama global legado (ambos). Nº de assuntos é da taxonomia
   *     compartilhada; nº de questões é factual daquele processo.
   */
  public List<DisciplineSummaryResponse> listDisciplines(String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    List<DisciplineSummaryResponse> out = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      long questionCount = instNorm == null
          ? questions.countByDisciplineCode(d.getCode())
          : questions.countByInstitutionAndDisciplineCode(instNorm, d.getCode());
      out.add(
          new DisciplineSummaryResponse(
              d.getCode(),
              d.getName(),
              topics.countByDisciplineCode(d.getCode()),
              questionCount));
    }
    return out;
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

  /** Detalhe de uma disciplina com seus assuntos. */
  public DisciplineDetailResponse getDiscipline(String code) {
    return getDiscipline(code, null);
  }

  /**
   * Detalhe da disciplina na trilha do processo (TASK E.2): contagens de
   * assuntos/questões daquele {@code institution} (global legado quando
   * ausente).
   */
  public DisciplineDetailResponse getDiscipline(String code, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    Discipline d = requireDiscipline(code);
    Map<Long, Long> topicCounts = instNorm == null ? topicCountMap()
        : topicCountMapForInstitution(instNorm);
    List<TopicSummaryResponse> topicDtos = new ArrayList<>();
    for (Topic t : topics.findByDisciplineCodeOrdered(d.getCode())) {
      topicDtos.add(toTopicSummary(t, topicCounts));
    }
    long questionCount = instNorm == null
        ? questions.countByDisciplineCode(d.getCode())
        : questions.countByInstitutionAndDisciplineCode(instNorm, d.getCode());
    return new DisciplineDetailResponse(
        d.getCode(),
        d.getName(),
        topicDtos.size(),
        questionCount,
        List.copyOf(topicDtos));
  }

  /**
   * Lista assuntos; com {@code disciplineCode} filtra por disciplina.
   *
   * @param disciplineCode opcional (ex. {@code MATEMATICA}); inexistente → 404
   */
  public List<TopicSummaryResponse> listTopics(String disciplineCode) {
    return listTopics(disciplineCode, null);
  }

  /**
   * Assuntos com frequência do processo (TASK E.2): {@code institution}
   * ausente = global legado; informado = só aquele processo (D.2-EAJ na
   * trilha EAJ, sem contaminar o IFRN).
   */
  public List<TopicSummaryResponse> listTopics(String disciplineCode, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    List<Topic> all;
    if (disciplineCode == null || disciplineCode.isBlank()) {
      all = topics.findAllOrdered();
    } else {
      Discipline d = requireDiscipline(disciplineCode);
      all = topics.findByDisciplineCodeOrdered(d.getCode());
    }
    Map<Long, Long> counts = instNorm == null ? topicCountMap()
        : topicCountMapForInstitution(instNorm);
    List<TopicSummaryResponse> out = new ArrayList<>(all.size());
    for (Topic t : all) {
      out.add(toTopicSummary(t, counts));
    }
    return out;
  }

  /** Detalhe de um assunto com série histórica por edição. */
  public TopicDetailResponse getTopic(long id) {
    return getTopic(id, null);
  }

  /**
   * Detalhe do assunto na trilha do processo (TASK E.2): série e confiança
   * daquele {@code institution} (anos da trilha; global legado quando
   * ausente).
   */
  public TopicDetailResponse getTopic(long id, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    Topic t = requireTopic(id);
    long questionCount = instNorm == null
        ? classifications.countByTopicId(t.getId())
        : classifications.countByTopicIdForInstitution(t.getId(), instNorm);
    List<Short> editionYears = instNorm == null
        ? classifications.editionsByTopic(t.getId())
        : classifications.editionsByTopicForInstitution(t.getId(), instNorm);
    List<Integer> editions = editionYears.stream().map(Short::intValue).toList();

    List<EditionCountResponse> perEdition = new ArrayList<>();
    List<Object[]> rows = instNorm == null
        ? classifications.countTopicByYear(t.getId())
        : classifications.countTopicByYearForInstitution(t.getId(), instNorm);
    for (Object[] row : rows) {
      perEdition.add(
          new EditionCountResponse(
              ((Number) row[0]).intValue(), toLong(row[1]), toLong(row[2])));
    }

    Map<Long, Long> subCounts = instNorm == null ? subtopicCountMap()
        : subtopicCountMapForInstitution(instNorm);
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
        instNorm == null ? toConfidence(classifications.confidenceByTopic(t.getId()))
            : toConfidence(classifications.confidenceByTopicForInstitution(t.getId(), instNorm)),
        List.copyOf(subDtos));
  }

  /**
   * Lista subassuntos; com {@code topicId} filtra por assunto.
   *
   * @param topicId opcional; inexistente → 404
   */
  public List<SubtopicSummaryResponse> listSubtopics(Long topicId) {
    return listSubtopics(topicId, null);
  }

  /**
   * Subassuntos com frequência do processo (TASK E.2): {@code institution}
   * ausente = global legado; informado = só aquele processo.
   */
  public List<SubtopicSummaryResponse> listSubtopics(Long topicId, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    List<Subtopic> all;
    if (topicId == null) {
      all = subtopics.findAllOrdered();
    } else {
      requireTopic(topicId);
      all = subtopics.findByTopicIdOrdered(topicId);
    }
    Map<Long, Long> counts = instNorm == null ? subtopicCountMap()
        : subtopicCountMapForInstitution(instNorm);
    List<SubtopicSummaryResponse> out = new ArrayList<>(all.size());
    for (Subtopic s : all) {
      out.add(toSubtopicSummary(s, counts));
    }
    return out;
  }

  /** Detalhe de um subassunto com série histórica por edição. */
  public SubtopicDetailResponse getSubtopic(long id) {
    return getSubtopic(id, null);
  }

  /**
   * Detalhe do subassunto na trilha do processo (TASK E.2).
   */
  public SubtopicDetailResponse getSubtopic(long id, String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    Subtopic s = requireSubtopic(id);
    long questionCount = instNorm == null
        ? classifications.countBySubtopicId(s.getId())
        : classifications.countBySubtopicIdForInstitution(s.getId(), instNorm);
    List<Integer> editions = (instNorm == null
        ? classifications.editionsBySubtopic(s.getId())
        : classifications.editionsBySubtopicForInstitution(s.getId(), instNorm))
        .stream().map(Short::intValue).toList();

    List<EditionCountResponse> perEdition = new ArrayList<>();
    List<Object[]> rows = instNorm == null
        ? classifications.countSubtopicByYear(s.getId())
        : classifications.countSubtopicByYearForInstitution(s.getId(), instNorm);
    for (Object[] row : rows) {
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
        instNorm == null ? toConfidence(classifications.confidenceBySubtopic(s.getId()))
            : toConfidence(classifications.confidenceBySubtopicForInstitution(s.getId(), instNorm)));
  }

  /** Panorama histórico global (frequências derivadas do banco). */
  public ContentStatsResponse getContentStats() {
    return getContentStats(null);
  }

  /**
   * Panorama histórico da trilha do processo (TASK E.2).
   *
   * @param institution {@code IFRN} ou {@code EAJ}; {@code null}/em-branco =
   *     panorama global legado (ambos). Na trilha EAJ, totais e frequências
   *     vêm de D.1/D.2-EAJ (130 classificações, 3 edições); na IFRN, do banco
   *     IFRN (6 edições) — sem contaminação cruzada.
   */
  public ContentStatsResponse getContentStats(String institution) {
    String instNorm = normalizeInstitutionFilter(institution);
    long totalQuestions = instNorm == null ? questions.count()
        : questions.countByExamInstitution(instNorm);
    long totalClassified = instNorm == null ? classifications.countClassified()
        : classifications.countClassifiedForInstitution(instNorm);
    List<String> versions = classifications.distinctTaxonomyVersions();

    Map<Long, long[]> topicStats = new HashMap<>();
    List<Object[]> statRows = instNorm == null ? classifications.statsByTopic()
        : classifications.statsByTopicForInstitution(instNorm);
    for (Object[] row : statRows) {
      topicStats.put(((Number) row[0]).longValue(), new long[] {toLong(row[1]), toLong(row[2])});
    }

    List<ContentDisciplineStatsResponse> perDiscipline = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      long qCount = instNorm == null ? questions.countByDisciplineCode(d.getCode())
          : questions.countByInstitutionAndDisciplineCode(instNorm, d.getCode());
      perDiscipline.add(
          new ContentDisciplineStatsResponse(
              d.getCode(),
              d.getName(),
              topics.countByDisciplineCode(d.getCode()),
              qCount));
    }

    List<ContentTopicStatsResponse> perTopic = new ArrayList<>();
    for (Topic t : topics.findAllOrdered()) {
      long[] st = topicStats.getOrDefault(t.getId(), new long[] {0L, 0L});
      double percent =
          totalClassified == 0 ? 0.0 : Math.round((st[0] * 1000.0 / totalClassified)) / 10.0;
      int editionsCount = instNorm == null ? classifications.editionsByTopic(t.getId()).size()
          : classifications.editionsByTopicForInstitution(t.getId(), instNorm).size();
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
      notes.add(instNorm == null
          ? "Nenhuma questão importada para este banco (TASK 2.3 ainda não executada)."
          : "Nenhuma questão importada para a trilha " + instNorm + " neste banco.");
    }
    if (totalClassified == 0) {
      notes.add("Nenhuma classificação no banco — estatísticas históricas indisponíveis.");
    }
    notes.add("Anuladas contam como conteúdo que apareceu na prova; regra de pontuação DESCONHECIDA.");
    if (instNorm == null) {
      notes.add("Tendência não calculada (6 edições, sem teste estatístico; oscilações de 1–2 questões são ruído).");
    } else if ("EAJ".equals(instNorm)) {
      notes.add("Trilha EAJ: 3 edições (2021, 2022, 2025; D.2 — sem interpolar edições inexistentes); tendência marcada DESCRITIVA.");
    } else {
      notes.add("Trilha IFRN: 6 edições (2020, 2022–2026; 2021 ausente, sem interpolação); tendência não calculada.");
    }

    return new ContentStatsResponse(
        totalQuestions,
        totalClassified,
        List.copyOf(versions),
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

  private Map<Long, Long> topicCountMapForInstitution(String institution) {
    Map<Long, Long> out = new HashMap<>();
    for (Object[] row : classifications.countByTopicForInstitution(institution)) {
      out.put(((Number) row[0]).longValue(), toLong(row[1]));
    }
    return out;
  }

  private Map<Long, Long> subtopicCountMapForInstitution(String institution) {
    Map<Long, Long> out = new HashMap<>();
    for (Object[] row : classifications.countBySubtopicForInstitution(institution)) {
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
