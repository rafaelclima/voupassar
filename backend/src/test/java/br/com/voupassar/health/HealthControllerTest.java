package br.com.voupassar.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Health público: acessível sem auth, com traceId e sem vazar detalhes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest {

  @Autowired MockMvc mvc;

  @Test
  void healthIsPublicAndReturnsUp() throws Exception {
    mvc.perform(get("/api/v1/health"))
        .andExpect(status().isOk())
        .andExpect(header().exists("X-Trace-Id"))
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.service").value("voupassar-backend"))
        .andExpect(jsonPath("$.version").exists())
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  @WithMockUser
  void unknownRouteReturnsEnvelopeWithoutStacktrace() throws Exception {
    mvc.perform(get("/api/v1/rota-que-nao-existe"))
        .andExpect(status().isNotFound())
        .andExpect(header().exists("X-Trace-Id"))
        .andExpect(jsonPath("$.code").value("NOT_FOUND"))
        .andExpect(jsonPath("$.message").exists())
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.path").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void protectedRouteWithoutAuthIsNotPublic() throws Exception {
    // Secure-by-default: fora do health, sem token → 401/403 em envelope
    // (nunca 200, nunca corpo vazio, nunca stack trace).
    mvc.perform(get("/api/v1/editions"))
        .andExpect(status().is4xxClientError())
        .andExpect(header().exists("X-Trace-Id"))
        .andExpect(jsonPath("$.code").exists())
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }
}
