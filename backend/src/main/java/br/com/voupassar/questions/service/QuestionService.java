package br.com.voupassar.questions.service;

import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.SubtopicRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.entity.QuestionPassage;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.ExamRepository;
import br.com.voupassar.exams.repository.QuestionPassageRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.questions.dto.DisciplineRef;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.questions.dto.PassageResponse;
import br.com.voupassar.questions.dto.QuestionOptionResponse;
import br.com.voupassar.questions.dto.QuestionResponse;
import br.com.voupassar.questions.dto.SubtopicRef;
import br.com.voupassar.questions.dto.TopicRef;
import br.com.voupassar.questions.entity.QuestionOption;
import br.com.voupassar.questions.repository.QuestionOptionRepository;
import br.com.voupassar.simulations.repository.SimulationQuestionRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Banco de questões (TASK 3.4) — somente leitura, paginado e filtrável —
 * mais ocultação do Modo Prova (TASK 5.3).
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Filtros por disciplina/assunto/subassunto/edição inexistentes → 404 com
 *       código explícito (nunca página vazia silenciosa que sugira ausência de
 *       cobrança).</li>
 *   <li>2021 → 404 com motivo explícito (ausente do dataset, AGENTS.md §3).</li>
 *   <li>Anuladas ({@code X}) saem normalmente: contam como conteúdo que
 *       apareceu na prova; regra de pontuação DESCONHECIDA.</li>
 *   <li>Dificuldade é estimativa (confiança BAIXA); explicação NULL = ainda
 *       não redigida; classificação com revisão PENDENTE — tudo sinalizado em
 *       {@code notes}, nunca omitido.</li>
 *   <li>Ordem fixa (ano-fonte, número, id): sem ordenação por relevância sem
 *       algoritmo auditável (Fase 4).</li>
 *   <li>Modo Prova (TASK 5.3, AGENTS.md §9): questão presente em simulado
 *       {@code PROVA} ainda {@code IN_PROGRESS} deste aluno sai com
 *       {@code answerKey=NULL} e {@code explanation=NULL} + nota de gabarito
 *       oculto (via {@code searchForUser}/{@code getByIdForUser}); após
 *       concluir/abandonar revela normalmente.</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class QuestionService {

  /** Teto de página: protege banco e VPS modesta sem paginar item a item. */
  public static final int MAX_SIZE = 100;

  private static final Set<String> DIFFICULTIES = Set.of("FACIL", "MEDIA", "DIFICIL");
  private static final Set<String> SOURCE_TYPES =
      Set.of("OFFICIAL", "AUTHORAL", "ADAPTED", "INTERNAL_REVIEW", "EXPERIMENTAL");

  private final QuestionRepository questions;
  private final QuestionOptionRepository options;
  private final QuestionClassificationRepository classifications;
  private final DisciplineRepository disciplines;
  private final TopicRepository topics;
  private final SubtopicRepository subtopics;
  private final ExamRepository exams;
  private final SimulationQuestionRepository caderno;
  private final QuestionPassageRepository passages;

  public QuestionService(
      QuestionRepository questions,
      QuestionOptionRepository options,
      QuestionClassificationRepository classifications,
      DisciplineRepository disciplines,
      TopicRepository topics,
      SubtopicRepository subtopics,
      ExamRepository exams) {
    this(questions, options, classifications, disciplines, topics, subtopics, exams, null, null);
  }

  public QuestionService(
      QuestionRepository questions,
      QuestionOptionRepository options,
      QuestionClassificationRepository classifications,
      DisciplineRepository disciplines,
      TopicRepository topics,
      SubtopicRepository subtopics,
      ExamRepository exams,
      SimulationQuestionRepository caderno) {
    this(questions, options, classifications, disciplines, topics, subtopics, exams, caderno, null);
  }

  @Autowired
  public QuestionService(
      QuestionRepository questions,
      QuestionOptionRepository options,
      QuestionClassificationRepository classifications,
      DisciplineRepository disciplines,
      TopicRepository topics,
      SubtopicRepository subtopics,
      ExamRepository exams,
      SimulationQuestionRepository caderno,
      QuestionPassageRepository passages) {
    this.questions = questions;
    this.options = options;
    this.classifications = classifications;
    this.disciplines = disciplines;
    this.topics = topics;
    this.subtopics = subtopics;
    this.exams = exams;
    this.caderno = caderno;
    this.passages = passages;
  }

  /**
   * Lista paginada com filtros opcionais ({@code null}/em-branco = sem filtro).
   *
   * @param disciplineCode código da disciplina (ex. {@code MATEMATICA})
   * @param topicId id do assunto
   * @param subtopicId id do subassunto
   * @param year ano da edição-fonte
   * @param difficulty {@code FACIL}, {@code MEDIA} ou {@code DIFICIL}
   * @param sourceType {@code OFFICIAL}, {@code AUTHORAL}, {@code ADAPTED},
   *     {@code INTERNAL_REVIEW} ou {@code EXPERIMENTAL}
   * @param page 0-based
   * @param size itens por página {@code [1, 100]}
   */
  public PageResponse<QuestionResponse> search(
      String disciplineCode,
      Long topicId,
      Long subtopicId,
      Integer year,
      String difficulty,
      String sourceType,
      int page,
      int size) {
    if (page < 0) {
      throw new BadRequestException("Página inválida: " + page + " (0-based).");
    }
    if (size < 1 || size > MAX_SIZE) {
      throw new BadRequestException("Tamanho de página inválido: " + size + " (permitido 1–" + MAX_SIZE + ").");
    }
    String discNorm = requireDisciplineFilter(disciplineCode);
    Long topicNorm = requireTopicFilter(topicId);
    Long subNorm = requireSubtopicFilter(subtopicId);
    Short yearNorm = requireYearFilter(year);
    String diffNorm = normalizeDifficulty(difficulty);
    String srcNorm = normalizeSourceType(sourceType);

    Page<Question> result =
        questions.search(discNorm, topicNorm, subNorm, yearNorm, diffNorm, srcNorm, PageRequest.of(page, size));

    List<Long> ids = result.stream().map(Question::getId).toList();
    Map<Long, List<QuestionOption>> optionsByQuestion = Map.of();
    Map<Long, QuestionClassification> classificationByQuestion = Map.of();
    Map<Long, List<PassageResponse>> passagesByQuestion = Map.of();
    if (!ids.isEmpty()) {
      optionsByQuestion = groupOptions(options.findByQuestionIdsOrdered(ids));
      classificationByQuestion = latestByQuestion(classifications.findActiveByQuestionIds(ids));
      passagesByQuestion = groupPassages(findPassages(ids));
    }

    List<QuestionResponse> content = new ArrayList<>(result.getNumberOfElements());
    for (Question q : result) {
      content.add(
          toResponse(
              q,
              optionsByQuestion.getOrDefault(q.getId(), List.of()),
              classificationByQuestion.get(q.getId()),
              passagesByQuestion.getOrDefault(q.getId(), List.of())));
    }
    return new PageResponse<>(
        List.copyOf(content),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.isFirst(),
        result.isLast());
  }

  /** Detalhe integral de uma questão (enunciado + alternativas + gabarito). */
  public QuestionResponse getById(long id) {
    if (id <= 0) {
      throw new BadRequestException("ID de questão inválido: " + id + ".");
    }
    Question q =
        questions
            .findById(id)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "QUESTION_NOT_FOUND", "Questão " + id + " não encontrada."));
    List<QuestionOption> opts = options.findByQuestionIdOrdered(id);
    List<QuestionClassification> actives =
        classifications.findActiveByQuestionId(id);
    return toResponse(
        q, opts, actives.isEmpty() ? null : actives.get(0),
        groupPassages(findPassages(List.of(id))).getOrDefault(id, List.of()));
  }

  /**
   * Lista paginada com ocultação do Modo Prova (TASK 5.3).
   *
   * <p>Mesmos filtros/paginação de {@link #search}, mas com {@code userId} do
   * dono do token: questões em simulado {@code PROVA} ainda {@code
   * IN_PROGRESS} deste aluno saem com gabarito/explicação ocultos (1 query
   * extra, sem N+1). {@code userId} nulo ou repositório ausente = sem
   * ocultação (nunca inventar prova em andamento sem evidência).
   */
  public PageResponse<QuestionResponse> searchForUser(
      Long userId,
      String disciplineCode,
      Long topicId,
      Long subtopicId,
      Integer year,
      String difficulty,
      String sourceType,
      int page,
      int size) {
    PageResponse<QuestionResponse> result =
        search(disciplineCode, topicId, subtopicId, year, difficulty, sourceType, page, size);
    if (userId == null || caderno == null || result.content().isEmpty()) {
      return result;
    }
    List<Long> ids = result.content().stream().map(QuestionResponse::id).toList();
    Set<Long> hidden;
    try {
      hidden = caderno.findInProvaInProgress(userId, ids);
    } catch (Exception e) {
      hidden = Set.of();
    }
    if (hidden == null || hidden.isEmpty()) {
      return result;
    }
    Set<Long> hiddenIds = new HashSet<>(hidden);
    List<QuestionResponse> masked = new ArrayList<>(result.content().size());
    for (QuestionResponse r : result.content()) {
      masked.add(hiddenIds.contains(r.id()) ? maskForProva(r) : r);
    }
    return new PageResponse<>(
        List.copyOf(masked),
        result.page(), result.size(), result.totalElements(),
        result.totalPages(), result.first(), result.last());
  }

  /**
   * Detalhe com ocultação do Modo Prova (TASK 5.3): se a questão está em
   * simulado {@code PROVA} ainda {@code IN_PROGRESS} deste aluno, o gabarito
   * ({@code answerKey}) e a explicação saem NULL com nota explícita.
   */
  public QuestionResponse getByIdForUser(Long userId, long id) {
    QuestionResponse full = getById(id);
    if (userId == null || caderno == null) {
      return full;
    }
    boolean hidden;
    try {
      hidden = caderno.existsInProvaInProgress(userId, id);
    } catch (Exception e) {
      hidden = false;
    }
    return hidden ? maskForProva(full) : full;
  }

  /**
   * Máscara do Modo Prova: preserva enunciado/alternativas/proveniência, mas
   * oculta {@code answerKey} e {@code explanation} (nunca inventar gabarito).
   * {@code annulled} segue verídico (anulada não tem resposta correta a
   * vazar; além disso anuladas ficam fora da seleção da TASK 5.1).
   * Textos-base ({@code passages}) e figuras seguem visíveis: são parte do
   * enunciado, não do gabarito (AGENTS.md §9 + TASK 6.9).
   */
  private static QuestionResponse maskForProva(QuestionResponse r) {
    List<String> notes = new ArrayList<>(r.notes().size() + 1);
    notes.addAll(r.notes());
    notes.add("Gabarito oculto durante a execução no Modo Prova "
        + "(questão em simulado PROVA em andamento): conclua ou abandone para ver a correção.");
    return new QuestionResponse(
        r.id(), r.sourceType(), r.examYear(), r.questionNumber(),
        r.discipline(), r.statement(), r.options(),
        null, r.annulled(), r.difficultyEstimate(), null,
        r.pageStart(), r.pageEnd(), r.hasFigure(),
        r.topic(), r.subtopic(),
        r.classificationConfidence(), r.taxonomyVersion(), r.classificationStatus(),
        r.validationStatus(), r.publicationStatus(),
        List.copyOf(notes),
        r.figures() != null ? r.figures() : java.util.List.of(),
        r.passages() != null ? r.passages() : java.util.List.of());
  }

  private String requireDisciplineFilter(String code) {
    if (code == null || code.isBlank()) {
      return null;
    }
    String normalized = code.trim().toUpperCase();
    if (!normalized.matches("[A-Z_]{1,64}")) {
      throw new BadRequestException("Código de disciplina inválido: " + code + ".");
    }
    disciplines
        .findByCode(normalized)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "DISCIPLINE_NOT_FOUND", "Disciplina " + normalized + " não encontrada."));
    return normalized;
  }

  private Long requireTopicFilter(Long topicId) {
    if (topicId == null) {
      return null;
    }
    if (topicId <= 0) {
      throw new BadRequestException("ID de assunto inválido: " + topicId + ".");
    }
    topics
        .findByIdWithDiscipline(topicId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "TOPIC_NOT_FOUND", "Assunto " + topicId + " não encontrado."));
    return topicId;
  }

  private Long requireSubtopicFilter(Long subtopicId) {
    if (subtopicId == null) {
      return null;
    }
    if (subtopicId <= 0) {
      throw new BadRequestException("ID de subassunto inválido: " + subtopicId + ".");
    }
    subtopics
        .findByIdWithTopic(subtopicId)
        .orElseThrow(
            () ->
                new ResourceNotFoundException(
                    "SUBTOPIC_NOT_FOUND", "Subassunto " + subtopicId + " não encontrado."));
    return subtopicId;
  }

  private Short requireYearFilter(Integer year) {
    if (year == null) {
      return null;
    }
    if (year < 2000 || year > 2100) {
      throw new BadRequestException("Ano de edição inválido: " + year + ".");
    }
    Short y = year.shortValue();
    if (exams.findByYear(y).isEmpty()) {
      if (year == 2021) {
        throw new ResourceNotFoundException(
            "EDITION_NOT_FOUND",
            "Edição 2021 não encontrada: ausente do dataset inicial (AGENTS.md §3).");
      }
      throw new ResourceNotFoundException("EDITION_NOT_FOUND", "Edição " + year + " não encontrada.");
    }
    return y;
  }

  private static String normalizeDifficulty(String difficulty) {
    if (difficulty == null || difficulty.isBlank()) {
      return null;
    }
    String normalized = difficulty.trim().toUpperCase();
    if (!DIFFICULTIES.contains(normalized)) {
      throw new BadRequestException(
          "Dificuldade inválida: " + difficulty + " (permitido FACIL, MEDIA, DIFICIL).");
    }
    return normalized;
  }

  private static String normalizeSourceType(String sourceType) {
    if (sourceType == null || sourceType.isBlank()) {
      return null;
    }
    String normalized = sourceType.trim().toUpperCase();
    if (!SOURCE_TYPES.contains(normalized)) {
      throw new BadRequestException(
          "Origem inválida: "
              + sourceType
              + " (permitido OFFICIAL, AUTHORAL, ADAPTED, INTERNAL_REVIEW, EXPERIMENTAL).");
    }
    return normalized;
  }

  private static Map<Long, List<QuestionOption>> groupOptions(List<QuestionOption> all) {
    Map<Long, List<QuestionOption>> out = new LinkedHashMap<>();
    for (QuestionOption o : all) {
      out.computeIfAbsent(o.getQuestion().getId(), k -> new ArrayList<>()).add(o);
    }
    return out;
  }

  private static Map<Long, QuestionClassification> latestByQuestion(
      List<QuestionClassification> all) {
    // findActiveByQuestionIds já ordena (questão ASC, id DESC): a primeira de
    // cada questão é a mais recente.
    Map<Long, QuestionClassification> out = new LinkedHashMap<>();
    for (QuestionClassification c : all) {
      out.putIfAbsent(c.getQuestion().getId(), c);
    }
    return out;
  }

  /** Busca vínculos de texto-base sem N+1 (1 query por página/detalhe). */
  private List<QuestionPassage> findPassages(List<Long> ids) {
    if (passages == null || ids == null || ids.isEmpty()) {
      return List.of();
    }
    try {
      return passages.findByQuestionIdsOrdered(ids);
    } catch (Exception e) {
      return List.of();
    }
  }

  private static Map<Long, List<PassageResponse>> groupPassages(List<QuestionPassage> all) {
    // findByQuestionIdsOrdered já ordena (questão ASC, position ASC).
    Map<Long, List<PassageResponse>> out = new LinkedHashMap<>();
    for (QuestionPassage qp : all) {
      out.computeIfAbsent(qp.getQuestionId(), k -> new ArrayList<>()).add(toPassage(qp));
    }
    return out;
  }

  private static PassageResponse toPassage(QuestionPassage qp) {
    var p = qp.getPassage();
    return new PassageResponse(
        p.getLabel(),
        p.getKind(),
        p.getTitle(),
        p.getByline(),
        p.getSubtitle(),
        p.getIntro(),
        p.getContent(),
        p.getVisualDescription(),
        p.getFormatNote(),
        p.getSourceNote(),
        p.getPageStart() == null ? null : p.getPageStart().intValue(),
        p.getPageEnd() == null ? null : p.getPageEnd().intValue());
  }

  private static QuestionResponse toResponse(
      Question q,
      List<QuestionOption> opts,
      QuestionClassification classification,
      List<PassageResponse> questionPassages) {
    Discipline d = q.getDiscipline();
    List<QuestionOptionResponse> optionDtos = new ArrayList<>(opts.size());
    for (QuestionOption o : opts) {
      optionDtos.add(new QuestionOptionResponse(o.getLabel(), o.getOptionText()));
    }

    Topic topic = classification != null ? classification.getTopic() : null;
    Subtopic subtopic = classification != null ? classification.getSubtopic() : null;
    TopicRef topicRef =
        topic == null ? null : new TopicRef(topic.getId(), topic.getCode(), topic.getName());
    SubtopicRef subtopicRef =
        subtopic == null
            ? null
            : new SubtopicRef(subtopic.getId(), subtopic.getCode(), subtopic.getName());

    List<String> notes = new ArrayList<>();
    if (q.isAnnulled()) {
      notes.add(
          "Anulada (X no gabarito): contou como conteúdo que apareceu na prova, sem pontuar; regra de pontuação DESCONHECIDA.");
    }
    if (q.getDifficultyEstimate() != null) {
      notes.add(
          "Dificuldade "
              + q.getDifficultyEstimate()
              + " estimada (confiança BAIXA, sem calibração — Fase 4).");
    } else {
      notes.add("Dificuldade NÃO CONFIRMADA.");
    }
    if (q.getExplanation() == null) {
      notes.add("Sem explicação redigida (NECESSITA REVISÃO).");
    }
    if (classification == null) {
      notes.add("Sem classificação pedagógica: assunto NÃO CONFIRMADO.");
    } else if ("PENDING".equals(classification.getStatus())) {
      notes.add(
          "Classificação pedagógica "
              + classification.getTaxonomyVersion()
              + " com revisão humana PENDENTE (TASK 12.2).");
    }
    if ("PENDENTE_REVISAO".equals(q.getPublicationStatus())) {
      notes.add("Publicação PENDENTE_REVISAO (curadoria TASK 12.2).");
    }
    if (q.isHasFigure()) {
      notes.add("Enunciado com figura no PDF-fonte (ver páginas).");
    }
    if ("OBJECTIVE".equals(q.getKind()) && opts.size() != 4) {
      notes.add(
          "Divergência: esperadas 4 alternativas, encontradas "
              + opts.size()
              + " (NECESSITA REVISÃO).");
    }

    return new QuestionResponse(
        q.getId(),
        q.getSourceType(),
        q.getSourceYear() == null ? null : q.getSourceYear().intValue(),
        q.getSourceQuestionNumber() == null ? null : q.getSourceQuestionNumber().intValue(),
        new DisciplineRef(d.getCode(), d.getName()),
        q.getStatement(),
        List.copyOf(optionDtos),
        q.getAnswerKey(),
        q.isAnnulled(),
        q.getDifficultyEstimate(),
        q.getExplanation(),
        q.getPageStart() == null ? null : q.getPageStart().intValue(),
        q.getPageEnd() == null ? null : q.getPageEnd().intValue(),
        q.isHasFigure(),
        topicRef,
        subtopicRef,
        classification == null ? null : classification.getConfidence(),
        classification == null ? null : classification.getTaxonomyVersion(),
        classification == null ? null : classification.getStatus(),
        q.getValidationStatus(),
        q.getPublicationStatus(),
        List.copyOf(notes),
        java.util.List.of(), // figuras: vazio até sync_figures.py preencher
        List.copyOf(questionPassages == null ? List.of() : questionPassages));
  }
}
