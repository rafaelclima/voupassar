package br.com.voupassar.performance.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.performance.dto.PerformanceEvolutionResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse;
import br.com.voupassar.performance.dto.RebuildResponse;
import br.com.voupassar.performance.service.PerformanceService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Desempenho do aluno (TASK 4.1) — cálculo determinístico sobre fatos.
 *
 * <p>Rotas autenticadas (Bearer), escopadas ao dono do token. Sem regra de
 * negócio aqui (AGENTS.md §19) — tudo no {@link PerformanceService}. O
 * agregado {@code student_topic_performance} é reconstruído a cada tentativa
 * (via {@code POST /attempts}) e pode ser refeito sob demanda em {@code
 * POST /performance/rebuild} (idempotente).
 */
@RestController
@RequestMapping(path = "/api/v1/performance", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Desempenho",
    description = "Acerto geral/por disciplina/assunto/subassunto e evolução temporal (TASK 4.1).")
public class PerformanceController {

  private final PerformanceService service;

  public PerformanceController(PerformanceService service) {
    this.service = service;
  }

  @Operation(summary = "Retrato do desempenho (geral + disciplina + assunto + subassunto).",
      description = "Anuladas fora do aproveitamento (pontuação DESCONHECIDA); "
          + "assuntos usam a vigente não-rejeitada (revisão PENDENTE).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Desempenho calculado."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/overview")
  public PerformanceOverviewResponse overview(
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getOverview(requireAuth(principal));
  }

  @Operation(summary = "Evolução temporal (baldes DAY/WEEK/MONTH em UTC).",
      description = "Só baldes com tentativas, em ordem cronológica, sem "
          + "interpolação; anuladas fora do aproveitamento.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Série temporal."),
    @ApiResponse(responseCode = "400", description = "Granularidade inválida.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/evolution")
  public PerformanceEvolutionResponse evolution(
      @Parameter(description = "DAY, WEEK (segunda-feira) ou MONTH (dia 1).", example = "WEEK")
          @RequestParam(defaultValue = "WEEK") String granularity,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getEvolution(requireAuth(principal), granularity);
  }

  @Operation(summary = "Reconstruir o agregado por conteúdo (idempotente).",
      description = "Recalcula student_topic_performance a partir do fato "
          + "imutável; o rebuild automático já acontece a cada tentativa.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Agregado reconstruído."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/rebuild")
  @ResponseStatus(HttpStatus.OK)
  public RebuildResponse rebuild(@AuthenticationPrincipal UserPrincipal principal) {
    return service.rebuildTopicPerformance(requireAuth(principal));
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
