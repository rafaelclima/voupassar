package br.com.voupassar.auth.service;

import br.com.voupassar.auth.config.AuthProperties;
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
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Autenticação (TASK 3.5): registro, login, logout, refresh rotativo,
 * recuperação e troca de senha.
 *
 * <p>Regras de evidência/segurança:
 * <ul>
 *   <li>Senhas só em hash bcrypt; nunca em log, nunca na resposta.</li>
 *   <li>Login e forgot respondem genérico (anti-enumeração de contas).</li>
 *   <li>Refresh opaco com rotação: reuso de token já rotacionado revoga a
 *       cadeia inteira (suspeita de roubo).</li>
 *   <li>Troca/reset incrementam {@code credential_version}: access tokens
 *       antigos caem no filtro, refreshes são revogados.</li>
 *   <li>Reset é uso único com expiração (V3).</li>
 * </ul>
 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);
  private static final String STUDENT = "STUDENT";
  private static final String GENERIC_FORGOT_MESSAGE =
      "Se o e-mail estiver cadastrado, um link de recuperação foi gerado. Verifique sua caixa de entrada.";

  private final UserRepository users;
  private final RoleRepository roles;
  private final UserRoleRepository userRoles;
  private final RefreshTokenRepository refreshTokens;
  private final PasswordResetTokenRepository resetTokens;
  private final StudentProfileRepository profiles;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwt;
  private final AuthProperties props;
  private final SecureRandom random = new SecureRandom();

  public AuthService(
      UserRepository users,
      RoleRepository roles,
      UserRoleRepository userRoles,
      RefreshTokenRepository refreshTokens,
      PasswordResetTokenRepository resetTokens,
      StudentProfileRepository profiles,
      PasswordEncoder passwordEncoder,
      JwtService jwt,
      AuthProperties props) {
    this.users = users;
    this.roles = roles;
    this.userRoles = userRoles;
    this.refreshTokens = refreshTokens;
    this.resetTokens = resetTokens;
    this.profiles = profiles;
    this.passwordEncoder = passwordEncoder;
    this.jwt = jwt;
    this.props = props;
  }

  /** Cadastro: usuário + STUDENT + perfil mínimo + par inicial de tokens. */
  @Transactional
  public TokenResponse register(RegisterRequest req, String clientIp, String userAgent) {
    String email = normalize(req.email());
    if (users.existsByEmail(email)) {
      throw new ConflictException("EMAIL_IN_USE", "E-mail já cadastrado.");
    }
    User user = new User(email, passwordEncoder.encode(req.password()));
    try {
      user = users.save(user);
    } catch (DataIntegrityViolationException e) {
      // Corrida entre exists e insert: mantém 409 em vez de 500.
      throw new ConflictException("EMAIL_IN_USE", "E-mail já cadastrado.");
    }
    Role student =
        roles
            .findByCode(STUDENT)
            .orElseThrow(() -> new IllegalStateException("Seed de roles ausente (V2): STUDENT."));
    userRoles.save(new UserRole(user, student));

    StudentProfile profile = new StudentProfile(user, req.displayName().trim());
    if (req.schoolYear() != null && !req.schoolYear().isBlank()) {
      profile.setSchoolYear(req.schoolYear().trim());
    }
    if (req.targetYear() != null) {
      profile.setTargetYear(req.targetYear().shortValue());
    }
    if (req.studyGoal() != null && !req.studyGoal().isBlank()) {
      profile.setStudyGoal(req.studyGoal().trim());
    }
    profiles.save(profile);

    log.info("registro user_id={}", user.getId());
    return issuePair(user, List.of(STUDENT), clientIp, userAgent);
  }

  /** Login: falha sempre genérica (conta inexistente, inativa ou senha errada). */
  @Transactional
  public TokenResponse login(LoginRequest req, String clientIp, String userAgent) {
    String email = normalize(req.email());
    Optional<User> found = users.findByEmail(email);
    User user = found.orElse(null);
    if (user == null || !user.isActive()
        || !passwordEncoder.matches(req.password(), user.getPasswordHash())) {
      // Sem distinguir motivo: anti-enumeração (inclusive conta desativada).
      throw new UnauthorizedException("INVALID_CREDENTIALS", "E-mail ou senha inválidos.");
    }
    user.setLastLoginAt(OffsetDateTime.now());
    users.save(user);
    List<String> roleCodes = userRoles.findRoleCodesByUserId(user.getId());
    log.info("login user_id={}", user.getId());
    return issuePair(user, roleCodes, clientIp, userAgent);
  }

  /**
   * Refresh com rotação. Pai é revogado e encadeado ao filho; reuso de pai
   * já rotacionado revoga tudo (roubo suspeito).
   *
   * <p>{@code noRollbackFor}: as revogações (reuso, expiração, conta inativa)
   * precisam commitar mesmo quando o fluxo termina em 401 — sem isso o
   * rollback desfaria a proteção.
   */
  @Transactional(noRollbackFor = UnauthorizedException.class)
  public TokenResponse refresh(RefreshRequest req, String clientIp, String userAgent) {
    RefreshToken current =
        refreshTokens
            .findByTokenHash(TokenHash.sha256Hex(req.refreshToken()))
            .orElseThrow(
                () -> new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente."));
    OffsetDateTime now = OffsetDateTime.now();
    if (current.isRevoked()) {
      if (current.getReplacedBy() != null) {
        // Reuso de token já rotacionado: revoga a cadeia inteira.
        refreshTokens.revokeAllActiveByUserId(current.getUser().getId(), now);
        log.warn("refresh reusado (rotação) user_id={}: cadeia revogada", current.getUser().getId());
        throw new UnauthorizedException(
            "REFRESH_REUSED", "Sessão inválida por segurança. Entre novamente.");
      }
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente.");
    }
    if (current.isExpired(now)) {
      current.setRevokedAt(now);
      refreshTokens.save(current);
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão expirada. Entre novamente.");
    }
    User user = current.getUser();
    if (!user.isActive()) {
      refreshTokens.revokeAllActiveByUserId(user.getId(), OffsetDateTime.now());
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente.");
    }
    current.setRevokedAt(now);
    String raw = newOpaqueToken();
    RefreshToken next = new RefreshToken(user, TokenHash.sha256Hex(raw),
        now.plusDays(props.getRefreshTtlDays()));
    next.setCreatedIp(clientIp);
    next.setUserAgent(userAgent);
    refreshTokens.save(next);
    current.setReplacedBy(next);
    refreshTokens.save(current);
    user.setLastLoginAt(now);
    users.save(user);
    List<String> roleCodes = userRoles.findRoleCodesByUserId(user.getId());
    return buildPair(user, roleCodes, raw);
  }

  /** Logout de uma sessão: revoga o refresh (idempotente — token desconhecido também dá 200). */
  @Transactional
  public MessageResponse logout(LogoutRequest req) {
    refreshTokens
        .findByTokenHash(TokenHash.sha256Hex(req.refreshToken()))
        .ifPresent(
            t -> {
              if (!t.isRevoked()) {
                t.setRevokedAt(OffsetDateTime.now());
                refreshTokens.save(t);
              }
            });
    return new MessageResponse("Sessão encerrada.");
  }

  /** Logout global: revoga todos os refreshes do usuário. */
  @Transactional
  public MessageResponse logoutAll(long userId) {
    int revoked = refreshTokens.revokeAllActiveByUserId(userId, OffsetDateTime.now());
    log.info("logout-all user_id={} revoked={}", userId, revoked);
    return new MessageResponse("Todas as sessões foram encerradas.");
  }

  /**
   * Recuperação: emite token de uso único quando a conta existe e está ativa;
   * resposta sempre genérica. Entrega por e-mail pendente de provedor
   * transacional (architecture.md §11) — ver docs/api-auth.md.
   */
  @Transactional
  public MessageResponse forgotPassword(ForgotPasswordRequest req, String clientIp) {
    String email = normalize(req.email());
    users
        .findByEmail(email)
        .filter(User::isActive)
        .ifPresent(
            user -> {
              PasswordResetToken token =
                  new PasswordResetToken(user, TokenHash.sha256Hex(newOpaqueToken()),
                      OffsetDateTime.now().plusMinutes(props.getResetTtlMinutes()));
              token.setCreatedIp(clientIp);
              resetTokens.save(token);
              // Sem o valor do token no log: auditoria sem vazamento.
              log.info("reset emitido user_id={}", user.getId());
            });
    return new MessageResponse(GENERIC_FORGOT_MESSAGE);
  }

  /** Efetiva a recuperação: valida uso único + expiração, troca a senha e derruba sessões. */
  @Transactional
  public MessageResponse resetPassword(ResetPasswordRequest req) {
    PasswordResetToken token =
        resetTokens
            .findByTokenHash(TokenHash.sha256Hex(req.token()))
            .orElseThrow(
                () -> new BadRequestException("INVALID_OR_EXPIRED_TOKEN", "Token inválido ou expirado."));
    OffsetDateTime now = OffsetDateTime.now();
    if (token.isUsed() || token.isExpired(now)) {
      throw new BadRequestException("INVALID_OR_EXPIRED_TOKEN", "Token inválido ou expirado.");
    }
    User user = token.getUser();
    if (!user.isActive()) {
      throw new BadRequestException("INVALID_OR_EXPIRED_TOKEN", "Token inválido ou expirado.");
    }
    user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    user.bumpCredentialVersion();
    users.save(user);
    token.setUsedAt(now);
    resetTokens.save(token);
    refreshTokens.revokeAllActiveByUserId(user.getId(), OffsetDateTime.now());
    log.info("reset concluído user_id={}", user.getId());
    return new MessageResponse("Senha redefinida. Entre com a nova senha.");
  }

  /**
   * Troca autenticada: exige senha atual, incrementa versão credencial,
   * revoga refreshes e devolve par novo (segue logado).
   */
  @Transactional
  public TokenResponse changePassword(long userId, ChangePasswordRequest req, String clientIp,
      String userAgent) {
    User user =
        users
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));
    if (!user.isActive()) {
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente.");
    }
    if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException("INVALID_CURRENT_PASSWORD", "Senha atual incorreta.");
    }
    if (passwordEncoder.matches(req.newPassword(), user.getPasswordHash())) {
      throw new BadRequestException("SAME_PASSWORD", "A nova senha deve ser diferente da atual.");
    }
    user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
    user.bumpCredentialVersion();
    users.save(user);
    refreshTokens.revokeAllActiveByUserId(user.getId(), OffsetDateTime.now());
    List<String> roleCodes = userRoles.findRoleCodesByUserId(user.getId());
    log.info("senha alterada user_id={}", user.getId());
    return issuePair(user, roleCodes, clientIp, userAgent);
  }

  /** Perfil mínimo da conta autenticada (validação de token + dados para o frontend). */
  @Transactional(readOnly = true)
  public UserResponse me(long userId) {
    User user =
        users
            .findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));
    if (!user.isActive()) {
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente.");
    }
    List<String> roleCodes = userRoles.findRoleCodesByUserId(user.getId());
    String displayName =
        profiles.findById(user.getId()).map(StudentProfile::getDisplayName).orElse(user.getEmail());
    return new UserResponse(user.getId(), user.getEmail(), displayName, roleCodes);
  }

  // ---- internals ----

  private TokenResponse issuePair(User user, List<String> roleCodes, String clientIp, String userAgent) {
    // O valor opaco sai uma única vez nesta resposta; só o hash fica no banco.
    String raw = newOpaqueToken();
    RefreshToken next = new RefreshToken(user, TokenHash.sha256Hex(raw),
        OffsetDateTime.now().plusDays(props.getRefreshTtlDays()));
    next.setCreatedIp(clientIp);
    next.setUserAgent(userAgent);
    refreshTokens.save(next);
    return buildPair(user, roleCodes, raw);
  }

  private TokenResponse buildPair(User user, List<String> roleCodes, String rawRefresh) {
    String access = jwt.generateAccessToken(user.getId(), user.getEmail(), roleCodes,
        user.getCredentialVersion());
    String displayName =
        profiles.findById(user.getId()).map(StudentProfile::getDisplayName).orElse(user.getEmail());
    return new TokenResponse(
        access,
        "Bearer",
        props.getAccessTtlMinutes() * 60,
        rawRefresh,
        new UserResponse(user.getId(), user.getEmail(), displayName, roleCodes));
  }

  private String newOpaqueToken() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  static String normalize(String email) {
    return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
  }
}
