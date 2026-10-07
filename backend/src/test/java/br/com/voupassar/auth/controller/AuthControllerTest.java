package br.com.voupassar.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.auth.dto.MessageResponse;
import br.com.voupassar.auth.dto.TokenResponse;
import br.com.voupassar.auth.dto.UserResponse;
import br.com.voupassar.auth.service.AuthService;
import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.security.UserPrincipal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Contrato JSON do /auth sem subir contexto (TASK 3.5).
 *
 * <p>Rotas autenticadas recebem o principal via SecurityContext (mesma
 * thread) + {@code AuthenticationPrincipalArgumentResolver} explícito.
 */
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock AuthService service;

  @Mock br.com.voupassar.security.ClientIpResolver clientIpResolver;

  @InjectMocks AuthController controller;

  private MockMvc mvc() {
    return MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  private static TokenResponse pair() {
    return new TokenResponse("access.jwt", "Bearer", 900L, "opaque-refresh",
        new UserResponse(1L, "a@b.c", "Aluno", List.of("STUDENT")));
  }

  private static void authenticate() {
    UserPrincipal principal = new UserPrincipal(1L, "a@b.c", List.of("STUDENT"), 1);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  @Test
  void registerReturns201WithPair() throws Exception {
    when(service.register(any(), any(), any())).thenReturn(pair());

    mvc().perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@b.c\",\"password\":\"senha-segura-123\",\"displayName\":\"Aluno\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.accessToken").value("access.jwt"))
        .andExpect(jsonPath("$.tokenType").value("Bearer"))
        .andExpect(jsonPath("$.refreshToken").value("opaque-refresh"))
        .andExpect(jsonPath("$.user.roles[0]").value("STUDENT"));
  }

  @Test
  void registerValidationFails400WithDetails() throws Exception {
    mvc().perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"invalido\",\"password\":\"curta\",\"displayName\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details").isArray())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void loginReturns200() throws Exception {
    when(service.login(any(), any(), any())).thenReturn(pair());

    mvc().perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@b.c\",\"password\":\"senha-segura-123\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());
  }

  @Test
  void refreshAndLogoutReturn200() throws Exception {
    when(service.refresh(any(), any(), any())).thenReturn(pair());
    when(service.logout(any())).thenReturn(new MessageResponse("Sessão encerrada."));

    mvc().perform(post("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"opaque\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());

    mvc().perform(post("/api/v1/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"refreshToken\":\"opaque\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Sessão encerrada."));
  }

  @Test
  void forgotAndResetReturnGeneric200() throws Exception {
    when(service.forgotPassword(any(), any()))
        .thenReturn(new MessageResponse("Se o e-mail estiver cadastrado, um link foi gerado."));
    when(service.resetPassword(any())).thenReturn(new MessageResponse("Senha redefinida."));

    mvc().perform(post("/api/v1/auth/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@b.c\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());

    mvc().perform(post("/api/v1/auth/password/reset")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"token\":\"opaque\",\"newPassword\":\"nova-senha-456\"}"))
        .andExpect(status().isOk());
  }

  @Test
  void meWithoutPrincipalIs401() throws Exception {
    mvc().perform(get("/api/v1/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void meWithPrincipalIs200() throws Exception {
    authenticate();
    when(service.me(1L))
        .thenReturn(new UserResponse(1L, "a@b.c", "Aluno", List.of("STUDENT")));

    mvc().perform(get("/api/v1/auth/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("a@b.c"));
  }

  @Test
  void changeAndLogoutAllRequirePrincipal() throws Exception {
    mvc().perform(post("/api/v1/auth/password/change")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"a\",\"newPassword\":\"nova-senha-456\"}"))
        .andExpect(status().isUnauthorized());

    authenticate();
    when(service.changePassword(anyLong(), any(), any(), any())).thenReturn(pair());
    when(service.logoutAll(1L)).thenReturn(new MessageResponse("Todas encerradas."));

    mvc().perform(post("/api/v1/auth/password/change")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"currentPassword\":\"antiga-123\",\"newPassword\":\"nova-senha-456\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").exists());

    mvc().perform(post("/api/v1/auth/logout-all"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").exists());
  }
}
