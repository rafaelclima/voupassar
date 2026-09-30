package br.com.voupassar.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handler global sem subir contexto: valida envelope, códigos e ausência
 * de stack trace — inclusive no 500.
 */
class GlobalExceptionHandlerTest {

  record ProbeIn(@NotBlank String name) {}

  @RestController
  @RequestMapping("/probe")
  static class ProbeController {
    @GetMapping("/not-found")
    void notFound() {
      throw new ResourceNotFoundException("ITEM_NAO_ENCONTRADO", "Item não encontrado.");
    }

    @GetMapping("/bad-request")
    void badRequest() {
      throw new BadRequestException("Filtro inválido.");
    }

    @GetMapping("/unauthorized")
    void unauthorized() {
      throw new UnauthorizedException("INVALID_CREDENTIALS", "E-mail ou senha inválidos.");
    }

    @GetMapping("/conflict")
    void conflict() {
      throw new ConflictException("EMAIL_IN_USE", "E-mail já cadastrado.");
    }

    @GetMapping("/limited")
    void limited() {
      throw new TooManyRequestsException("Muitas tentativas.");
    }

    @GetMapping("/boom")
    void boom() {
      throw new IllegalStateException("falha interna simulada");
    }

    @PostMapping("/validate")
    void validate(@Valid @RequestBody ProbeIn in) {}
  }

  private final MockMvc mvc =
      MockMvcBuilders.standaloneSetup(new ProbeController())
          .setControllerAdvice(new GlobalExceptionHandler())
          .build();

  @Test
  void notFoundEnvelope() throws Exception {
    mvc.perform(get("/probe/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("ITEM_NAO_ENCONTRADO"))
        .andExpect(jsonPath("$.message").value("Item não encontrado."))
        .andExpect(jsonPath("$.traceId").exists())
        .andExpect(jsonPath("$.stackTrace").doesNotExist());
  }

  @Test
  void badRequestEnvelope() throws Exception {
    mvc.perform(get("/probe/bad-request"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
  }

  @Test
  void validationEnvelopeListsDetails() throws Exception {
    mvc.perform(
            post("/probe/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details").isArray());
  }

  @Test
  void authErrorsKeepEnvelopeAndStatus() throws Exception {
    mvc.perform(get("/probe/unauthorized"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"))
        .andExpect(jsonPath("$.stackTrace").doesNotExist());

    mvc.perform(get("/probe/conflict"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("EMAIL_IN_USE"));

    mvc.perform(get("/probe/limited"))
        .andExpect(status().isTooManyRequests())
        .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
  }

  @Test
  void unexpectedNeverLeaksInternals() throws Exception {
    mvc.perform(get("/probe/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").value("Erro interno. Tente novamente."))
        .andExpect(jsonPath("$.stackTrace").doesNotExist())
        .andExpect(jsonPath("$.details").doesNotExist());
  }
}
