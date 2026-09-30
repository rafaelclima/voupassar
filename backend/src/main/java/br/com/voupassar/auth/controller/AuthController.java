package br.com.voupassar.auth.controller;

import br.com.voupassar.auth.dto.ChangePasswordRequest;
import br.com.voupassar.auth.dto.ForgotPasswordRequest;
import br.com.voupassar.auth.dto.LoginRequest;
import br.com.voupassar.auth.dto.LogoutRequest;
import br.com.voupassar.auth.dto.MessageResponse;
import br.com.voupassar.auth.dto.RefreshRequest;
import br.com.voupassar.auth.dto.RegisterRequest;
import br.com.voupassar.auth.dto.ResetPasswordRequest;
import br.com.voupassar.auth.dto.TokenResponse;
import br.com.voupassar.auth.dto.UserResponse;
import br.com.voupassar.auth.service.AuthService;
import br.com.voupassar.common.dto.ApiError;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autenticação (TASK 3.5).
 *
 * <p>Público: registro, login, refresh, logout (por refresh), recuperação e
 * reset. Autenticado (Bearer): {@code /me}, troca de senha e logout global.
 * Sem regra de negócio aqui (AGENTS.md §19) — tudo no {@link AuthService}.
 */
@RestController
@RequestMapping(path = "/api/v1/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Autenticação", description = "Registro, login, tokens, logout e recuperação de senha.")
public class AuthController {

  private final AuthService service;

  public AuthController(AuthService service) {
    this.service = service;
  }

  @Operation(summary = "Cadastrar conta (papel STUDENT + perfil mínimo).")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Conta criada; par de tokens emitido."),
    @ApiResponse(responseCode = "400", description = "Dados inválidos.",
        content = @Content(schema = @Schema(implementation = ApiError.class))),
    @ApiResponse(responseCode = "409", description = "E-mail já cadastrado.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public TokenResponse register(@Valid @RequestBody RegisterRequest req, HttpServletRequest http) {
    return service.register(req, clientIp(http), userAgent(http));
  }

  @Operation(summary = "Entrar (falha sempre genérica, anti-enumeração).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Par de tokens emitido."),
    @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  public TokenResponse login(@Valid @RequestBody LoginRequest req, HttpServletRequest http) {
    return service.login(req, clientIp(http), userAgent(http));
  }

  @Operation(summary = "Rotacionar refresh token (pai é revogado; reuso derruba a cadeia).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Par novo emitido."),
    @ApiResponse(responseCode = "401", description = "Refresh inválido, expirado ou reusado.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/refresh", consumes = MediaType.APPLICATION_JSON_VALUE)
  public TokenResponse refresh(@Valid @RequestBody RefreshRequest req, HttpServletRequest http) {
    return service.refresh(req, clientIp(http), userAgent(http));
  }

  @Operation(summary = "Encerrar uma sessão (revoga o refresh; idempotente).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Sessão encerrada."),
    @ApiResponse(responseCode = "400", description = "Corpo inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/logout", consumes = MediaType.APPLICATION_JSON_VALUE)
  public MessageResponse logout(@Valid @RequestBody LogoutRequest req) {
    return service.logout(req);
  }

  @Operation(summary = "Encerrar todas as sessões do usuário autenticado.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Todas as sessões encerradas."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping("/logout-all")
  public MessageResponse logoutAll(@AuthenticationPrincipal UserPrincipal principal) {
    return service.logoutAll(requireAuth(principal));
  }

  @Operation(summary = "Pedir recuperação de senha (resposta sempre genérica).",
      description = "Emite token de uso único quando a conta existe. Entrega por e-mail "
          + "pendente de provedor transacional — ver docs/api-auth.md.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Resposta genérica (exista ou não a conta)."),
    @ApiResponse(responseCode = "400", description = "E-mail inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/password/forgot", consumes = MediaType.APPLICATION_JSON_VALUE)
  public MessageResponse forgot(@Valid @RequestBody ForgotPasswordRequest req, HttpServletRequest http) {
    return service.forgotPassword(req, clientIp(http));
  }

  @Operation(summary = "Redefinir senha com token de uso único (derruba sessões).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Senha redefinida."),
    @ApiResponse(responseCode = "400", description = "Token inválido/expirado ou senha fraca.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/password/reset", consumes = MediaType.APPLICATION_JSON_VALUE)
  public MessageResponse reset(@Valid @RequestBody ResetPasswordRequest req) {
    return service.resetPassword(req);
  }

  @Operation(summary = "Trocar senha autenticado (exige atual; devolve par novo).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Senha trocada; par novo emitido."),
    @ApiResponse(responseCode = "401", description = "Token inválido ou senha atual incorreta.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @PostMapping(path = "/password/change", consumes = MediaType.APPLICATION_JSON_VALUE)
  public TokenResponse changePassword(@Valid @RequestBody ChangePasswordRequest req,
      @AuthenticationPrincipal UserPrincipal principal, HttpServletRequest http) {
    return service.changePassword(requireAuth(principal), req, clientIp(http), userAgent(http));
  }

  @Operation(summary = "Conta autenticada (validação de token + dados mínimos).")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Conta + papéis."),
    @ApiResponse(responseCode = "401", description = "Sem token ou token inválido.",
        content = @Content(schema = @Schema(implementation = ApiError.class)))
  })
  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
    return service.me(requireAuth(principal));
  }

  private static long requireAuth(UserPrincipal principal) {
    if (principal == null) {
      throw new UnauthorizedException("Autenticação necessária.");
    }
    return principal.userId();
  }

  private static String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded != null && !forwarded.isBlank()) {
      int comma = forwarded.indexOf(',');
      return (comma < 0 ? forwarded : forwarded.substring(0, comma)).trim();
    }
    String remote = request.getRemoteAddr();
    return remote != null ? remote : "-";
  }

  private static String userAgent(HttpServletRequest request) {
    String ua = request.getHeader("User-Agent");
    if (ua != null && ua.length() > 255) {
      return ua.substring(0, 255);
    }
    return ua;
  }
}
