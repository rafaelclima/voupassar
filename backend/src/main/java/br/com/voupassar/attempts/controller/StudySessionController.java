package br.com.voupassar.attempts.controller;

import br.com.voupassar.attempts.dto.CreateStudySessionRequest;
import br.com.voupassar.attempts.dto.StudySessionResponse;
import br.com.voupassar.attempts.service.AttemptService;
import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
 * Sessões de estudo (TASK 3.7) — agrupador mínimo de tentativas.
 *
 * <p>Rotas autenticadas (Bearer), escopadas ao dono do token. Sem regra de
 * negócio aqui (AGENTS.md §19) — tudo no {@link AttemptService}. Pausar/
 * retomar e simulados completos ficam para a Fase 5.
 */
@RestController
@RequestMapping(path = "/api/v1/study-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Sessões de estudo", description = "Blocos que agrupam tentativas (Modo Estudo/Prova/Revisão).")
public class StudySessionController {

  private final AttemptService service;

  public StudySessionController(AttemptService service) {
    this.service = service;
  }

  @Operation(summary = "Abrir sessão de estudo (IN_PROGRESS).")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Sessão aberta."),
    @ApiResponse(responseCode = "400", description = "Modo inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public StudySessionResponse create(
      @Valid @RequestBody CreateStudySessionRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.createSession(requireAuth(principal), req);
  }

  @Operation(summary = "Consultar sessão do dono do token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sessão."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public StudySessionResponse get(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getSession(requireAuth(principal), id);
  }

  @Operation(summary = "Encerrar sessão (IN_PROGRESS → FINISHED).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sessão encerrada."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Sessão já encerrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/{id}/finish")
  public StudySessionResponse finish(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.finishSession(requireAuth(principal), id);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
