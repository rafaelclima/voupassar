package br.com.voupassar.studyplan.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "study_plans")
public class StudyPlan {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "is_active", nullable = false)
  private Boolean isActive = true;

  @Column(name = "algorithm_version", nullable = false)
  private String algorithmVersion;

  @Column(name = "status", nullable = false)
  private String status = "PESSOAL";

  @Column(name = "generated_at", nullable = false)
  private OffsetDateTime generatedAt;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "studyPlan", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<StudyPlanItem> items = new ArrayList<>();

  public StudyPlan() {}

  public StudyPlan(Long userId, String algorithmVersion) {
    this.userId = userId;
    this.algorithmVersion = algorithmVersion;
    this.isActive = true;
    this.generatedAt = OffsetDateTime.now();
    this.createdAt = OffsetDateTime.now();
    this.updatedAt = OffsetDateTime.now();
  }

  public Long getId() { return id; }

  public Long getUserId() { return userId; }

  public Boolean getIsActive() { return isActive; }

  public void setIsActive(Boolean isActive) { this.isActive = isActive; }

  public String getAlgorithmVersion() { return algorithmVersion; }

  public String getStatus() { return status; }

  public void setStatus(String status) { this.status = status; }

  public OffsetDateTime getGeneratedAt() { return generatedAt; }

  public OffsetDateTime getCreatedAt() { return createdAt; }

  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public List<StudyPlanItem> getItems() { return items; }

  public void setItems(List<StudyPlanItem> items) { this.items = items; }
}
