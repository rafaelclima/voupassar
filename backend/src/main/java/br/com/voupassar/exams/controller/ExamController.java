package br.com.voupassar.exams.controller;

import br.com.voupassar.exams.dto.EditionDetailResponse;
import br.com.voupassar.exams.dto.EditionStatsResponse;
import br.com.voupassar.exams.dto.EditionSummaryResponse;
import br.com.voupassar.exams.dto.ExamDocumentResponse;
import br.com.voupassar.exams.service.ExamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import br.com.voupassar.common.dto.ApiError;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de provas (TASK 3.2) — somente leitura.
 *
 * <p>Rotas autenticadas (secure-by-default até a TASK 3.5 emitir tokens).
 * Nenhum endpoint expõe enunciado, alternativa ou gabarito (TASK 3.4),
 * nem redistribui PDF-fonte: documentos saem como metadados
 * (nome literal + SHA-256 + páginas), nunca como binário.
 */
@RestController
@RequestMapping(path = "/api/v1/editions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Edições", description = "Provas IFRN + EAJ/UFRN: edições, documentos e estatísticas (TASK E.1).")
public class ExamController {

  private final ExamService service;

  public ExamController(ExamService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar edições presentes no banco (ordem: instituição, ano).",
      description =
          "Sem filtro = IFRN + EAJ, cada uma com rótulo institution (compatível com clientes antigos). "
              + "Com ?institution=IFRN|EAJ = só aquele processo.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Lista das edições."),
    @ApiResponse(
        responseCode = "400",
        description = "Processo seletivo inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Sem autenticação.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping
  public List<EditionSummaryResponse> list(
      @Parameter(description = "Filtro por processo: IFRN ou EAJ. Ausente = ambas.", example = "EAJ")
          @RequestParam(required = false) String institution) {
    return service.listEditions(institution);
  }

  @Operation(summary = "Consultar uma edição (versões, documentos, prompt da discursiva).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalhe da edição."),
    @ApiResponse(
        responseCode = "400",
        description = "Ano inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Edição inexistente (ex. 2021, ausente do dataset).",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{year}")
  public EditionDetailResponse get(
      @Parameter(description = "Ano da edição.", example = "2026") @PathVariable int year,
      @Parameter(
              description =
                  "Processo seletivo: IFRN ou EAJ. Ausente = IFRN (compatibilidade); ano sozinho nunca decide (EAJ-2022 ≠ IFRN-2022).",
              example = "EAJ")
          @RequestParam(required = false) String institution) {
    return service.getEdition(year, institution);
  }

  @Operation(summary = "Consultar documentos-fonte de uma edição (metadados, sem binário).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Documentos da edição."),
    @ApiResponse(
        responseCode = "404",
        description = "Edição inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{year}/documents")
  public List<ExamDocumentResponse> documents(
      @Parameter(description = "Ano da edição.", example = "2026") @PathVariable int year,
      @Parameter(description = "Processo seletivo: IFRN ou EAJ. Ausente = IFRN.", example = "EAJ")
          @RequestParam(required = false) String institution) {
    return service.listDocuments(year, institution);
  }

  @Operation(
      summary = "Estatísticas da edição: esperado (capa) × importado (banco).",
      description =
          "Anuladas contam como conteúdo que apareceu na prova; regra de pontuação é DESCONHECIDA.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Estatísticas da edição."),
    @ApiResponse(
        responseCode = "404",
        description = "Edição inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{year}/stats")
  public EditionStatsResponse stats(
      @Parameter(description = "Ano da edição.", example = "2026") @PathVariable int year,
      @Parameter(description = "Processo seletivo: IFRN ou EAJ. Ausente = IFRN.", example = "EAJ")
          @RequestParam(required = false) String institution) {
    return service.getStats(year, institution);
  }
}
