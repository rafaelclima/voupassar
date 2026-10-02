package br.com.voupassar.admin.controller;

import br.com.voupassar.admin.dto.AdminMetricsResponse;
import br.com.voupassar.admin.dto.ClassificationReviewResponse;
import br.com.voupassar.admin.dto.InconsistencyResponse;
import br.com.voupassar.admin.dto.ReviewQueueItemResponse;
import br.com.voupassar.admin.dto.UpdateClassificationRequest;
import br.com.voupassar.admin.dto.UpdateQuestionStatusRequest;
import br.com.voupassar.admin.service.AdminService;
import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.questions.dto.PageResponse;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Área administrativa (TASK 12.1): fila de revisão, inconsistências,
 * métricas e curadoria de questões/classificações.
 *
 * <p>Todo o controller exige {@code CURATOR} ou {@code ADMIN} (papéis do
 * seed V2; o cadastro cria só {@code STUDENT} — a concessão é documentada
 * em {@code docs/api-admin.md}). Leituras não expõem PII nem conteúdo
 * integral de questão; escritas são UPDATEs dirigidos e auditáveis.
 */
@RestController
@RequestMapping(path = "/api/v1/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('CURATOR','ADMIN')")
@Tag(name = "Administração", description = "Curadoria do banco de questões (CURATOR/ADMIN).")
public class AdminController {

  private final AdminService service;

  public AdminController(AdminService service) {
    this.service = service;
  }

  @Operation(
      summary = "Fila de revisão: questões por status de validação.",
      description =
          "Resumo para triagem (detalhe integral no GET /questions/{id}). "
              + "Ordem fixa: ano-fonte, número, id.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Página da fila."),
    @ApiResponse(responseCode = "401", description = "Sem Bearer.", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "403", description = "Sem papel CURATOR/ADMIN.", content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/review-queue")
  public PageResponse<ReviewQueueItemResponse> reviewQueue(
      @Parameter(description = "Status de validação.", example = "PENDING")
          @RequestParam(defaultValue = "PENDING") String validationStatus,
      @Parameter(description = "Página 0-based.", example = "0")
          @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Itens por página (1–100).", example = "20")
          @RequestParam(defaultValue = "20") int size,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    requireAuth(principal);
    return service.reviewQueue(validationStatus, page, size);
  }

  @Operation(
      summary = "Inconsistências atuais do banco de questões.",
      description =
          "Checagens vivas da auditoria 11.1 (alternativas, resposta, enunciado, "
              + "classificação sem tópico). Positivo = fila de curadoria, nunca deleção.")
  @GetMapping("/inconsistencies")
  public List<InconsistencyResponse> inconsistencies(
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    requireAuth(principal);
    return service.inconsistencies();
  }

  @Operation(
      summary = "Métricas do banco de questões.",
      description = "Totais por status de validação, publicação e classificação. Sem PII.")
  @GetMapping("/metrics")
  public AdminMetricsResponse metrics(
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    requireAuth(principal);
    return service.metrics();
  }

  @Operation(
      summary = "Atualizar status de curadoria de uma questão.",
      description =
          "Regra: PUBLICAVEL exige questão APPROVED. APPROVED só sai para REJECTED.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Questão atualizada (resumo)."),
    @ApiResponse(responseCode = "400", description = "Status inválido ou transição proibida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Questão inexistente.", content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PatchMapping("/questions/{id}/status")
  public ReviewQueueItemResponse updateQuestionStatus(
      @Parameter(description = "ID da questão.", example = "1") @PathVariable long id,
      @Valid @RequestBody UpdateQuestionStatusRequest req,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.updateQuestionStatus(id, req, requireAuth(principal));
  }

  @Operation(
      summary = "Revisar uma classificação.",
      description = "REVIEWED/APPROVED/REJECTED com observação; carimba revisor e instante.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Classificação revisada."),
    @ApiResponse(responseCode = "400", description = "Status inválido ou transição proibida.", content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Classificação inexistente.", content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PatchMapping("/classifications/{id}")
  public ClassificationReviewResponse reviewClassification(
      @Parameter(description = "ID da classificação.", example = "10") @PathVariable long id,
      @Valid @RequestBody UpdateClassificationRequest req,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.reviewClassification(id, req, requireAuth(principal));
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
