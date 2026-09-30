package br.com.voupassar.profile.service;

import br.com.voupassar.auth.entity.StudentProfile;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.StudentProfileRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.auth.repository.UserRoleRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.profile.dto.AttemptHistoryResponse;
import br.com.voupassar.profile.dto.ProfileResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse.DisciplineStatResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse.ModeStatResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse.TopicProgressResponse;
import br.com.voupassar.profile.dto.UpdateProfileRequest;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.entity.StudentTopicPerformance;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.questions.dto.PageResponse;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Perfil do aluno (TASK 3.6): dados, preferências, estatísticas, progresso e
 * histórico — tudo escopado ao dono do token.
 *
 * <p>Regras de evidência:
 * <ul>
 *   <li>Perfil: mesmos limites do registro (TASK 3.5); em-branco vira NULL
 *       nos opcionais; e-mail/senha não mudam aqui.</li>
 *   <li>Estatísticas derivam só de {@code question_attempts} (fato imutável)
 *       + {@code student_topic_performance} (agregado da Fase 4, ainda vazio).
 *       Nenhum número é inventado: sem tentativas, totais zeram e
 *       {@code accuracy} sai NULL com nota explícita.</li>
 *   <li>Anuladas contam como conteúdo respondido e ficam fora do
 *       aproveitamento (pontuação DESCONHECIDA, TASK 1.3 §4).</li>
 *   <li>Histórico é paginado, mais recentes primeiro, sem enunciado nem
 *       gabarito (isso é a TASK 3.4).</li>
 * </ul>
 */
@Service
public class ProfileService {

  /** Teto de página: mesmo da TASK 3.4 (protege banco e VPS modesta). */
  public static final int MAX_SIZE = 100;

  private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

  private final UserRepository users;
  private final StudentProfileRepository profiles;
  private final UserRoleRepository userRoles;
  private final QuestionAttemptRepository attempts;
  private final StudentTopicPerformanceRepository performance;

  public ProfileService(
      UserRepository users,
      StudentProfileRepository profiles,
      UserRoleRepository userRoles,
      QuestionAttemptRepository attempts,
      StudentTopicPerformanceRepository performance) {
    this.users = users;
    this.profiles = profiles;
    this.userRoles = userRoles;
    this.attempts = attempts;
    this.performance = performance;
  }

  /** Perfil completo do dono do token. */
  @Transactional(readOnly = true)
  public ProfileResponse getProfile(long userId) {
    User user = requireActiveUser(userId);
    StudentProfile profile = requireProfile(userId);
    return toProfile(user, profile, userRoles.findRoleCodesByUserId(userId));
  }

  /** Atualiza nome/preferências do dono do token (PUT completo). */
  @Transactional
  public ProfileResponse updateProfile(long userId, UpdateProfileRequest req) {
    User user = requireActiveUser(userId);
    StudentProfile profile = requireProfile(userId);

    profile.setDisplayName(req.displayName().trim());
    profile.setSchoolYear(normalizeOptional(req.schoolYear()));
    profile.setTargetYear(req.targetYear() == null ? null : req.targetYear().shortValue());
    profile.setStudyGoal(normalizeOptional(req.studyGoal()));
    profiles.save(profile);

    log.info("perfil atualizado user_id={}", userId);
    return toProfile(user, profile, userRoles.findRoleCodesByUserId(userId));
  }

