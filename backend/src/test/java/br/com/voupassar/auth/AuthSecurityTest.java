package br.com.voupassar.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Fiação de segurança do /auth no contexto real (TASK 3.5) — sem banco.
 *
 * <p>Prova que o fluxo público passa pelo chain (validação 400, não 401) e
 * que o resto segue protegido (401 em envelope, sem stack trace).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthSecurityTest {

  @Autowired MockMvc mvc;

  @Test
  void publicAuthEndpointsReachValidationInsteadOf401() throws Exception {
    // Corpo inválido chega ao controller (400 de validação) — não é barrado no chain.
    mvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"x\",\"password\":\"y\",\"displayName\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    mvc.perform(post("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

    mvc.perform(post("/api/v1/auth/password/forgot")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"nao-email\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
  }

  @Test
  void authenticatedAuthEndpointsStillRequireBearer() throws Exception {
    mvc.perform(get("/api/v1/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(header().exists("X-Trace-Id"))
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.stackTrace").doesNotExist());

    mvc.perform(post("/api/v1/auth/logout-all"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
  }

  @Test
  void garbageBearerIsDiscardedAsAnonymous() throws Exception {
    mvc.perform(get("/api/v1/auth/me").header("Authorization", "Bearer lixo"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }
}
