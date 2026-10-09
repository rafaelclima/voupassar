package br.com.voupassar.exams.service;

import br.com.voupassar.exams.dto.DisciplineStatResponse;
import br.com.voupassar.exams.dto.EditionDetailResponse;
import br.com.voupassar.exams.dto.EditionStatsResponse;
import br.com.voupassar.exams.dto.EditionSummaryResponse;
import br.com.voupassar.exams.dto.EssayPromptResponse;
import br.com.voupassar.exams.dto.ExamDocumentResponse;
import br.com.voupassar.exams.dto.ExamVersionResponse;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Exam;
import br.com.voupassar.exams.entity.ExamDocument;
import br.com.voupassar.exams.entity.ExamVersion;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.ExamDocumentRepository;
import br.com.voupassar.exams.repository.ExamEssayPromptRepository;
import br.com.voupassar.exams.repository.ExamRepository;
import br.com.voupassar.exams.repository.ExamVersionRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consultas de provas (TASK 3.2) — somente leitura.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>2021 não existe no dataset: responde 404 com motivo explícito,
 *       nunca com dados inventados.</li>
 *   <li>{@code scoring_rule} NULL = DESCONHECIDA (nunca default inventado).</li>
 *   <li>Estatísticas distinguem <b>esperado</b> (capa) de <b>importado</b>
 *       (banco via TASK 2.3).</li>
 *   <li>Enunciados/alternativas/gabaritos não saem aqui (TASK 3.4).</li>
 * </ul>
 */
@Service
@Transactional(readOnly = true)
public class ExamService {

  private final ExamRepository exams;
  private final ExamVersionRepository versions;
  private final ExamDocumentRepository documents;
  private final ExamEssayPromptRepository essayPrompts;
  private final DisciplineRepository disciplines;
  private final QuestionRepository questions;

  public ExamService(
      ExamRepository exams,
      ExamVersionRepository versions,
      ExamDocumentRepository documents,
      ExamEssayPromptRepository essayPrompts,
      DisciplineRepository disciplines,
      QuestionRepository questions) {
    this.exams = exams;
    this.versions = versions;
    this.documents = documents;
    this.essayPrompts = essayPrompts;
    this.disciplines = disciplines;
    this.questions = questions;
  }

  /** Lista as edições presentes no banco, em ordem crescente de ano. */
  public List<EditionSummaryResponse> listEditions() {
    List<Exam> all = exams.findAllByOrderByYearAsc();
    List<EditionSummaryResponse> out = new ArrayList<>(all.size());
    for (Exam e : all) {
      out.add(
          new EditionSummaryResponse(
              e.getYear().intValue(),
              e.getInstitution(),
              e.getEdital(),
              e.getDurationMinutes().intValue(),
              e.getObjectiveCount().intValue(),
              e.getLpCount().intValue(),
              e.getMatCount().intValue(),
              e.getCnCount().intValue(),
              e.getChCount().intValue(),
              Boolean.TRUE.equals(e.getHasEssay()),
              e.getScoringRule(),
              versions.countByExamYear(e.getYear()),
              documents.countByExamYear(e.getYear())));
    }
    return out;
  }

  /** Detalhe de uma edição com versões, documentos e prompt da discursiva. */
  public EditionDetailResponse getEdition(int year) {
    Exam exam = requireExam(year);
    Short y = toYear(year);
    List<ExamVersion> vers = versions.findByExamYearOrderByVersionCodeAsc(y);
    List<ExamDocument> docs = documents.findByExamYear(y);

    Map<Long, List<ExamDocumentResponse>> byVersion = new LinkedHashMap<>();
    for (ExamVersion v : vers) {
      byVersion.put(v.getId(), new ArrayList<>());
    }
    for (ExamDocument d : docs) {
      Long vid = d.getVersion() != null ? d.getVersion().getId() : null;
      byVersion.computeIfAbsent(vid, k -> new ArrayList<>()).add(toDocument(d));
    }

    List<ExamVersionResponse> versionDtos = new ArrayList<>(vers.size());
    for (ExamVersion v : vers) {
      versionDtos.add(
          new ExamVersionResponse(
              v.getVersionCode(),
              v.getPublishedAt(),
              v.getNote(),
              List.copyOf(byVersion.getOrDefault(v.getId(), List.of()))));
    }

    EssayPromptResponse essay =
        essayPrompts
            .findByExamYear(y)
            .map(
                p ->
                    new EssayPromptResponse(
                        p.getGenre(),
                        p.getTheme(),
                        p.getPseudonym(),
                        p.getProposalExcerpt(),
                        p.getCriteriaText(),
                        p.getPage().intValue()))
            .orElse(null);

    return new EditionDetailResponse(
        exam.getYear().intValue(),
        exam.getInstitution(),
        exam.getEdital(),
        exam.getDurationMinutes().intValue(),
        exam.getObjectiveCount().intValue(),
        exam.getLpCount().intValue(),
        exam.getMatCount().intValue(),
        exam.getCnCount().intValue(),
        exam.getChCount().intValue(),
        Boolean.TRUE.equals(exam.getHasEssay()),
        exam.getScoringRule(),
        versionDtos,
        essay);
  }

