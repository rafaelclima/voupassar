package br.com.voupassar.performance.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.GlobalExceptionHandler;
import br.com.voupassar.performance.dto.PerformanceEvolutionResponse;
import br.com.voupassar.performance.dto.PerformanceOverviewResponse;
import br.com.voupassar.performance.service.PerformanceService;
import br.com.voupassar.security.UserPrincipal;
import java.time.LocalDate;
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
 * Contrato JSON do /performance sem subir contexto (TASK 4.1).
 */
@ExtendWith(MockitoExtension.class)
class PerformanceControllerTest {

  @Mock PerformanceService service;

  @InjectMocks PerformanceController controller;

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

  private static void authenticate() {
    UserPrincipal principal = new UserPrincipal(1L, "a@b.c", List.of("STUDENT"), 1);
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
  }

  private static PerformanceOverviewResponse overview() {
    return new PerformanceOverviewResponse(
        4L, 3L, 2L, 1L, 1L, 2.0 / 3.0, OffsetDateTime.parse("2026-09-04T10:00:00Z"), 0L,
        List.of(), List.of(), List.of(), List.of("nota"));
  }

  @Test
  void withoutPrincipalIs401() throws Exception {
    mvc().perform(get("/api/v1/performance/overview")).andExpect(status().isUnauthorized());
    mvc().perform(get("/api/v1/performance/evolution")).andExpect(status().isUnauthorized());
    mvc().perform(post("/api/v1/performance/rebuild")).andExpect(status().isUnauthorized());
  }

  @Test
  void overviewIs200() throws Exception {
    authenticate();
    when(service.getOverview(1L)).thenReturn(overview());

    mvc().perform(get("/api/v1/performance/overview"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalAttempts").value(4))
        .andExpect(jsonPath("$.scoredAttempts").value(3))
        .andExpect(jsonPath("$.notes[0]").exists());
  }

  @Test
  void evolutionIs200AndInvalidGranularityIs400() throws Exception {
    authenticate();
    when(service.getEvolution(anyLong(), anyString())).thenReturn(
        new PerformanceEvolutionResponse("WEEK",
            List.of(new PerformanceEvolutionResponse.BucketResponse(
                LocalDate.of(2026, 8, 31), 2L, 2L, 1L, 0.5)),
            List.of("nota")));

    mvc().perform(get("/api/v1/performance/evolution?granularity=WEEK"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.granularity").value("WEEK"))
        .andExpect(jsonPath("$.buckets[0].attempts").value(2));

    when(service.getEvolution(anyLong(), anyString()))
        .thenThrow(new BadRequestException("INVALID_GRANULARITY", "Granularidade deve ser DAY, WEEK ou MONTH."));
    mvc().perform(get("/api/v1/performance/evolution?granularity=YEAR"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_GRANULARITY"));
  }

  @Test
  void rebuildIs200() throws Exception {
    authenticate();
    when(service.rebuildTopicPerformance(1L)).thenReturn(
        new br.com.voupassar.performance.dto.RebuildResponse(
            3, OffsetDateTime.parse("2026-09-04T10:00:00Z")));

    mvc().perform(post("/api/v1/performance/rebuild"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.rebuiltRows").value(3));
  }
}
