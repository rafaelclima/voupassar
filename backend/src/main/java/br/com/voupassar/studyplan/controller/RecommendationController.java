package br.com.voupassar.studyplan.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.security.UserPrincipal;
import br.com.voupassar.studyplan.dto.StudyPlanItemResponse;
import br.com.voupassar.studyplan.dto.StudyPlanResponse;
import br.com.voupassar.studyplan.service.RecommendationService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Roteiro de estudos do aluno (TASK 4.3 + 4.4, correção IDOR na TASK 16.1).
 *
 * <p>Todo o acesso usa o dono do token ({@code principal}); não existe
 * {@code ?userId} desde a TASK 16.1 (P1 — um aluno nunca gera, lê ou atualiza
 * o plano de outro). O retorno é DTO ({@link StudyPlanResponse} /
 * {@link StudyPlanItemResponse}), nunca entidade JPA.
 */
@RestController
@RequestMapping(path = "/api/v1/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Motor Educacional", description = "Recomendação transparente (TASK 4.3) e roteiro de estudos (TASK 4.4).")
public class RecommendationController {

  private final RecommendationService service;

  public RecommendationController(RecommendationService service) {
    this.service = service;
  }

  /**
   * Gera ou regenera o roteiro de estudos do dono do token.
   */
  @Operation(summary = "Gera roteiro de estudos (TASK 4.3 + 4.4)",
      description = "Algoritmo determinístico e explicável: cada item traz motivo textual e evidência rastreável. "
          + "Desativa o plano anterior (preserva histórico) e cria um novo. Só o dono do token.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Roteiro criado."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping
  public StudyPlanResponse generate(
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.generatePlan(requireAuth(principal), institution);
  }

  @Operation(summary = "Recupera o roteiro vigente",
      description = "Retorna o plano ativo do dono do token e seus itens ordenados por prioridade.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Roteiro vigente."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Sem roteiro vigente (NO_ACTIVE_PLAN).",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/plan")
  public StudyPlanResponse getPlan(
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.getPlan(requireAuth(principal), institution);
  }

  @Operation(summary = "Atualiza status de um item do roteiro",
      description = "Registra progresso do dono do token: TODO → DOING → DONE / SKIPPED. "
          + "Item de outro aluno responde 403.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Status atualizado."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "403", description = "Item pertence a outro aluno.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Item não encontrado (ITEM_NOT_FOUND).",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/items/{itemId}/status")
  public StudyPlanItemResponse updateStatus(
      @PathVariable Long itemId,
      @RequestParam String status,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.updateItemStatus(itemId, status, requireAuth(principal));
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new br.com.voupassar.exception.UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
