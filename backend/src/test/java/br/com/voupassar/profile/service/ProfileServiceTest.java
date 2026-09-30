package br.com.voupassar.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.voupassar.auth.entity.StudentProfile;
import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.StudentProfileRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.auth.repository.UserRoleRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.profile.dto.AttemptHistoryResponse;
import br.com.voupassar.profile.dto.ProfileStatsResponse;
import br.com.voupassar.profile.dto.UpdateProfileRequest;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.questions.dto.PageResponse;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Regras do ProfileService (TASK 3.6) com mocks — sem banco.
 */
@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

  @Mock UserRepository users;
  @Mock StudentProfileRepository profiles;
  @Mock UserRoleRepository userRoles;
  @Mock QuestionAttemptRepository attempts;
  @Mock StudentTopicPerformanceRepository performance;

  private ProfileService service;

  @BeforeEach
  void setup() {
    service = new ProfileService(users, profiles, userRoles, attempts, performance);
  }

  private static User user(long id, boolean active) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha-segura-123"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(active);
    return u;
  }

  private static StudentProfile profile(User u, String display) {
    StudentProfile p = new StudentProfile(u, display);
    return p;
  }

  // ---- perfil ----

  @Test
  void getProfileMapsAllFields() {
    User u = user(1L, true);
    StudentProfile p = profile(u, "Aluno");
    p.setSchoolYear("9º ano");
    p.setTargetYear((short) 2027);
    p.setStudyGoal("Passar");
    when(users.findById(1L)).thenReturn(Optional.of(u));
    when(profiles.findById(1L)).thenReturn(Optional.of(p));
    when(userRoles.findRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));

    var out = service.getProfile(1L);

    assertEquals(1L, out.id());
    assertEquals("a@b.c", out.email());
    assertEquals("Aluno", out.displayName());
    assertEquals("9º ano", out.schoolYear());
    assertEquals(2027, out.targetYear());
    assertEquals("Passar", out.studyGoal());
    assertEquals(List.of("STUDENT"), out.roles());
  }

  @Test
  void getProfileInactiveIs401AndMissingProfileIs404() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, false)));
    assertThrows(UnauthorizedException.class, () -> service.getProfile(1L));

    when(users.findById(2L)).thenReturn(Optional.of(user(2L, true)));
    when(profiles.findById(2L)).thenReturn(Optional.empty());
    assertThrows(ResourceNotFoundException.class, () -> service.getProfile(2L));
  }

  @Test
  void updateProfileTrimsAndNullifiesBlanks() {
    User u = user(1L, true);
    StudentProfile p = profile(u, "Antigo");
    when(users.findById(1L)).thenReturn(Optional.of(u));
    when(profiles.findById(1L)).thenReturn(Optional.of(p));
    when(userRoles.findRoleCodesByUserId(1L)).thenReturn(List.of("STUDENT"));

    var out = service.updateProfile(
        1L, new UpdateProfileRequest("  Novo Nome  ", "   ", 2028, ""));

    assertEquals("Novo Nome", out.displayName());
    assertNull(out.schoolYear());
    assertEquals(2028, out.targetYear());
    assertNull(out.studyGoal());
    verify(profiles).save(any(StudentProfile.class));
  }

  // ---- stats ----

  @Test
  void statsEmptyIsZeroWithNullAccuracyAndNotes() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
    when(attempts.countByUserId(1L)).thenReturn(0L);
    when(attempts.countByUserIdAndAnnulledFalse(1L)).thenReturn(0L);
    when(attempts.countByUserIdAndAnnulledFalseAndCorrectTrue(1L)).thenReturn(0L);
    when(attempts.countByUserIdAndAnnulledTrue(1L)).thenReturn(0L);
    when(attempts.statsByMode(1L)).thenReturn(List.<Object[]>of());
    when(attempts.statsByDiscipline(1L)).thenReturn(List.<Object[]>of());
    when(attempts.lastAttemptAt(1L)).thenReturn(Optional.empty());
    when(performance.findProgressByUserId(1L)).thenReturn(List.of());

    ProfileStatsResponse stats = service.getStats(1L);

    assertEquals(0L, stats.totalAttempts());
    assertNull(stats.accuracy());
    assertNull(stats.lastAttemptAt());
    assertTrue(stats.byMode().isEmpty());
    assertTrue(stats.topicProgress().isEmpty());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("Nenhuma tentativa")));
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("4.1")));
  }

  @Test
  void statsAggregatesExcludeAnnulledFromAccuracy() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
    when(attempts.countByUserId(1L)).thenReturn(5L);
    when(attempts.countByUserIdAndAnnulledFalse(1L)).thenReturn(4L);
    when(attempts.countByUserIdAndAnnulledFalseAndCorrectTrue(1L)).thenReturn(3L);
    when(attempts.countByUserIdAndAnnulledTrue(1L)).thenReturn(1L);
    when(attempts.statsByMode(1L)).thenReturn(
        List.<Object[]>of(new Object[] {"ESTUDO", 5L, 4L, 3L, 1L}));
    when(attempts.statsByDiscipline(1L)).thenReturn(
        List.<Object[]>of(new Object[] {"MATEMATICA", "Matemática", 5L, 4L, 3L, 1L}));
    when(attempts.lastAttemptAt(1L))
        .thenReturn(Optional.of(OffsetDateTime.parse("2026-09-01T10:00:00Z")));
    when(performance.findProgressByUserId(1L)).thenReturn(List.of());

    ProfileStatsResponse stats = service.getStats(1L);

    assertEquals(5L, stats.totalAttempts());
    assertEquals(4L, stats.scoredAttempts());
    assertEquals(3L, stats.correct());
    assertEquals(1L, stats.incorrect());
    assertEquals(1L, stats.annulled());
    assertEquals(0.75, stats.accuracy());
    assertNotNull(stats.lastAttemptAt());
    assertEquals(1, stats.byMode().size());
    assertEquals(0.75, stats.byMode().get(0).accuracy());
    assertEquals("MATEMATICA", stats.byDiscipline().get(0).disciplineCode());
    assertTrue(stats.notes().stream().anyMatch(n -> n.contains("Anuladas")));
  }

  // ---- history ----

  @Test
  void historyRejectsBadPagination() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));

    assertThrows(BadRequestException.class, () -> service.getHistory(1L, -1, 20));
    assertThrows(BadRequestException.class, () -> service.getHistory(1L, 0, 0));
    assertThrows(BadRequestException.class, () -> service.getHistory(1L, 0, 101));
  }

  @Test
  void historyMapsProvenanceWithoutLeakingStatement() {
    when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));

    Discipline d = new Discipline("MATEMATICA", "Matemática");
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", 12L);
    ReflectionTestUtils.setField(q, "discipline", d);
    ReflectionTestUtils.setField(q, "sourceYear", (short) 2026);
    ReflectionTestUtils.setField(q, "sourceQuestionNumber", (short) 17);

    QuestionAttempt a = new QuestionAttempt();
    ReflectionTestUtils.setField(a, "id", 101L);
    ReflectionTestUtils.setField(a, "question", q);
    ReflectionTestUtils.setField(a, "selectedOption", "C");
    ReflectionTestUtils.setField(a, "correct", Boolean.TRUE);
    ReflectionTestUtils.setField(a, "annulled", false);
    ReflectionTestUtils.setField(a, "mode", "ESTUDO");
    ReflectionTestUtils.setField(a, "timeSpentSeconds", 95);
    ReflectionTestUtils.setField(a, "answeredAt", OffsetDateTime.parse("2026-09-01T10:00:00Z"));

    when(attempts.findHistory(any(), any()))
        .thenReturn(new PageImpl<>(List.of(a), PageRequest.of(0, 20), 1));

    PageResponse<AttemptHistoryResponse> page = service.getHistory(1L, 0, 20);

    assertEquals(1L, page.totalElements());
    AttemptHistoryResponse item = page.content().get(0);
    assertEquals(101L, item.id());
    assertEquals(12L, item.questionId());
    assertEquals(2026, item.sourceYear());
    assertEquals(17, item.sourceNumber());
    assertEquals("MATEMATICA", item.disciplineCode());
    assertEquals("C", item.selectedOption());
    assertEquals(Boolean.TRUE, item.isCorrect());
  }
}
