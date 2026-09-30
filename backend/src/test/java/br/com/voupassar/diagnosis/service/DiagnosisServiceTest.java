package br.com.voupassar.diagnosis.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.QuestionClassification;
import br.com.voupassar.content.entity.Subtopic;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.diagnosis.dto.DiagnosisResponse;
import br.com.voupassar.exception.UnauthorizedException;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exams.entity.Question;
import br.com.voupassar.exams.repository.DisciplineRepository;
import br.com.voupassar.exams.repository.QuestionRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DiagnosisServiceTest {

  @Mock UserRepository users;
  @Mock QuestionAttemptRepository attempts;
  @Mock QuestionClassificationRepository classifications;
  @Mock TopicRepository topics;
  @Mock DisciplineRepository disciplines;
  @Mock QuestionRepository questions;

  private DiagnosisService service;

  @BeforeEach
  void setup() {
    service = new DiagnosisService(
        users, attempts, classifications, topics, disciplines, questions);
  }

  private static User user(long id, boolean active) {
    User u = new User("a@b.c", new BCryptPasswordEncoder(4).encode("senha"));
    ReflectionTestUtils.setField(u, "id", id);
    u.setActive(active);
    return u;
  }

  private static Discipline discipline(String code, String name) {
    Discipline d = new Discipline(code, name);
    ReflectionTestUtils.setField(d, "id", 1L);
    return d;
  }

  private static Topic topic(long id, String code, String name, Discipline d) {
    Topic t = new Topic();
    ReflectionTestUtils.setField(t, "id", id);
    ReflectionTestUtils.setField(t, "code", code);
    ReflectionTestUtils.setField(t, "name", name);
    ReflectionTestUtils.setField(t, "discipline", d);
    return t;
  }

  private static Subtopic subtopic(long id, String code, String name, Topic t) {
    Subtopic s = new Subtopic();
    ReflectionTestUtils.setField(s, "id", id);
    ReflectionTestUtils.setField(s, "code", code);
    ReflectionTestUtils.setField(s, "name", name);
    ReflectionTestUtils.setField(s, "topic", t);
    return s;
  }

  private static Question question(long id, Discipline d) {
    Question q = new Question();
    ReflectionTestUtils.setField(q, "id", id);
    ReflectionTestUtils.setField(q, "discipline", d);
    return q;
  }

  private static QuestionAttempt attempt(
      long id, User u, Question q, boolean annulled, Boolean correct, String answeredAt) {
    QuestionAttempt a = new QuestionAttempt();
    ReflectionTestUtils.setField(a, "id", id);
    a.setUser(u);
    a.setQuestion(q);
    a.setAnnulled(annulled);
    a.setCorrect(correct);
    a.setMode("ESTUDO");
    a.setSelectedOption("A");
    a.setAnsweredAt(OffsetDateTime.parse(answeredAt));
    return a;
  }

  private static QuestionClassification classification(
      long id, Question q, Topic t, Subtopic s) {
    QuestionClassification c = new QuestionClassification();
    ReflectionTestUtils.setField(c, "id", id);
    ReflectionTestUtils.setField(c, "question", q);
    ReflectionTestUtils.setField(c, "topic", t);
    ReflectionTestUtils.setField(c, "subtopic", s);
    ReflectionTestUtils.setField(c, "confidence", "ALTA");
    ReflectionTestUtils.setField(c, "status", "PENDING");
    return c;
  }

  private void activeUser() {
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(user(1L, true)));
  }

  @Test
  void emptyIsUnknown() {
    activeUser();
    Mockito.when(attempts.findAllByUserIdWithQuestion(1L)).thenReturn(List.of());
    Mockito.when(topics.findAllOrdered()).thenReturn(List.of());
    Mockito.when(classifications.countByTopic()).thenReturn(List.of());
    Mockito.when(classifications.countClassified()).thenReturn(0L);
    Mockito.when(disciplines.findAllByOrderByCodeAsc()).thenReturn(List.of());

    DiagnosisResponse out = service.getDiagnosis(1L);

    assertEquals(0L, out.totalAttempts());
    assertNull(out.accuracy());
    assertEquals("DESCONHECIDO", out.overallLevel());
    assertTrue(out.strengths().isEmpty());
    assertTrue(out.gaps().isEmpty());
  }

  @Test
  void masteryLevelsAreDeterministic() {
    assertEquals("NAO_AVALIADO", DiagnosisService.masteryLevel(0, null));
    assertEquals("NAO_AVALIADO", DiagnosisService.masteryLevel(0, 0.8));
    assertEquals("EM_OBSERVACAO", DiagnosisService.masteryLevel(1, 0.8));
    assertEquals("EM_OBSERVACAO", DiagnosisService.masteryLevel(2, 0.9));
    assertEquals("FRAGIL", DiagnosisService.masteryLevel(3, 0.3));
    assertEquals("FRAGIL", DiagnosisService.masteryLevel(5, 0.4));
    assertEquals("DOMINADO", DiagnosisService.masteryLevel(4, 0.75));
    assertEquals("DOMINADO", DiagnosisService.masteryLevel(10, 1.0));
    assertEquals("EM_DESENVOLVIMENTO", DiagnosisService.masteryLevel(3, 0.5));
    assertEquals("EM_DESENVOLVIMENTO", DiagnosisService.masteryLevel(3, 0.69));
  }

  @Test
  void inactiveIs401() {
    Mockito.when(users.findById(1L)).thenReturn(Optional.of(user(1L, false)));
    assertThrows(UnauthorizedException.class, () -> service.getDiagnosis(1L));
  }
}
