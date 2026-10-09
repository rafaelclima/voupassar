package br.com.voupassar.review.controller;

import br.com.voupassar.review.dto.ReviewQueueResponse;
import br.com.voupassar.review.service.ReviewService;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Modo revisão do aluno (TASK 4.5) — fila determinística e explicável.
 *
 * <p>Só questões já tentadas entram na fila (anuladas ficam fora; nunca
 * tentadas pertencem ao diagnóstico/roteiro). Cada item traz o motivo
 * auditável que o colocou naquela posição.
 */
@RestController
@RequestMapping(path = "/api/v1/review", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Modo Revisão",
    description = "Fila de revisão do aluno: erros, assuntos fracos, questões não dominadas e baixa retenção (TASK 4.5).")
public class ReviewController {

  private final ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  @Operation(summary = "Fila de revisão do aluno (determinística e explicável).",
      description = "Ordem: erros persistentes e regressões primeiro; depois acertos em assuntos frágeis; por fim manutenção do consolidado. "
          + "Anuladas ficam fora da fila (pontuação DESCONHECIDA, TASK 1.3 §4). "
          + "Questões nunca tentadas ficam fora (ver diagnóstico TASK 4.2 e roteiro TASK 4.4). "
          + "Classificação de assunto é derivada com revisão PENDENTE (TASK 12.2).")
  @GetMapping("/queue")
  public ReviewQueueResponse queue(
      @Parameter(description = "Máximo de itens [1–100] (padrão 20).")
      @RequestParam(required = false) Integer limit,
      @Parameter(description = "Filtro por disciplina (ex. MATEMATICA).")
      @RequestParam(required = false) String discipline,
      @Parameter(description = "Filtro por assunto (id do tópico).")
      @RequestParam(required = false) Long topicId,
      @Parameter(description = "Quando true, só erros (ERRO_SEM_ACERTO e ERRO_RECENTE).")
      @RequestParam(required = false, defaultValue = "false") boolean onlyErrors,
      @Parameter(description = "Trilha do processo (IFRN ou EAJ; ausente = global legado).")
      @RequestParam(required = false) String institution,
      @Parameter(hidden = true) @AuthenticationPrincipal UserPrincipal principal) {
    return service.getQueue(requireAuth(principal), limit, discipline, topicId, onlyErrors, institution);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new br.com.voupassar.exception.UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
