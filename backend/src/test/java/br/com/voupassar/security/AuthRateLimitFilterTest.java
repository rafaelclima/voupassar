package br.com.voupassar.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.voupassar.auth.config.AuthProperties;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Rate limiting de borda (TASK 22.1): rajada acima do limite → 429 +
 * {@code Retry-After}; uso normal passa; GET não é limitado; XFF só vale
 * atrás de proxy configurado.
 */
class AuthRateLimitFilterTest {

  private AuthProperties props;
  private AuthRateLimitFilter filter;

  @BeforeEach
  void setup() {
    props = new AuthProperties();
    props.getRateLimit().setEnabled(true);
    props.getRateLimit().setMaxRequests(2);
    props.getRateLimit().setWriteMaxRequests(2);
    props.getRateLimit().setWindowSeconds(60);
    props.setBehindProxy(false);
    filter = new AuthRateLimitFilter(props, new ClientIpResolver(props), new ObjectMapper());
  }

  private static MockHttpServletRequest post(String uri, String remote) {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
    req.setRemoteAddr(remote);
    return req;
  }

  @Test
  void writeBurstIs429WithRetryAfter() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 2; i++) {
      filter.doFilter(post("/api/v1/simulations/by-discipline", "9.9.9.9"),
          new MockHttpServletResponse(), chain);
    }
    MockHttpServletResponse blocked = new MockHttpServletResponse();
    filter.doFilter(post("/api/v1/simulations/by-discipline", "9.9.9.9"), blocked, chain);

    verify(chain, times(2)).doFilter(any(), any());
    assertEquals(429, blocked.getStatus());
    assertEquals("60", blocked.getHeader("Retry-After"));
    assertTrue(blocked.getContentAsString().contains("RATE_LIMITED"));
  }

  @Test
  void readsAreNotLimited() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 5; i++) {
      MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/simulations/attempts/1");
      req.setRemoteAddr("9.9.9.9");
      filter.doFilter(req, new MockHttpServletResponse(), chain);
    }
    verify(chain, times(5)).doFilter(any(), any());
  }

  @Test
  void authRouteStillLimited() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 2; i++) {
      filter.doFilter(post("/api/v1/auth/login", "9.9.9.9"), new MockHttpServletResponse(), chain);
    }
    MockHttpServletResponse blocked = new MockHttpServletResponse();
    filter.doFilter(post("/api/v1/auth/login", "9.9.9.9"), blocked, chain);

    verify(chain, times(2)).doFilter(any(), any());
    assertEquals(429, blocked.getStatus());
    assertNotNull(blocked.getHeader("Retry-After"));
  }

  @Test
  void otherPrefixesUntouched() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 5; i++) {
      filter.doFilter(post("/api/v1/review/sessions", "9.9.9.9"),
          new MockHttpServletResponse(), chain);
    }
    verify(chain, times(5)).doFilter(any(), any());
  }

  @Test
  void spoofedXffIgnoredWithoutProxy() throws Exception {
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 2; i++) {
      filter.doFilter(post("/api/v1/attempts", "9.9.9.9"), new MockHttpServletResponse(), chain);
    }
    // Balde cheio pelo IP real: forjar XFF não abre balde novo.
    MockHttpServletRequest spoofed = post("/api/v1/attempts", "9.9.9.9");
    spoofed.addHeader("X-Forwarded-For", "1.2.3.4");
    MockHttpServletResponse blocked = new MockHttpServletResponse();
    filter.doFilter(spoofed, blocked, chain);
    assertEquals(429, blocked.getStatus());

    // Outro IP real tem balde próprio, mesmo com o mesmo XFF.
    MockHttpServletRequest other = post("/api/v1/attempts", "8.8.8.8");
    other.addHeader("X-Forwarded-For", "1.2.3.4");
    filter.doFilter(other, new MockHttpServletResponse(), chain);
    verify(chain, times(3)).doFilter(any(), any());
  }

  @Test
  void xffTrustedBehindProxy() throws Exception {
    props.setBehindProxy(true);
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 2; i++) {
      MockHttpServletRequest req = post("/api/v1/attempts", "10.0.0.1");
      req.addHeader("X-Forwarded-For", "203.0.113.7, 10.0.0.1");
      filter.doFilter(req, new MockHttpServletResponse(), chain);
    }
    // Primeira entrada do XFF esgotou: mesmo proxy, outro cliente passa.
    MockHttpServletRequest other = post("/api/v1/attempts", "10.0.0.1");
    other.addHeader("X-Forwarded-For", "203.0.113.9");
    filter.doFilter(other, new MockHttpServletResponse(), chain);
    verify(chain, times(3)).doFilter(any(), any());
  }

  @Test
  void disabledPassesEverything() throws Exception {
    props.getRateLimit().setEnabled(false);
    FilterChain chain = mock(FilterChain.class);
    for (int i = 0; i < 5; i++) {
      filter.doFilter(post("/api/v1/attempts", "9.9.9.9"), new MockHttpServletResponse(), chain);
    }
    verify(chain, times(5)).doFilter(any(), any());
  }
}
