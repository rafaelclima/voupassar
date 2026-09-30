package br.com.voupassar.studyplan.controller;

import br.com.voupassar.security.UserPrincipal;
import br.com.voupassar.studyplan.dto.RecommendationResponse;
import br.com.voupassar.studyplan.entity.StudyPlan;
import br.com.voupassar.studyplan.entity.StudyPlanItem;
import br.com.voupassar.studyplan.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/recommendations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Motor Educacional", description = "Recomendação transparente (TASK 4.3) e roteiro de estudos (TASK 4.4).")
public class RecommendationController {

  private final RecommendationService service;

  public RecommendationController(RecommendationService service) {
    this.service = service;
  }

  /**
   * Gera ou regenera o roteiro de estudos do aluno.
   */
  @Operation(summary = "Gera roteiro de estudos (TASK 4.3 + 4.4)",
      description = "Algoritmo determinístico e explicável: cada item traz motivo textual e evidência rastreável. "
          + "Desativa o plano anterior (preserva histórico) e cria um novo.")
  @PostMapping
  public StudyPlan generate(
      @org.springframework.web.bind.annotation.RequestParam(required = false) Long userId,
      @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal principal) {
    long uid = (userId != null) ? userId : requireAuth(principal);
    return service.generatePlan(uid);
  }

  @Operation(summary = "Recupera o roteiro vigente", description = "Retorna o plano ativo e seus itens ordenados por prioridade.")
  @GetMapping("/plan")
  public StudyPlan getPlan(
      @org.springframework.web.bind.annotation.RequestParam(required = false) Long userId,
      @org.springframework.security.core.annotation.AuthenticationPrincipal UserPrincipal principal) {
    long uid = (userId != null) ? userId : requireAuth(principal);
    StudyPlan plan = service.getPlan(uid);
    List<StudyPlanItem> items = service.getPlanItems(plan.getId());
    plan.setItems(items);
    return plan;
  }

  @Operation(summary = "Atualiza status de um item do roteiro", description = "Registra progresso do aluno: TODO → DOING → DONE / SKIPPED.")
  @PostMapping("/items/{itemId}/status")
  public StudyPlanItem updateStatus(
      @PathVariable Long itemId,
      @org.springframework.web.bind.annotation.RequestParam String status) {
    return service.updateItemStatus(itemId, status);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new br.com.voupassar.exception.UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
