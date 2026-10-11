package br.com.voupassar.aiprova.controller;

import br.com.voupassar.aiprova.AiSimulationService;
import br.com.voupassar.aiprova.dto.AiAnswerRequest;
import br.com.voupassar.aiprova.dto.AiAnswerResponse;
import br.com.voupassar.aiprova.dto.AiAttemptResponse;
import br.com.voupassar.aiprova.dto.AiFeedbackResponse;
import br.com.voupassar.aiprova.dto.AiGenerateRequest;
import br.com.voupassar.aiprova.dto.AiResultResponse;
import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * Prova-IA experimental (TASK A.3, Fase A: provedor mockado determinístico).
 *
 * <p><b>Questão gerada por IA — não é questão oficial IFRN/EAJ; placar sem
 * validade de prova real; nada-IA alimenta o motor educacional.</b>
 * Rotas autenticadas (Bearer), escopadas ao dono do token. Sem regra de
 * negócio aqui (AGENTS.md §19) — tudo no {@link AiSimulationService}.
 * Respostas-IA escrevem em {@code ai_generation_answers} (fato próprio),
 * nunca em {@code question_attempts}; o caderno-IA é inline (sem {@code
 * GET /questions} para IA).
 */
@RestController
@RequestMapping(path = "/api/v1/simulations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Prova-IA (experimental)",
    description = "TASK A.3: gerar prova inédita com questões criadas por IA no formato de um processo "
        + "existente (IFRN/EAJ), executar no ciclo de simulado sobre o caderno-IA congelado. "
        + "Questão gerada por IA — não é questão oficial; placar experimental sem validade de prova real.")
public class AiSimulationController {

  private final AiSimulationService service;

  public AiSimulationController(AiSimulationService service) {
    this.service = service;
  }

  @Operation(summary = "[Experimental] Gerar e iniciar prova-IA (caderno congelado, IN_PROGRESS).",
      description = "Monta o caderno-IA no formato da edição-modelo (IFRN: 40Q 20LP+20MAT; EAJ-2022/2025: 40Q; "
          + "EAJ-2021: 50Q 15/15/12/8 — estrutura dirigida pelos dados, nunca universal; "
          + "editionModel ausente = mais recente da trilha no banco). O gabarito-IA fica oculto até encerrar. "
          + "Cada questão carrega o selo de experimental.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Prova-IA criada e iniciada."),
    @ApiResponse(responseCode = "400", description = "Processo ou modo inválidos.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Edição-modelo não encontrada (nunca inventada).",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "422", description = "Lote do provedor fora do contrato (sem caderno parcial).",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/ai-generated", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public AiAttemptResponse create(
      @RequestBody AiGenerateRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.createAiSimulation(requireAuth(principal), req);
  }

  @Operation(summary = "[Experimental] Consultar execução-IA (retomar em andamento ou rever encerrada).",
      description = "Enquanto IN_PROGRESS o gabarito-IA fica oculto (só progresso respondida/pendente); "
          + "após encerrar revela o gabarito congelado e o resumo. Caderno-IA inline com selo por questão.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Execução-IA com o caderno."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/ai/attempts/{id}")
  public AiAttemptResponse get(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getAiAttempt(requireAuth(principal), id);
  }

  @Operation(summary = "[Experimental] Responder posição do caderno-IA.",
      description = "Escreve em ai_generation_answers (fato próprio, nunca question_attempts). "
          + "No Modo Estudo revela acerto/erro imediato; no Modo Prova o resultado fica oculto até encerrar.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resposta registrada."),
    @ApiResponse(responseCode = "400", description = "Posição ou alternativa inválidas.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução ou posição não encontradas.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Prova-IA já encerrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/ai/attempts/{id}/answers", consumes = MediaType.APPLICATION_JSON_VALUE)
  public AiAnswerResponse answer(
      @PathVariable long id,
      @RequestBody AiAnswerRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.answerPosition(requireAuth(principal), id, req);
  }

  @Operation(summary = "[Experimental] Concluir prova-IA (IN_PROGRESS → SUBMITTED) com placar do servidor.",
      description = "Vale a última resposta por posição; não respondidas contam como pendentes. "
          + "Placar experimental sobre gabarito gerado por IA — sem validade de prova real.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado com a correção por posição."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Prova-IA já encerrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/ai/attempts/{id}/submit")
  public AiResultResponse submit(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.submitAiAttempt(requireAuth(principal), id);
  }

  @Operation(summary = "[Experimental] Desistir da prova-IA (IN_PROGRESS → ABANDONED) com placar parcial.",
      description = "Não existe pausa no servidor: abandonar é a desistência explícita; "
          + "retomar depois é manter IN_PROGRESS via GET.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado parcial."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Prova-IA já encerrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/ai/attempts/{id}/abandon")
  public AiResultResponse abandon(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.abandonAiAttempt(requireAuth(principal), id);
  }

  @Operation(summary = "[Experimental] Visualizar resultado-IA (só após concluir ou abandonar).",
      description = "Correção por posição contra o gabarito-IA congelado + selo experimental no placar. "
          + "Enquanto IN_PROGRESS retorna 409 SIMULATION_NOT_FINISHED (gabarito oculto).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado com a correção por posição."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Prova-IA ainda em andamento.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/ai/attempts/{id}/result")
  public AiResultResponse result(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getAiResult(requireAuth(principal), id);
  }

  @Operation(summary = "[Experimental] Feedback imediato de uma posição-IA (Modo Estudo).",
      description = "Acerto/erro, resposta do gabarito-IA congelado e assunto rotulado da última resposta. "
          + "No Modo ESTUDO em qualquer status; no Modo PROVA só após encerrar (durante a prova retorna "
          + "409 STUDY_FEEDBACK_UNAVAILABLE). Sem resposta na posição retorna 409 FEEDBACK_NOT_AVAILABLE.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Feedback da posição-IA."),
    @ApiResponse(responseCode = "400", description = "Posição inválida.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução ou posição não encontradas.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Prova em andamento ou posição sem resposta.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/ai/attempts/{id}/feedback/{position}")
  public AiFeedbackResponse feedback(
      @PathVariable long id,
      @PathVariable int position,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getAiFeedback(requireAuth(principal), id, position);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
