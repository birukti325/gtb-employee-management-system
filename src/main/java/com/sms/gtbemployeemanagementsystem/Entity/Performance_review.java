package com.sms.gtbemployeemanagementsystem.Entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "performance_review")
public class Performance_review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "review_date")
    private LocalDate reviewDate;

    @Column(name = "reviewer_name")
    private String reviewerName;

    @Column(name = "rating")
    private Integer rating; // e.g., 1 to 5 scale

    @Column(name = "comments", length = 1000)
    private String comments;

    @Column(name = "goals_set", length = 1000)
    private String goalsSet;

    // Default Constructor
    public Performance_review() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public LocalDate getReviewDate() { return reviewDate; }
    public void setReviewDate(LocalDate reviewDate) { this.reviewDate = reviewDate; }

    public String getReviewerName() { return reviewerName; }
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public String getGoalsSet() { return goalsSet; }
    public void setGoalsSet(String goalsSet) { this.goalsSet = goalsSet; }
}