package br.com.voupassar.studyplan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import br.com.voupassar.auth.entity.User;
import br.com.voupassar.auth.repository.StudentProfileRepository;
import br.com.voupassar.auth.repository.UserRepository;
import br.com.voupassar.content.entity.Topic;
import br.com.voupassar.content.repository.QuestionClassificationRepository;
import br.com.voupassar.content.repository.TopicRepository;
import br.com.voupassar.exams.entity.Discipline;
import br.com.voupassar.exception.BadRequestException;
import br.com.voupassar.exception.ResourceNotFoundException;
import br.com.voupassar.profile.entity.StudentTopicPerformance;
import br.com.voupassar.profile.repository.QuestionAttemptRepository;
import br.com.voupassar.profile.repository.StudentTopicPerformanceRepository;
import br.com.voupassar.studyplan.dto.StudyPlanItemResponse;
import br.com.voupassar.studyplan.dto.StudyPlanResponse;
import br.com.voupassar.studyplan.entity.StudyPlan;
import br.com.voupassar.studyplan.entity.StudyPlanItem;
import br.com.voupassar.studyplan.repository.StudyPlanItemRepository;
import br.com.voupassar.studyplan.repository.StudyPlanRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
  @Mock QuestionClassificationRepository classifications;
  @Mock StudentProfileRepository profiles;

  private RecommendationService service;

  @BeforeEach
  void setup() {
    service = new RecommendationService(
        users, studyPlans, studyPlanItems, topics, subtopics, attempts,
        performanceRepo, classifications, profiles);
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
    assertEquals("v2-deterministico", plan.algorithmVersion());
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

  // ---- TASK 18.1 (score v2) ----

  private static Discipline discipline(String code) {
    Discipline d = new Discipline(code, code);
    ReflectionTestUtils.setField(d, "id", 1L);
    return d;
  }

  private static Topic topic(long id, String code, Discipline d) {
    Topic t = new Topic();
    ReflectionTestUtils.setField(t, "id", id);
    ReflectionTestUtils.setField(t, "code", code);
    ReflectionTestUtils.setField(t, "name", code);
    ReflectionTestUtils.setField(t, "discipline", d);
    return t;
  }

  private static StudentTopicPerformance perf(Topic t, int attempts, String accuracy, OffsetDateTime last) {
    StudentTopicPerformance p = mock(StudentTopicPerformance.class);
    when(p.getTopic()).thenReturn(t);
    when(p.getAttempts()).thenReturn(attempts);
    when(p.getAccuracy()).thenReturn(accuracy == null ? null : new BigDecimal(accuracy));
    when(p.getLastAttemptAt()).thenReturn(last);
    return p;
  }

  private void stubUserWithPlan(long userId) {
    User user = new User("v2@test.com", "hash");
    ReflectionTestUtils.setField(user, "id", userId);
    when(users.findById(userId)).thenReturn(Optional.of(user));
    when(studyPlans.findByUserIdAndIsActiveTrue(userId)).thenReturn(Optional.empty());
  }

  @Test
  void frequentWeakOutranksRareWeak() {
    // Tudo o mais igual (5 tentativas, 20%, há 10 dias, só FACIL):
    // o frequente (70 questões/6 edições) vem antes do raro (5/1).
    Discipline d = discipline("MAT");
    Topic freq = topic(1L, "FREQUENTE", d);
    Topic rare = topic(2L, "RARO", d);
    stubUserWithPlan(1L);
    when(topics.findAllOrdered()).thenReturn(List.of(freq, rare));
    OffsetDateTime last = OffsetDateTime.now().minusDays(10);
    StudentTopicPerformance perfFreq = perf(freq, 5, "0.2", last);
    StudentTopicPerformance perfRare = perf(rare, 5, "0.2", last);
    when(performanceRepo.findProgressByUserId(1L)).thenReturn(List.of(perfFreq, perfRare));
    when(classifications.countByTopic()).thenReturn(List.<Object[]>of(
        new Object[] {1L, 70L}, new Object[] {2L, 5L}));
    when(classifications.editionsByTopic(1L)).thenReturn(List.of((short) 2020, (short) 2022, (short) 2023, (short) 2024, (short) 2025, (short) 2026));
    when(classifications.editionsByTopic(2L)).thenReturn(List.of((short) 2026));
    when(classifications.difficultyByTopic()).thenReturn(List.<Object[]>of(
        new Object[] {1L, "FACIL", 10L}, new Object[] {2L, "FACIL", 10L}));

    StudyPlanResponse plan = service.generatePlan(1L);

    assertEquals("v2-deterministico", plan.algorithmVersion());
    assertEquals(2, plan.items().size());
    assertEquals(1L, plan.items().get(0).topicId());
    assertEquals(2L, plan.items().get(1).topicId());
    assertEquals((short) 1, plan.items().get(0).priority());
    assertTrue(plan.items().get(0).reason().contains("70 questão(ões)"));
  }

  @Test
  void gapsOrderedByFrequency() {
    // Sem tentativas: lacunas ordenadas só pela frequência (piso 0.9 + 0.1×freq).
    Discipline d = discipline("LP");
    Topic freq = topic(1L, "FREQUENTE", d);
    Topic rare = topic(2L, "RARO", d);
    stubUserWithPlan(1L);
    when(topics.findAllOrdered()).thenReturn(List.of(rare, freq));
    when(performanceRepo.findProgressByUserId(1L)).thenReturn(List.of());
    when(classifications.countByTopic()).thenReturn(List.<Object[]>of(
        new Object[] {1L, 70L}, new Object[] {2L, 5L}));
    when(classifications.editionsByTopic(1L)).thenReturn(List.of((short) 2024, (short) 2025, (short) 2026));
    when(classifications.editionsByTopic(2L)).thenReturn(List.of((short) 2026));
    when(classifications.difficultyByTopic()).thenReturn(List.of());

    StudyPlanResponse plan = service.generatePlan(1L);

    assertEquals(1L, plan.items().get(0).topicId());
    assertEquals(2L, plan.items().get(1).topicId());
    assertEquals((short) 1, plan.items().get(0).priority());
  }

  @Test
  void bandingRankZeroIsP1() {
    // Regressão: v1 dava P5 ao melhor rank (fórmula invertida); v2 dá P1.
    Discipline d = discipline("MAT");
    Topic a = topic(1L, "A", d);
    Topic b = topic(2L, "B", d);
    Topic c = topic(3L, "C", d);
    Topic e = topic(4L, "D", d);
    Topic f = topic(5L, "E", d);
    Topic g = topic(6L, "F", d);
    stubUserWithPlan(1L);
    when(topics.findAllOrdered()).thenReturn(List.of(a, b, c, e, f, g));
    when(performanceRepo.findProgressByUserId(1L)).thenReturn(List.of());
    when(classifications.countByTopic()).thenReturn(List.<Object[]>of(
        new Object[] {1L, 60L}, new Object[] {2L, 50L}, new Object[] {3L, 40L},
        new Object[] {4L, 30L}, new Object[] {5L, 20L}, new Object[] {6L, 10L}));
    for (long id = 1; id <= 6; id++) {
      when(classifications.editionsByTopic(id)).thenReturn(List.of((short) 2026));
    }
    when(classifications.difficultyByTopic()).thenReturn(List.of());

    StudyPlanResponse plan = service.generatePlan(1L);

    assertEquals((short) 1, plan.items().get(0).priority());
    short prev = 0;
    for (var item : plan.items()) {
      assertTrue(item.priority() >= prev, "prioridade monotônica por rank");
      prev = item.priority();
    }
    assertEquals((short) 5, plan.items().get(5).priority());
  }

  @Test
  void factorHelpers() {
    OffsetDateTime now = OffsetDateTime.now();
    // Frequência: 70% contagem + 30% amplitude.
    assertEquals(1.0, RecommendationService.frequencyFactor(70L, 70L, 6, 6), 1e-9);
    assertEquals(0.37, RecommendationService.frequencyFactor(7L, 70L, 6, 6), 1e-9);
    assertEquals(0.07, RecommendationService.frequencyFactor(7L, 70L, 0, 1), 1e-9);
    assertEquals(0.0, RecommendationService.frequencyFactor(0L, 0L, 0, 0), 1e-9);
    // Recência: neutra sem sinal; +dias/90 com teto 1.5.
    assertEquals(1.0, RecommendationService.recencyFactor(null, now), 1e-9);
    assertEquals(1.0, RecommendationService.recencyFactor(now, now), 1e-9);
    assertEquals(1.5, RecommendationService.recencyFactor(now.minusDays(45), now), 1e-9);
    assertEquals(1.5, RecommendationService.recencyFactor(now.minusDays(900), now), 1e-9);
    assertEquals(1.0, RecommendationService.recencyFactor(now.plusDays(3), now), 1e-9);
    // Dificuldade: neutra sem sinal; 1 + 0.5 × (MEDIA+DIFICIL)/total.
    assertEquals(1.0, RecommendationService.difficultyFactor(null), 1e-9);
    assertEquals(1.0, RecommendationService.difficultyFactor(new long[] {10, 0, 0}), 1e-9);
    assertEquals(1.5, RecommendationService.difficultyFactor(new long[] {0, 7, 3}), 1e-9);
    assertEquals(1.25, RecommendationService.difficultyFactor(new long[] {5, 5, 0}), 1e-9);
    // Carga: urgência × meta (só banding).
    int year = now.getYear();
    assertEquals(1.0, RecommendationService.loadMultiplier(null, null, year), 1e-9);
    assertEquals(1.2, RecommendationService.loadMultiplier((short) year, null, year), 1e-9);
    assertEquals(1.1, RecommendationService.loadMultiplier((short) (year + 1), null, year), 1e-9);
    assertEquals(1.0, RecommendationService.loadMultiplier((short) (year + 5), null, year), 1e-9);
    assertEquals(1.26, RecommendationService.loadMultiplier((short) year, "passar no IFRN", year), 1e-9);
    // Bandas: quintis ÷ carga.
    assertEquals(2, RecommendationService.priorityBandSize(10, 1.0));
    assertEquals(1, RecommendationService.priorityBandSize(0, 1.0));
    assertEquals(1, RecommendationService.priorityBandSize(6, 1.0));
  }
}
