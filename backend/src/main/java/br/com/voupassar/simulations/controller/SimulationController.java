package br.com.voupassar.simulations.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.security.UserPrincipal;
import br.com.voupassar.simulations.dto.CreateDisciplineSimulationRequest;
import br.com.voupassar.simulations.dto.CreateEditionSimulationRequest;
import br.com.voupassar.simulations.dto.StudyFeedbackResponse;
import br.com.voupassar.simulations.dto.SimulationAttemptResponse;
import br.com.voupassar.simulations.dto.SimulationAttemptSummary;
import br.com.voupassar.simulations.dto.SimulationResultResponse;
import br.com.voupassar.simulations.service.SimulationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simulados por disciplina (TASK 5.1), simulado real por edição (TASK 5.5) e
 * feedback do Modo Estudo (TASK 5.2).
 *
 * <p>Rotas autenticadas (Bearer), escopadas ao dono do token. Sem regra de
 * negócio aqui (AGENTS.md §19) — tudo no {@link SimulationService}. As
 * respostas do caderno continuam via {@code POST /attempts} com {@code
 * simulationAttemptId} (TASK 3.7); o placar é calculado no servidor ao
 * encerrar.
 */
@RestController
@RequestMapping(path = "/api/v1/simulations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Simulados",
    description = "Simulado por disciplina (TASK 5.1), simulado real por edição (TASK 5.5), com feedback imediato do Modo Estudo por posição (TASK 5.2).")
public class SimulationController {

  private final SimulationService service;

  public SimulationController(SimulationService service) {
    this.service = service;
  }

  @Operation(summary = "Criar e iniciar simulado por disciplina (caderno congelado, IN_PROGRESS).",
      description = "Amostra aleatória sem reposição sobre as questões não-anuladas da disciplina "
          + "(com filtro opcional de dificuldade). Quantidade acima do disponível retorna 400 "
          + "INSUFFICIENT_QUESTIONS com o disponível explícito. O gabarito fica oculto até encerrar.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Simulado criado e iniciado."),
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou questões insuficientes.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Disciplina não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/by-discipline", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public SimulationAttemptResponse createByDiscipline(
      @Valid @RequestBody CreateDisciplineSimulationRequest req,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.createByDiscipline(requireAuth(principal), req, institution);
  }

  @Operation(summary = "Criar e iniciar simulado real por edição (caderno integral em ordem original, IN_PROGRESS).",
      description = "Reproduz a estrutura da edição (institution,year) selecionada (configuração da própria edição, nunca regra universal). "
          + "Posições 1..N seguem a numeração original, incluindo anuladas (fora do aproveitamento). "
          + "institution ausente = IFRN (compatibilidade); EAJ-2021 = 50Q 15/15/12/8, EAJ-2022/2025 = 40Q 20/20. "
          + "Edição inexistente retorna 404 EDITION_NOT_FOUND (IFRN 2021 ausente; EAJ só 2021/2022/2025); "
          + "banco divergente da capa retorna 409 INCOMPLETE_EDITION.")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Simulado real criado e iniciado."),
    @ApiResponse(responseCode = "400", description = "Dados inválidos.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Edição não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Edição com banco incompleto/divergente.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/by-edition", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public SimulationAttemptResponse createByEdition(
      @Valid @RequestBody CreateEditionSimulationRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.createByEdition(requireAuth(principal), req);
  }

  @Operation(summary = "Listar execuções do aluno (mais recentes primeiro).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Página de execuções."),
    @ApiResponse(responseCode = "400", description = "Paginação inválida.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/attempts")
  public PageResponse<SimulationAttemptSummary> list(
      @Parameter(description = "Página 0-based.") @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Itens por página [1–100].") @RequestParam(defaultValue = "20") int size,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.listAttempts(requireAuth(principal), page, size);
  }

  @Operation(summary = "Consultar execução do dono do token (retomar em andamento ou rever encerrada).",
      description = "Enquanto IN_PROGRESS o gabarito fica oculto (só progresso respondida/pendente); "
          + "após encerrar revela o gabarito congelado e o resumo.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Execução com o caderno."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/attempts/{id}")
  public SimulationAttemptResponse get(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getAttempt(requireAuth(principal), id);
  }

  @Operation(summary = "Concluir execução (IN_PROGRESS → SUBMITTED) com placar do servidor.",
      description = "Vale a última tentativa por questão; não respondidas contam como pendentes; "
          + "anuladas ficam fora do aproveitamento (pontuação DESCONHECIDA).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado com a correção por questão."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Simulado já encerrado.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/attempts/{id}/submit")
  public SimulationResultResponse submit(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.submitAttempt(requireAuth(principal), id);
  }

  @Operation(summary = "Desistir da execução (IN_PROGRESS → ABANDONED) com placar parcial.",
      description = "Não existe pausa no servidor: abandonar é a desistência explícita; "
          + "retomar depois é manter IN_PROGRESS via GET.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado parcial."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Simulado já encerrado.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/attempts/{id}/abandon")
  public SimulationResultResponse abandon(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.abandonAttempt(requireAuth(principal), id);
  }

  @Operation(summary = "Visualizar resultado (só após concluir ou abandonar).",
      description = "Correção por posição contra o gabarito congelado. Enquanto IN_PROGRESS "
          + "retorna 409 SIMULATION_NOT_FINISHED (gabarito oculto).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resultado com a correção por questão."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução não encontrada.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Simulado ainda em andamento.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/attempts/{id}/result")
  public SimulationResultResponse result(
      @PathVariable long id,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getResult(requireAuth(principal), id);
  }

  @Operation(summary = "Feedback imediato de uma posição (Modo Estudo).",
      description = "Acerto/erro, resposta correta (gabarito congelado) e "
          + "assunto da última tentativa vinculada a esta execução. No Modo ESTUDO em "
          + "qualquer status; no Modo PROVA só após encerrar (durante a prova retorna "
          + "409 STUDY_FEEDBACK_UNAVAILABLE). Sem resposta na posição retorna 409 "
          + "FEEDBACK_NOT_AVAILABLE.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Feedback da posição."),
    @ApiResponse(responseCode = "400", description = "Posição inválida.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Execução ou posição não encontradas.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "Prova em andamento ou posição sem resposta.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/attempts/{id}/feedback/{position}")
  public StudyFeedbackResponse feedback(
      @PathVariable long id,
      @PathVariable int position,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getStudyFeedback(requireAuth(principal), id, position);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
