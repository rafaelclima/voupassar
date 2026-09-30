package br.com.voupassar.studyplan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.studyplan.entity.StudyPlan;
import br.com.voupassar.studyplan.entity.StudyPlanItem;
import br.com.voupassar.studyplan.repository.StudyPlanItemRepository;
import br.com.voupassar.studyplan.repository.StudyPlanRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

  @Mock UserRepository users;
  @Mock StudyPlanRepository studyPlans;
  @Mock StudyPlanItemRepository studyPlanItems;
  @Mock TopicRepository topics;
  @Mock br.com.voupassar.content.repository.SubtopicRepository subtopics;
  @Mock QuestionAttemptRepository attempts;
  @Mock StudentTopicPerformanceRepository performanceRepo;

  private RecommendationService service;

  @BeforeEach
  void setup() {
    service = new RecommendationService(
        users, studyPlans, studyPlanItems, topics, subtopics, attempts, performanceRepo);
  }

  @Test
  void generatePlanCreatesNewPlanWhenNoneExists() {
    User user = new User("teste@test.com", "hash");
    ReflectionTestUtils.setField(user, "id", 1L);
    when(users.findById(1L)).thenReturn(Optional.of(user));
    when(studyPlans.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
    when(topics.findAllOrdered()).thenReturn(Collections.emptyList());

    StudyPlan plan = service.generatePlan(1L);

    assertNotNull(plan);
    assertEquals(1L, plan.getUserId());
    assertEquals("v1-deterministico", plan.getAlgorithmVersion());
    assertTrue(plan.getIsActive());
  }

  @Test
  void generatePlanDeactivatesPreviousPlan() {
    User user = new User("teste@test.com", "hash");
    ReflectionTestUtils.setField(user, "id", 2L);
    StudyPlan oldPlan = new StudyPlan(2L, "v0");
    oldPlan.setIsActive(true);

    when(users.findById(2L)).thenReturn(Optional.of(user));
    when(studyPlans.findByUserIdAndIsActiveTrue(2L)).thenReturn(Optional.of(oldPlan));
    when(topics.findAllOrdered()).thenReturn(Collections.emptyList());

    service.generatePlan(2L);
    assertFalse(oldPlan.getIsActive());
  }

  @Test
  void generatePlanCreatesPlanSuccessfully() {
    User user = new User("teste@test.com", "hash");
    ReflectionTestUtils.setField(user, "id", 1L);
    when(users.findById(1L)).thenReturn(Optional.of(user));
    when(studyPlans.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
    when(topics.findAllOrdered()).thenReturn(Collections.emptyList());

    StudyPlan plan = service.generatePlan(1L);

    assertNotNull(plan);
    assertEquals(1L, plan.getUserId());
  }
}
