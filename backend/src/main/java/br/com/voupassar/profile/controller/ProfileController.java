package br.com.voupassar.profile.controller;

import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.profile.dto.AttemptHistoryResponse;
import br.com.voupassar.profile.dto.ProfileResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse;
import br.com.voupassar.profile.dto.UpdateProfileRequest;
import br.com.voupassar.profile.service.ProfileService;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Perfil do aluno (TASK 3.6) — tudo escopado ao dono do token.
 *
 * <p>Rotas autenticadas (Bearer). Sem regra de negócio aqui (AGENTS.md §19)
 * — tudo no {@link ProfileService}. Estatísticas e histórico derivam só de
 * fatos registrados; diagnóstico/roteiro/recomendação ficam para a Fase 4.
 */
@RestController
@RequestMapping(path = "/api/v1/profile", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Perfil", description = "Perfil do aluno: dados, preferências, estatísticas, progresso e histórico.")
public class ProfileController {

  private final ProfileService service;

  public ProfileController(ProfileService service) {
    this.service = service;
  }

  @Operation(summary = "Ver o próprio perfil (dados + preferências).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Perfil do aluno."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "404", description = "Conta ou perfil não encontrados.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping
  public ProfileResponse get(@AuthenticationPrincipal UserPrincipal principal) {
    return service.getProfile(requireAuth(principal));
  }

  @Operation(summary = "Atualizar nome e preferências (ano escolar, ano-alvo, objetivo).",
      description = "PUT completo: exibe nome obrigatório; opcionais aceitam null "
          + "(em-branco vira null). E-mail e senha não mudam aqui.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Perfil atualizado."),
    @ApiResponse(responseCode = "400", description = "Dados inválidos.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ProfileResponse update(
      @Valid @RequestBody UpdateProfileRequest req,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.updateProfile(requireAuth(principal), req);
  }

  @Operation(summary = "Estatísticas + progresso (só fatos registrados).",
      description = "Totais, aproveitamento geral/por modo/por disciplina e "
          + "progresso por conteúdo. Anuladas ficam fora do aproveitamento "
          + "(pontuação DESCONHECIDA). Sem tentativas, accuracy sai null.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Estatísticas do aluno."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/stats")
  public ProfileStatsResponse stats(@AuthenticationPrincipal UserPrincipal principal) {
    return service.getStats(requireAuth(principal));
  }

  @Operation(summary = "Histórico de tentativas (paginado, mais recentes primeiro).",
      description = "Proveniência mínima por item (ano-fonte, número, "
          + "disciplina); sem enunciado nem gabarito.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Página do histórico."),
    @ApiResponse(responseCode = "400", description = "Paginação inválida.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/history")
  public PageResponse<AttemptHistoryResponse> history(
      @Parameter(description = "Página 0-based.", example = "0")
          @RequestParam(defaultValue = "0") int page,
      @Parameter(description = "Itens por página (1–100).", example = "20")
          @RequestParam(defaultValue = "20") int size,
      @AuthenticationPrincipal UserPrincipal principal) {
    return service.getHistory(requireAuth(principal), page, size);
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }
}
