package br.com.voupassar.admin.controller;

import br.com.voupassar.admin.dto.AdminMetricsResponse;
import br.com.voupassar.admin.dto.InconsistencyResponse;
import br.com.voupassar.admin.service.AdminService;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Área administrativa: inconsistências e métricas do banco de questões.
 *
 * <p>Todo o controller exige {@code CURATOR} ou {@code ADMIN} (papéis do
 * seed V2; o cadastro cria só {@code STUDENT} — a concessão é documentada
 * em {@code docs/api-admin.md}). Leituras não expõem PII nem conteúdo
 * integral de questão. Desde a decisão de produto 2026-10-06 não há fila
 * de revisão nem PATCH de curadoria — só observabilidade.
 */
@RestController
@RequestMapping(path = "/api/v1/admin", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('CURATOR','ADMIN')")
@Tag(name = "Administração", description = "Saúde do banco de questões (CURATOR/ADMIN).")
public class AdminController {

  private final AdminService service;

  public AdminController(AdminService service) {
    this.service = service;
  }

  @Operation(
      summary = "Inconsistências atuais do banco de questões.",
      description =
          "Checagens vivas da auditoria 11.1 (alternativas, resposta, enunciado, "
              + "classificação sem tópico). Positivo = observação, nunca deleção.")
  @GetMapping("/inconsistencies")
  public List<InconsistencyResponse> inconsistencies(
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    requireAuth(principal);
    return service.inconsistencies();
  }

  @Operation(
      summary = "Métricas do banco de questões.",
      description = "Totais de questões e classificações. Sem PII.")
  @GetMapping("/metrics")
  public AdminMetricsResponse metrics(
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    requireAuth(principal);
    return service.metrics();
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
