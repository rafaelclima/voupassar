package br.com.voupassar.studyplan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.profile.entity.QuestionAttempt;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.studyplan.dto.StudyPlanItemResponse;
import br.com.voupassar.studyplan.dto.StudyPlanResponse;
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
import org.springframework.security.access.AccessDeniedException;
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

    StudyPlanResponse plan = service.generatePlan(1L);

    assertNotNull(plan);
    assertEquals(1L, plan.userId());
    assertEquals("v1-deterministico", plan.algorithmVersion());
    assertTrue(plan.isActive());
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

    StudyPlanResponse plan = service.generatePlan(1L);

    assertNotNull(plan);
    assertEquals(1L, plan.userId());
  }

  @Test
  void generatePlanReturnsDtoWithoutEntityLeak() {
    User user = new User("teste@test.com", "hash");
    ReflectionTestUtils.setField(user, "id", 1L);
    when(users.findById(1L)).thenReturn(Optional.of(user));
    when(studyPlans.findByUserIdAndIsActiveTrue(1L)).thenReturn(Optional.empty());
    when(topics.findAllOrdered()).thenReturn(Collections.emptyList());

    StudyPlanResponse plan = service.generatePlan(1L);

    // DTO: sem proxy JPA, itens como lista de DTOs (vazia aqui).
    assertNotNull(plan.items());
    assertTrue(plan.items().isEmpty());
  }

  @Test
  void getPlanWithoutActivePlanThrows404() {
    // Regressão TASK 6.4: IllegalArgumentException caía no handler genérico
    // e GET /recommendations/plan devolvia 500 em vez do 404 documentado.
    when(studyPlans.findByUserIdAndIsActiveTrue(9L)).thenReturn(Optional.empty());

    ResourceNotFoundException ex =
        assertThrows(ResourceNotFoundException.class, () -> service.getPlan(9L));
    assertEquals("NO_ACTIVE_PLAN", ex.getCode());
  }

  @Test
  void updateItemStatusInvalidThrows400() {
    // Status fora de TODO/DOING/DONE/SKIPPED violava o CHECK do banco (500).
    BadRequestException ex = assertThrows(
        BadRequestException.class, () -> service.updateItemStatus(1L, "CONCLUIDO", 1L));
    assertEquals("INVALID_STATUS", ex.getCode());
    verifyNoInteractions(studyPlanItems);
  }

  @Test
  void updateItemStatusMissingItemThrows404() {
    when(studyPlanItems.findById(404L)).thenReturn(Optional.empty());

    ResourceNotFoundException ex = assertThrows(
        ResourceNotFoundException.class, () -> service.updateItemStatus(404L, "DONE", 1L));
    assertEquals("ITEM_NOT_FOUND", ex.getCode());
  }

  @Test
  void updateItemStatusDoneSucceedsForOwner() {
    StudyPlan plan = new StudyPlan(1L, "v1-deterministico");
    StudyPlanItem item = new StudyPlanItem(plan, 5L, null, (short) 1, "motivo", "{}");
    when(studyPlanItems.findById(11L)).thenReturn(Optional.of(item));
    when(studyPlanItems.save(item)).thenReturn(item);

    StudyPlanItemResponse out = service.updateItemStatus(11L, "DONE", 1L);

    assertEquals("DONE", out.status());
  }

  @Test
  void updateItemStatusOfAnotherStudentIs403() {
    // TASK 16.1 (P1 — IDOR): item do plano de B atualizado por A → 403.
    StudyPlan planOfB = new StudyPlan(2L, "v1-deterministico");
    StudyPlanItem itemOfB = new StudyPlanItem(planOfB, 5L, null, (short) 1, "motivo", "{}");
    when(studyPlanItems.findById(11L)).thenReturn(Optional.of(itemOfB));

    assertThrows(AccessDeniedException.class,
        () -> service.updateItemStatus(11L, "DONE", 1L));
    verify(studyPlanItems, never()).save(any());
  }
}
