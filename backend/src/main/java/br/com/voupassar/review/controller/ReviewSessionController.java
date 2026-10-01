package br.com.voupassar.review.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.review.dto.CreateReviewSessionRequest;
import br.com.voupassar.review.dto.ReviewSessionResponse;
import br.com.voupassar.review.dto.ReviewSessionResultResponse;
import br.com.voupassar.review.service.ReviewSessionService;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Modo Revisão — experiência dedicada (TASK 5.4).
 *
 * <p>Cria, retoma e conclui sessões de revisão sobre o caderno congelado do
 * top-N da fila (TASK 4.5). Revisão não gera simulado (ERD §2.6): aqui nasce
 * {@code study_sessions} com {@code mode=REVISAO}. As respostas continuam
 * via {@code POST /attempts} com {@code studySessionId} e
 * {@code mode=REVISAO} (TASK 3.7, feedback imediato — REVISAO nunca oculta
 * resultado); o encerramento usa {@code POST /study-sessions/{id}/finish}.
 */
@RestController
@RequestMapping(path = "/api/v1/review/sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Modo Revisão",
    description = "Sessão de revisão dedicada: caderno congelado do top-N da fila, progresso com feedback imediato e resultado ao encerrar (TASK 5.4).")
public class ReviewSessionController {

  private final ReviewSessionService service;

  public ReviewSessionController(ReviewSessionService service) {
    this.service = service;
  }

  @Operation(summary = "Criar e iniciar sessão de revisão (caderno congelado, IN_PROGRESS).",
      description = "Congela o top-N da fila de revisão (ordem determinística da TASK 4.5) "
          + "como caderno desta sessão. Fila vazia retorna 400 NO_REVIEW_ITEMS; "
          + "filtros inválidos herdam 404 da fila.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Sessão de revisão criada e iniciada."),
    @ApiResponse(responseCode = "400", description = "Filtros malformados ou fila vazia.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Disciplina ou assunto não encontrados.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ReviewSessionResponse create(
      @Valid @RequestBody(required = false) CreateReviewSessionRequest req,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.create(requireAuth(principal),
        req == null ? new CreateReviewSessionRequest(null, null, null, false) : req);
  }

  @Operation(summary = "Retomar sessão de revisão (caderno + progresso com feedback imediato).",
      description = "Posições respondidas nesta sessão mostram selectedOption/isCorrect "
          + "sempre revelados (REVISAO nunca oculta); pendentes mostram como responder.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sessão com o caderno e o progresso."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public ReviewSessionResponse get(
      @PathVariable long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.get(requireAuth(principal), id);
  }

  @Operation(summary = "Visualizar resultado da revisão (só após encerrar a sessão).",
      description = "Correção por posição a partir da última tentativa REVISAO por questão. "
          + "Enquanto IN_PROGRESS retorna 409 REVIEW_NOT_FINISHED.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado com a correção por questão."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Sessão ainda em andamento.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}/result")
  public ReviewSessionResultResponse result(
      @PathVariable long id,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.getResult(requireAuth(principal), id);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
