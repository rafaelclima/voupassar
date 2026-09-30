package br.com.voupassar.diagnosis.controller;

import br.com.voupassar.diagnosis.dto.DiagnosisResponse;
import br.com.voupassar.diagnosis.service.DiagnosisService;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Diagnóstico inicial do aluno (TASK 4.2) — determinístico, transparente,
 * explicável e auditável sobre o fato imutável {@code question_attempts}.
 */
@RestController
@RequestMapping(path = "/api/v1/diagnosis", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Diagnóstico",
    description = "Diagnóstico inicial do aluno: pontos fortes, fracos, lacunas, fila de prioridades e nível estimado (TASK 4.2).")
public class DiagnosisController {

  private final DiagnosisService service;

  public DiagnosisController(DiagnosisService service) {
    this.service = service;
  }

  @Operation(summary = "Diagnóstico inicial do aluno (determinístico e explicável).",
      description = "Fonte única: question_attempts (fato imutável) + classificações vigentes não-rejeitadas (derivada, revisão PENDENTE — TASK 12.2). "
          + "Limiares explícitos: DOMINADO ≥ 70%, FRÁGIL < 50%, sinal mínimo 3 pontuáveis por assunto; lacuna = 0 pontuáveis. "
          + "Fila de prioridades: assunto não-dominado mais urgente primeiro (determinístico, auditável).")
  @GetMapping
  public DiagnosisResponse diagnose(
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.getDiagnosis(requireAuth(principal));
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new br.com.voupassar.exception.UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
