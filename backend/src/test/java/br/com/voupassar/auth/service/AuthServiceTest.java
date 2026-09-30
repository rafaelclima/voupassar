package br.com.voupassar.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

import br.com.voupassar.auth.config.AuthProperties;
import br.com.voupassar.auth.dto.ChangePasswordRequest;
import br.com.voupassar.auth.dto.ForgotPasswordRequest;
import br.com.voupassar.auth.dto.LoginRequest;
import br.com.voupassar.auth.dto.LogoutRequest;
import br.com.voupassar.auth.dto.RefreshRequest;
import br.com.voupassar.auth.dto.RegisterRequest;
import br.com.voupassar.auth.dto.ResetPasswordRequest;
import br.com.voupassar.auth.dto.TokenResponse;
import br.com.voupassar.auth.entity.PasswordResetToken;
import br.com.voupassar.auth.entity.RefreshToken;
import br.com.voupassar.auth.entity.Role;
import br.com.voupassar.auth.entity.StudentProfile;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.entity.UserRole;
import br.com.voupassar.auth.repository.PasswordResetTokenRepository;
import br.com.voupassar.auth.repository.RefreshTokenRepository;
import br.com.voupassar.auth.repository.RoleRepository;
import br.com.voupassar.auth.repository.StudentProfileRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.auth.repository.UserRoleRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ConflictException;
import br.com.voupassar.exception.UnauthorizedException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Regras do AuthService (TASK 3.5) com mocks — sem banco.
 *
 * <p>BCrypt e JWT reais (custo 4 por velocidade); repositórios mockados.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock UserRepository users;
  @Mock RoleRepository roles;
  @Mock UserRoleRepository userRoles;
  @Mock RefreshTokenRepository refreshTokens;
  @Mock PasswordResetTokenRepository resetTokens;
  @Mock StudentProfileRepository profiles;

  private AuthService service;
  private PasswordEncoder encoder;

  @BeforeEach
  void setup() {
    AuthProperties props = new AuthProperties();
    props.setJwtSecret("test-secret-min-32-chars-0123456789abcdef");
    props.setJwtIssuer("voupassar-test");
    props.setAccessTtlMinutes(15);
    props.setRefreshTtlDays(7);
    props.setResetTtlMinutes(60);
    encoder = new BCryptPasswordEncoder(4);
    service =
        new AuthService(users, roles, userRoles, refreshTokens, resetTokens, profiles, encoder,
            new JwtService(props), props);
  }

  private static User user(long id, String email, String rawPassword, PasswordEncoder enc) {
    User u = new User(email, enc.encode(rawPassword));
    ReflectionTestUtils.setField(u, "id", id);
    return u;
  }

  private static RegisterRequest registerReq() {
    return new RegisterRequest("Novo@Exemplo.com", "senha-segura-123", "Novo Aluno", null, null, null);
  }

  // ---- registro ----

  @Test
  void registerNormalizesEmailGrantsStudentAndIssuesPair() {
    when(users.existsByEmail("novo@exemplo.com")).thenReturn(false);
    when(users.save(any(User.class)))
        .thenAnswer(inv -> {
          User u = inv.getArgument(0);
          ReflectionTestUtils.setField(u, "id", 7L);
          return u;
        });
    when(roles.findByCode("STUDENT")).thenReturn(Optional.of(new Role()));
    when(profiles.findById(7L)).thenReturn(Optional.empty());

    TokenResponse pair = service.register(registerReq(), "127.0.0.1", "test");

    ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
    verify(users).save(saved.capture());
    assertEquals("novo@exemplo.com", saved.getValue().getEmail());
    assertNotEquals("senha-segura-123", saved.getValue().getPasswordHash());
    assertTrue(encoder.matches("senha-segura-123", saved.getValue().getPasswordHash()));
    verify(userRoles).save(any(UserRole.class));
    verify(profiles).save(any(StudentProfile.class));
    assertNotNull(pair.accessToken());
    assertNotNull(pair.refreshToken());
    assertEquals("Bearer", pair.tokenType());
    assertEquals(900L, pair.expiresIn());
    assertEquals(7L, pair.user().id());
  }

  @Test
  void registerDuplicateEmailIs409() {
    when(users.existsByEmail("novo@exemplo.com")).thenReturn(true);

    assertThrows(ConflictException.class, () -> service.register(registerReq(), null, null));
    verify(users, never()).save(any());
  }

  // ---- login ----

  @Test
  void loginSuccessIssuesPairAndStampsLastLogin() {
    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    when(users.findByEmail("a@b.c")).thenReturn(Optional.of(u));
    when(userRoles.findRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));
    when(profiles.findById(1L)).thenReturn(Optional.empty());

    TokenResponse pair = service.login(new LoginRequest("A@B.c", "senha-segura-123"), null, null);

    assertNotNull(pair.accessToken());
    assertNotNull(u.getLastLoginAt());
  }

  @Test
  void loginFailuresAreGeneric401() {
    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    when(users.findByEmail("a@b.c")).thenReturn(Optional.of(u));

    assertThrows(UnauthorizedException.class,
        () -> service.login(new LoginRequest("a@b.c", "errada"), null, null));
    assertThrows(UnauthorizedException.class,
        () -> service.login(new LoginRequest("ninguem@x.y", "senha-segura-123"), null, null));

    u.setActive(false);
    assertThrows(UnauthorizedException.class,
        () -> service.login(new LoginRequest("a@b.c", "senha-segura-123"), null, null));
  }

  // ---- refresh ----

  @Test
  void refreshRotatesAndRevokesParent() {
    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    RefreshToken parent =
        new RefreshToken(u, "hash-pai", OffsetDateTime.now().plusDays(7));
    when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.of(parent));
    when(userRoles.findRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));
    when(profiles.findById(1L)).thenReturn(Optional.empty());

    TokenResponse pair = service.refresh(new RefreshRequest("raw-pai"), null, null);

    assertNotNull(parent.getRevokedAt());
    assertNotNull(parent.getReplacedBy());
    assertNotNull(pair.refreshToken());
    assertNotEquals("raw-pai", pair.refreshToken());
  }

  @Test
  void refreshReuseRevokesChain() {
    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    RefreshToken parent = new RefreshToken(u, "hash-pai", OffsetDateTime.now().plusDays(7));
    parent.setRevokedAt(OffsetDateTime.now().minusMinutes(1));
    parent.setReplacedBy(new RefreshToken(u, "hash-filho", OffsetDateTime.now().plusDays(7)));
    when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.of(parent));

    UnauthorizedException ex =
        assertThrows(UnauthorizedException.class,
            () -> service.refresh(new RefreshRequest("raw-pai"), null, null));
    assertEquals("REFRESH_REUSED", ex.getCode());
    verify(refreshTokens).revokeAllActiveByUserId(eq(1L), any());
  }

  @Test
  void refreshUnknownOrExpiredIs401() {
    when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.empty());
    assertThrows(UnauthorizedException.class,
        () -> service.refresh(new RefreshRequest("x"), null, null));

    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    RefreshToken expired = new RefreshToken(u, "h", OffsetDateTime.now().minusMinutes(1));
    when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.of(expired));
    assertThrows(UnauthorizedException.class,
        () -> service.refresh(new RefreshRequest("x"), null, null));
  }

  // ---- logout ----

  @Test
  void logoutIsIdempotent() {
    when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.empty());
    assertEquals("Sessão encerrada.", service.logout(new LogoutRequest("x")).message());

    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    RefreshToken t = new RefreshToken(u, "h", OffsetDateTime.now().plusDays(1));
    when(refreshTokens.findByTokenHash(any())).thenReturn(Optional.of(t));
    service.logout(new LogoutRequest("x"));
    assertNotNull(t.getRevokedAt());
  }

  // ---- forgot/reset ----

  @Test
  void forgotIsGenericButIssuesTokenForActiveAccount() {
    User u = user(1L, "a@b.c", "senha-segura-123", encoder);
    when(users.findByEmail("a@b.c")).thenReturn(Optional.of(u));

    var msg = service.forgotPassword(new ForgotPasswordRequest("a@b.c"), null);

    assertTrue(msg.message().contains("Se o e-mail"));
    verify(resetTokens).save(any(PasswordResetToken.class));
  }

  @Test
  void forgotUnknownIssuesNothingButSameMessage() {
    when(users.findByEmail("z@z.z")).thenReturn(Optional.empty());

    var msg = service.forgotPassword(new ForgotPasswordRequest("z@z.z"), null);

    assertTrue(msg.message().contains("Se o e-mail"));
    verify(resetTokens, never()).save(any());
  }

  @Test
  void resetValidRotatesCredentialsAndKillsSessions() {
    User u = user(1L, "a@b.c", "antiga-123", encoder);
    PasswordResetToken token =
        new PasswordResetToken(u, "h", OffsetDateTime.now().plusMinutes(60));
    when(resetTokens.findByTokenHash(any())).thenReturn(Optional.of(token));

    service.resetPassword(new ResetPasswordRequest("raw", "nova-senha-456"));

    assertTrue(encoder.matches("nova-senha-456", u.getPasswordHash()));
    assertEquals(2, u.getCredentialVersion());
    assertNotNull(token.getUsedAt());
    verify(refreshTokens).revokeAllActiveByUserId(eq(1L), any());
  }

  @Test
  void resetReusedOrExpiredIs400() {
    User u = user(1L, "a@b.c", "antiga-123", encoder);
    PasswordResetToken used = new PasswordResetToken(u, "h", OffsetDateTime.now().plusMinutes(60));
    used.setUsedAt(OffsetDateTime.now());
    when(resetTokens.findByTokenHash(any())).thenReturn(Optional.of(used));
    assertThrows(BadRequestException.class,
        () -> service.resetPassword(new ResetPasswordRequest("raw", "nova-senha-456")));

    PasswordResetToken expired =
        new PasswordResetToken(u, "h", OffsetDateTime.now().minusMinutes(1));
    when(resetTokens.findByTokenHash(any())).thenReturn(Optional.of(expired));
    assertThrows(BadRequestException.class,
        () -> service.resetPassword(new ResetPasswordRequest("raw", "nova-senha-456")));

    when(resetTokens.findByTokenHash(any())).thenReturn(Optional.empty());
    assertThrows(BadRequestException.class,
        () -> service.resetPassword(new ResetPasswordRequest("raw", "nova-senha-456")));
  }

  // ---- change/me ----

  @Test
  void changePasswordRequiresCurrentAndReissuesPair() {
    User u = user(1L, "a@b.c", "antiga-123", encoder);
    when(users.findById(1L)).thenReturn(Optional.of(u));
    when(userRoles.findRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));
    when(profiles.findById(1L)).thenReturn(Optional.empty());

    TokenResponse pair =
        service.changePassword(1L, new ChangePasswordRequest("antiga-123", "nova-senha-456"), null, null);

    assertNotNull(pair.accessToken());
    assertEquals(2, u.getCredentialVersion());
    verify(refreshTokens).revokeAllActiveByUserId(eq(1L), any());
  }

  @Test
  void changePasswordRejectsWrongCurrentAndSamePassword() {
    User u = user(1L, "a@b.c", "antiga-123", encoder);
    when(users.findById(1L)).thenReturn(Optional.of(u));

    assertThrows(UnauthorizedException.class,
        () -> service.changePassword(1L, new ChangePasswordRequest("errada", "nova-senha-456"), null, null));
    assertThrows(BadRequestException.class,
        () -> service.changePassword(1L, new ChangePasswordRequest("antiga-123", "antiga-123"), null, null));
  }

  @Test
  void meReturnsProfileDisplayName() {
    User u = user(1L, "a@b.c", "x", encoder);
    when(users.findById(1L)).thenReturn(Optional.of(u));
    when(userRoles.findRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));
    StudentProfile p = new StudentProfile(u, "Apelido");
    when(profiles.findById(1L)).thenReturn(Optional.of(p));

    var me = service.me(1L);

    assertEquals("Apelido", me.displayName());
    assertEquals(List.of("STUDENT"), me.roles());
  }
}
