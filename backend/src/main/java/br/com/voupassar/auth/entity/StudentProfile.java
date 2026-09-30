package br.com.voupassar.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Perfil pedagógico 1:1 com {@code users} (TASK 3.5 — criado no registro;
 * edição completa na TASK 3.6).
 */
@Entity
@Table(name = "student_profiles")
public class StudentProfile {

  @Id
  private Long userId;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "display_name", nullable = false)
  private String displayName;

  @Column(name = "school_year")
  private String schoolYear;

  @Column(name = "target_year")
  private Short targetYear;

  @Column(name = "study_goal")
  private String studyGoal;

  public StudentProfile() {}

  public StudentProfile(User user, String displayName) {
    this.user = user;
    this.displayName = displayName;
  }

  public Long getUserId() {
    return userId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public String getSchoolYear() {
    return schoolYear;
  }

  public void setSchoolYear(String schoolYear) {
    this.schoolYear = schoolYear;
  }

  public Short getTargetYear() {
    return targetYear;
  }

  public void setTargetYear(Short targetYear) {
    this.targetYear = targetYear;
  }

  public String getStudyGoal() {
    return studyGoal;
  }

  public void setStudyGoal(String studyGoal) {
    this.studyGoal = studyGoal;
  }
}