  /** Estatísticas + progresso do dono do token (só leitura). */
  @Transactional(readOnly = true)
  public ProfileStatsResponse getStats(long userId) {
    requireActiveUser(userId);

    long total = attempts.countByUserId(userId);
    long scored = attempts.countByUserIdAndAnnulledFalse(userId);
    long correct = attempts.countByUserIdAndAnnulledFalseAndCorrectTrue(userId);
    long annulled = attempts.countByUserIdAndAnnulledTrue(userId);
    long incorrect = Math.max(0L, scored - correct);
    Double accuracy = scored == 0 ? null : correct / (double) scored;
    Optional<OffsetDateTime> last = attempts.lastAttemptAt(userId);

    List<ModeStatResponse> byMode = new ArrayList<>();
    for (Object[] row : attempts.statsByMode(userId)) {
      long rowTotal = toLong(row[1]);
      long rowScored = toLong(row[2]);
      long rowCorrect = toLong(row[3]);
      byMode.add(new ModeStatResponse(
          String.valueOf(row[0]), rowTotal, rowScored, rowCorrect,
          rowScored == 0 ? null : toLong(row[3]) / (double) rowScored));
    }

    List<DisciplineStatResponse> byDiscipline = new ArrayList<>();
    for (Object[] row : attempts.statsByDiscipline(userId)) {
      long rowTotal = toLong(row[2]);
      long rowScored = toLong(row[3]);
      long rowCorrect = toLong(row[4]);
      byDiscipline.add(new DisciplineStatResponse(
          String.valueOf(row[0]), String.valueOf(row[1]),
          rowTotal, rowScored, rowCorrect,
          rowScored == 0 ? null : rowCorrect / (double) rowScored));
    }

    List<TopicProgressResponse> progress = new ArrayList<>();
    for (StudentTopicPerformance p : performance.findProgressByUserId(userId)) {
      progress.add(new TopicProgressResponse(
          p.getTopic().getCode(),
          p.getTopic().getName(),
          p.getTopic().getDiscipline().getCode(),
          p.getSubtopic() == null ? null : p.getSubtopic().getCode(),
          p.getSubtopic() == null ? null : p.getSubtopic().getName(),
          p.getAttempts(),
          p.getHits(),
          p.getAccuracy(),
          p.getLastAttemptAt()));
    }

    List<String> notes = new ArrayList<>();
    if (total == 0) {
      notes.add("Nenhuma tentativa registrada: aproveitamento ainda DESCONHECIDO (responda questões ou faça um simulado).");
    }
    if (annulled > 0) {
      notes.add("Anuladas contam como conteúdo respondido e ficam fora do aproveitamento; regra de pontuação DESCONHECIDA.");
    }
    if (progress.isEmpty()) {
      notes.add("Progresso por conteúdo ainda sem dados: o agregado student_topic_performance é calculado na TASK 4.1.");
    }
    notes.add("Diagnóstico, roteiro e recomendações entram na Fase 4 — aqui só fatos registrados.");

    return new ProfileStatsResponse(
        total, scored, correct, incorrect, annulled, accuracy,
        last.orElse(null), List.copyOf(byMode), List.copyOf(byDiscipline),
        List.copyOf(progress), List.copyOf(notes));
  }

  /**
   * Histórico paginado de tentativas do dono do token (mais recentes
   * primeiro).
   *
   * @param page 0-based
   * @param size itens por página {@code [1, 100]}
   */
  @Transactional(readOnly = true)
  public PageResponse<AttemptHistoryResponse> getHistory(long userId, int page, int size) {
    requireActiveUser(userId);
    if (page < 0) {
      throw new BadRequestException("Página inválida: " + page + " (0-based).");
    }
    if (size < 1 || size > MAX_SIZE) {
      throw new BadRequestException(
          "Tamanho de página inválido: " + size + " (permitido 1–" + MAX_SIZE + ").");
    }
    Page<QuestionAttempt> result = attempts.findHistory(userId, PageRequest.of(page, size));
    List<AttemptHistoryResponse> content = new ArrayList<>(result.getNumberOfElements());
    for (QuestionAttempt a : result) {
      content.add(toHistory(a));
    }
    return new PageResponse<>(
        List.copyOf(content),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.isFirst(),
        result.isLast());
  }

  // ---- internals ----

  private User requireActiveUser(long userId) {
    User user = users.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Conta não encontrada."));
    if (!user.isActive()) {
      throw new UnauthorizedException("INVALID_REFRESH_TOKEN", "Sessão inválida. Entre novamente.");
    }
    return user;
  }

  private StudentProfile requireProfile(long userId) {
    return profiles.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "PROFILE_NOT_FOUND", "Perfil não encontrado (conta sem student_profiles — NECESSITA REVISÃO)."));
  }

  private static ProfileResponse toProfile(User user, StudentProfile p, List<String> roles) {
    return new ProfileResponse(
        user.getId(),
        user.getEmail(),
        p.getDisplayName(),
        p.getSchoolYear(),
        p.getTargetYear() == null ? null : p.getTargetYear().intValue(),
        p.getStudyGoal(),
        List.copyOf(roles));
  }

  private static AttemptHistoryResponse toHistory(QuestionAttempt a) {
    return new AttemptHistoryResponse(
        a.getId(),
        a.getQuestion().getId(),
        a.getQuestion().getSourceYear() == null ? null : a.getQuestion().getSourceYear().intValue(),
        a.getQuestion().getSourceQuestionNumber() == null
            ? null : a.getQuestion().getSourceQuestionNumber().intValue(),
        a.getQuestion().getDiscipline().getCode(),
        a.getQuestion().getDiscipline().getName(),
        a.getSelectedOption(),
        a.getCorrect(),
        a.isAnnulled(),
        a.getMode(),
        a.getTimeSpentSeconds(),
        a.getStudySessionId(),
        a.getSimulationAttemptId(),
        a.getAnsweredAt());
  }

  private static String normalizeOptional(String v) {
    if (v == null || v.isBlank()) {
      return null;
    }
    return v.trim();
  }

  private static long toLong(Object v) {
    return v == null ? 0L : ((Number) v).longValue();
  }
}