  /** Documentos-fonte de uma edição (nomes literais + SHA-256 auditável). */
  public List<ExamDocumentResponse> listDocuments(int year) {
    requireExam(year);
    return documents.findByExamYear(toYear(year)).stream().map(this::toDocument).toList();
  }

  /** Estatísticas: esperado (capa) × importado (banco). */
  public EditionStatsResponse getStats(int year) {
    Exam exam = requireExam(year);
    Short y = toYear(year);
    long imported = questions.countByExamYear(y);
    long annulled = questions.countByExamYearAndAnnulledTrue(y);
    long confirmed = imported - annulled;

    Map<String, long[]> byDiscipline = new LinkedHashMap<>();
    for (Object[] row : questions.countByDiscipline(y)) {
      String code = (String) row[0];
      long total = ((Number) row[2]).longValue();
      Object ann = row[3];
      long annN = ann == null ? 0L : ((Number) ann).longValue();
      // row[1] = name (resolvido abaixo contra disciplines para consistência)
      byDiscipline.put(code, new long[] {total, annN});
    }

    List<DisciplineStatResponse> perDiscipline = new ArrayList<>();
    for (Discipline d : disciplines.findAllByOrderByCodeAsc()) {
      long[] counts = byDiscipline.getOrDefault(d.getCode(), new long[] {0L, 0L});
      perDiscipline.add(
          new DisciplineStatResponse(
              d.getCode(), d.getName(), expectedFor(d.getCode(), exam), counts[0], counts[1]));
    }

    List<String> notes = new ArrayList<>();
    if (exam.getScoringRule() == null) {
      notes.add("scoring_rule DESCONHECIDA: pontuação de anuladas e da discursiva sem fonte oficial.");
    }
    if (imported == 0) {
      notes.add("Nenhuma questão importada para esta edição (TASK 2.3 ainda não executada neste banco).");
    } else if (imported != exam.getObjectiveCount().intValue()) {
      notes.add(
          "Importado (" + imported + ") difere do esperado na capa (" + exam.getObjectiveCount()
              + "): NECESSITA REVISÃO antes de publicar simulado real.");
    }
    if (annulled > 0) {
      notes.add("Anuladas contam como conteúdo que apareceu na prova; regra de pontuação DESCONHECIDA.");
    }

    return new EditionStatsResponse(
        exam.getYear().intValue(),
        exam.getInstitution(),
        exam.getEdital(),
        exam.getObjectiveCount().intValue(),
        imported,
        confirmed,
        annulled,
        perDiscipline,
        documents.countByExamYear(y),
        versions.countByExamYear(y),
        Boolean.TRUE.equals(exam.getHasEssay()),
        exam.getScoringRule() != null,
        List.copyOf(notes));
  }

  private static Short toYear(int year) {
    return (short) year;
  }

  private Exam requireExam(int year) {
    if (year < 2000 || year > 2100) {
      throw new BadRequestException("Ano de edição inválido: " + year + ".");
    }
    return exams
        .findByYear(toYear(year))
        .orElseThrow(
            () -> {
              if (year == 2021) {
                return new ResourceNotFoundException(
                    "EDITION_NOT_FOUND",
                    "Edição 2021 não encontrada: ausente do dataset inicial (AGENTS.md §3).");
              }
              return new ResourceNotFoundException(
                  "EDITION_NOT_FOUND", "Edição " + year + " não encontrada.");
            });
  }

  private int expectedFor(String disciplineCode, Exam exam) {
    return switch (disciplineCode) {
      case "LINGUA_PORTUGUESA" -> exam.getLpCount().intValue();
      case "MATEMATICA" -> exam.getMatCount().intValue();
      // C.2 semeia CIENCIAS_NATUREZA/CIENCIAS_HUMANAS; até lá caem no default 0
      // (sem disciplinas CN/CH no banco, sem linha esperada — nunca inventar).
      case "CIENCIAS_NATUREZA" -> exam.getCnCount().intValue();
      case "CIENCIAS_HUMANAS" -> exam.getChCount().intValue();
      default -> 0;
    };
  }

  private ExamDocumentResponse toDocument(ExamDocument d) {
    return new ExamDocumentResponse(
        d.getId(),
        d.getKind(),
        d.getFileName(),
        d.getSha256(),
        d.getPages() != null ? d.getPages().intValue() : 0,
        d.getGenerator(),
        d.getNote(),
        d.getVersion() != null ? d.getVersion().getVersionCode() : null);
  }
}
