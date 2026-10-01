package br.com.voupassar.questions.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.questions.dto.QuestionResponse;
import br.com.voupassar.questions.service.QuestionService;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de questões (TASK 3.4) — somente leitura, paginada e filtrável — com
 * ocultação do Modo Prova (TASK 5.3).
 *
 * <p>Rotas autenticadas (secure-by-default). Expõe enunciado, alternativas e
 * gabarito com proveniência completa; questão em simulado {@code PROVA} ainda
 * {@code IN_PROGRESS} deste aluno sai com gabarito/explicação ocultos
 * (preserva a sensação de prova — AGENTS.md §9).
 */
@RestController
@RequestMapping(path = "/api/v1/questions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Questões", description = "Banco de questões: listagem paginada, filtros e detalhe.")
public class QuestionController {

  private final QuestionService service;

  public QuestionController(QuestionService service) {
    this.service = service;
  }

  @Operation(
      summary = "Listar questões com filtros e paginação.",
      description =
          "Filtros opcionais por disciplina, assunto, subassunto, edição, dificuldade e origem. "
              + "Ordem fixa (ano-fonte, número, id). Cada item traz notas de evidência "
              + "(anulada, dificuldade estimada, revisão pendente). Questão em simulado "
              + "PROVA em andamento sai com gabarito/explicação ocultos (TASK 5.3).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Página de questões."),
    @ApiResponse(
        responseCode = "400",
        description = "Filtro ou paginação inválidos.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Filtro referencia entidade inexistente (ex. edição 2021).",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping
  public PageResponse<QuestionResponse> search(
      @Parameter(description = "Filtro por disciplina.", example = "MATEMATICA")
          @RequestParam(required = false) String disciplineCode,
      @Parameter(description = "Filtro por assunto (id).", example = "5")
          @RequestParam(required = false) Long topicId,
      @Parameter(description = "Filtro por subassunto (id).", example = "25")
          @RequestParam(required = false) Long subtopicId,
      @Parameter(description = "Filtro por edição-fonte (ano).", example = "2026")
          @RequestParam(required = false) Integer year,
      @Parameter(description = "Filtro por dificuldade estimada.", example = "MEDIA")
          @RequestParam(required = false) String difficulty,
      @Parameter(
              description = "Filtro por origem. OFFICIAL = prova real do IFRN.",
              example = "OFFICIAL")
          @RequestParam(required = false) String sourceType,
      @Parameter(description = "Página 0-based.", example = "0")
          @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Itens por página (1–100).", example = "20")
          @RequestParam(defaultValue = "20") int size,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.searchForUser(
        requireAuth(principal),
        disciplineCode, topicId, subtopicId, year, difficulty, sourceType, page, size);
  }

  @Operation(
      summary = "Consultar uma questão (enunciado, alternativas, gabarito, proveniência).",
      description =
          "Inclui classificação vigente e notas de evidência. Resposta X = anulada "
              + "(contou como conteúdo, sem pontuar). Em simulado PROVA em andamento "
              + "o gabarito/explicação saem ocultos (TASK 5.3).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Detalhe da questão."),
    @ApiResponse(
        responseCode = "400",
        description = "ID inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(
        responseCode = "404",
        description = "Questão inexistente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public QuestionResponse get(
      @Parameter(description = "ID da questão.", example = "1") @PathVariable long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.getByIdForUser(requireAuth(principal), id);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
