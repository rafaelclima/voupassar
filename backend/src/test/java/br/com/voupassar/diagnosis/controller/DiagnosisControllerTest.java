package br.com.voupassar.diagnosis.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.diagnosis.dto.DiagnosisResponse;
import br.com.voupassar.diagnosis.service.DiagnosisService;
import br.com.voupassar.security.UserPrincipal;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Contrato JSON do /diagnosis sem subir contexto (TASK 4.2).
 */
@ExtendWith(MockitoExtension.class)
class DiagnosisControllerTest {

  @Mock DiagnosisService service;

  @InjectMocks DiagnosisController controller;

  private MockMvc mvc() {
    return MockMvcBuilders.standaloneSetup(controller)
        .setControllerAdvice(new br.com.voupassar.exception.GlobalExceptionHandler())
        .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
        .build();
  }

  @AfterEach
  void clearAuth() {
    SecurityContextHolder.clearContext();
  }

  private static void authenticate() {
    UserPrincipal principal = new UserPrincipal(1L, "a@b.c", List.of("STUDENT"), 1);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  private static DiagnosisResponse empty() {
    return new DiagnosisResponse(
        0L, 0L, 0L, 0L, 0L, null, "DESCONHECIDO", null, 0L,
        List.of(), List.of(), List.of(), List.of(), List.of(),
        List.of(), List.of(), List.of("nota"));
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(get("/api/v1/diagnosis"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void diagnoseIs200() throws Exception {
    authenticate();
    when(service.getDiagnosis(anyLong(), isNull())).thenReturn(empty());

    mvc().perform(get("/api/v1/diagnosis"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.overallLevel").value("DESCONHECIDO"))
        .andExpect(jsonPath("$.notes[0]").exists());
  }

  @Test
  void institutionEajReachesService() throws Exception {
    authenticate();
    when(service.getDiagnosis(anyLong(), eq("EAJ"))).thenReturn(empty());

    mvc().perform(get("/api/v1/diagnosis").param("institution", "EAJ"))
        .andExpect(status().isOk());

    verify(service).getDiagnosis(anyLong(), eq("EAJ"));
  }
}
