package br.com.voupassar.studyplan.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import java.time.OffsetDateTime;

@Entity
@Table(name = "study_plan_items")
public class StudyPlanItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Ignorado no JSON: o plano já carrega os itens (plan -> items).
  // Sem isso a serialização entra em recursão infinita
  // (plan.items[].studyPlan.items...) e GET/POST do roteiro retornam 500.
  // Detectado ao vivo na TASK 6.4 (dashboard).
  @JsonIgnore
  @ManyToOne
  @JoinColumn(name = "study_plan_id", nullable = false)
  private StudyPlan studyPlan;

  @Column(name = "topic_id", nullable = false)
  private Long topicId;

  @Column(name = "subtopic_id", nullable = true)
  private Long subtopicId;

  @Column(name = "priority", nullable = false)
  private Short priority;

  @Column(name = "reason", nullable = false)
  private String reason;

  @Column(name = "evidence_json", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private String evidenceJson;

  @Column(name = "status", nullable = false)
  private String status = "TODO";

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  public StudyPlanItem() {}

  public StudyPlanItem(StudyPlan studyPlan, Long topicId, Long subtopicId,
      Short priority, String reason, String evidenceJson) {
    this.studyPlan = studyPlan;
    this.topicId = topicId;
    this.subtopicId = subtopicId;
    this.priority = priority;
    this.reason = reason;
    this.evidenceJson = evidenceJson;
    this.status = "TODO";
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  public Long getId() { return id; }

  public StudyPlan getStudyPlan() { return studyPlan; }

  public void setStudyPlan(StudyPlan studyPlan) { this.studyPlan = studyPlan; }

  public Long getTopicId() { return topicId; }

  public void setTopicId(Long topicId) { this.topicId = topicId; }

  public Long getSubtopicId() { return subtopicId; }

  public void setSubtopicId(Long subtopicId) { this.subtopicId = subtopicId; }

  public Short getPriority() { return priority; }

  public void setPriority(Short priority) { this.priority = priority; }

  public String getReason() { return reason; }

  public void setReason(String reason) { this.reason = reason; }

  public String getEvidenceJson() { return evidenceJson; }

  public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }

  public String getStatus() { return status; }

  public void setStatus(String status) { this.status = status; }

  public OffsetDateTime getCreatedAt() { return createdAt; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
