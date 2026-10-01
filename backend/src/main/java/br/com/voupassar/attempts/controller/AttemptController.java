package br.com.voupassar.attempts.controller;

import br.com.voupassar.attempts.dto.AttemptResponse;
import br.com.voupassar.attempts.dto.CreateAttemptRequest;
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
 * Tentativas (TASK 3.7) — registro do fato de resposta com correção do servidor,
 * mais ocultação do Modo Prova (TASK 5.3).
 *
 * <p>Rotas autenticadas (Bearer), escopadas ao dono do token. Sem regra de
 * negócio aqui (AGENTS.md §19) — tudo no {@link AttemptService}. O fato é
 * imutável: existe POST (registrar) e GET unitário (consultar); sem PUT nem
 * DELETE. Em {@code PROVA} com execução/sessão {@code IN_PROGRESS} o
 * {@code isCorrect} sai NULL (oculto até encerrar). O histórico paginado
 * continua em {@code GET /profile/history} (TASK 3.6) e os agregados em
 * {@code GET /profile/stats}.
 */
@RestController
@RequestMapping(path = "/api/v1/attempts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Tentativas", description = "Registro de respostas com correção do servidor (questão, resposta, resultado, tempo, modo, vínculo).")
public class AttemptController {

  private final AttemptService service;

  public AttemptController(AttemptService service) {
    this.service = service;
  }

  @Operation(summary = "Registrar tentativa (correção no servidor; oculta em PROVA em andamento).",
      description = "Informa questão + resposta + modo + vínculo (sessão ou "
          + "simulado). O servidor calcula isCorrect/wasAnnulled pelo gabarito "
          + "e carimba answeredAt. Anuladas saem com isCorrect null (pontuação "
          + "DESCONHECIDA). Em PROVA com execução/sessão IN_PROGRESS o isCorrect "
          + "sai null (resultado oculto até concluir/abandonar/encerrar — TASK 5.3).")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Tentativa registrada e corrigida."),
    @ApiResponse(responseCode = "400", description = "Dados inválidos (opção/modo/tempo/vínculo).",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Questão, sessão ou simulado não encontrados.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Sessão/simulado já encerrados.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public AttemptResponse submit(
      @Valid @RequestBody CreateAttemptRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.submitAttempt(requireAuth(principal), req);
  }

  @Operation(summary = "Consultar tentativa do dono do token.",
      description = "Em PROVA com execução/sessão ainda IN_PROGRESS o isCorrect "
          + "sai null (oculto até encerrar — TASK 5.3); após encerrar revela normalmente.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Tentativa."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Tentativa não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/{id}")
  public AttemptResponse get(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getAttempt(requireAuth(principal), id);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
