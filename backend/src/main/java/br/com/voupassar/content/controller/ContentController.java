package br.com.voupassar.content.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.content.dto.ContentStatsResponse;
import br.com.voupassar.content.dto.DisciplineDetailResponse;
import br.com.voupassar.content.dto.DisciplineSummaryResponse;
import br.com.voupassar.content.dto.SubtopicDetailResponse;
import br.com.voupassar.content.dto.SubtopicSummaryResponse;
import br.com.voupassar.content.dto.TopicDetailResponse;
import br.com.voupassar.content.dto.TopicSummaryResponse;
import br.com.voupassar.content.service.ContentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de disciplinas e conteúdos (TASK 3.3) — somente leitura.
 *
 * <p>Rotas autenticadas (secure-by-default até a TASK 3.5 emitir tokens).
 * Taxonomia v1.1 observada nas provas (seed V2); frequências históricas
 * derivadas das classificações no banco (revisão humana PENDENTE) — nunca
 * verdade oficial do IFRN. Enunciados, alternativas e gabaritos ficam para
 * a TASK 3.4.
 */
@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Conteúdos", description = "Disciplinas, assuntos, subassuntos e estatísticas históricas.")
public class ContentController {

  private final ContentService service;

  public ContentController(ContentService service) {
    this.service = service;
  }

  @Operation(summary = "Listar disciplinas observadas nas provas.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Disciplinas."),
    @ApiResponse(
        responseCode = "401",
        description = "Sem autenticação.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/disciplines")
  public List<DisciplineSummaryResponse> listDisciplines(
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.listDisciplines(institution);
  }

  @Operation(summary = "Consultar uma disciplina com seus assuntos.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalhe da disciplina."),
    @ApiResponse(
        responseCode = "404",
        description = "Disciplina inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/disciplines/{code}")
  public DisciplineDetailResponse getDiscipline(
      @Parameter(description = "Código da disciplina.", example = "MATEMATICA")
          @PathVariable String code,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.getDiscipline(code, institution);
  }

  @Operation(
      summary = "Listar assuntos (opcionalmente por disciplina).",
      description = "Sem filtro retorna todos; com disciplineCode filtra (inexistente → 404).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Assuntos."),
    @ApiResponse(
        responseCode = "404",
        description = "Disciplina do filtro inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/topics")
  public List<TopicSummaryResponse> listTopics(
      @Parameter(description = "Filtro por disciplina.", example = "MATEMATICA")
          @RequestParam(required = false) String disciplineCode,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.listTopics(disciplineCode, institution);
  }

  @Operation(summary = "Consultar um assunto com série histórica por edição.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalhe do assunto."),
    @ApiResponse(
        responseCode = "404",
        description = "Assunto inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/topics/{id}")
  public TopicDetailResponse getTopic(
      @Parameter(description = "ID do assunto.", example = "5") @PathVariable long id,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.getTopic(id, institution);
  }

  @Operation(
      summary = "Listar subassuntos (opcionalmente por assunto).",
      description = "Sem filtro retorna todos; com topicId filtra (inexistente → 404).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Subassuntos."),
    @ApiResponse(
        responseCode = "404",
        description = "Assunto do filtro inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/subtopics")
  public List<SubtopicSummaryResponse> listSubtopics(
      @Parameter(description = "Filtro por assunto.", example = "5")
          @RequestParam(required = false) Long topicId,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.listSubtopics(topicId, institution);
  }

  @Operation(summary = "Consultar um subassunto com série histórica por edição.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalhe do subassunto."),
    @ApiResponse(
        responseCode = "404",
        description = "Subassunto inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/subtopics/{id}")
  public SubtopicDetailResponse getSubtopic(
      @Parameter(description = "ID do subassunto.", example = "25") @PathVariable long id,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.getSubtopic(id, institution);
  }

  @Operation(
      summary = "Panorama histórico dos conteúdos.",
      description =
          "Frequências derivadas das classificações no banco (revisão PENDENTE); "
              + "anuladas contam como conteúdo, sem pontuar; tendência não calculada.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Panorama histórico."),
    @ApiResponse(
        responseCode = "401",
        description = "Sem autenticação.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/content/stats")
  public ContentStatsResponse contentStats(
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution) {
    return service.getContentStats(institution);
  }
}
