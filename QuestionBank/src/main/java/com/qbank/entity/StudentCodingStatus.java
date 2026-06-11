package com.qbank.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "student_coding_status", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "coding_question_id"})
})
public class StudentCodingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne
    @JoinColumn(name = "coding_question_id", nullable = false)
    private CodingQuestion codingQuestion;

    @Column(nullable = false, length = 50)
    private String status = "NOT_STARTED"; // SOLVED, ATTEMPTED, NOT_STARTED, PENDING_REVIEW

    @Column(name = "submission_date")
    private LocalDateTime submissionDate;

    @Column(nullable = false)
    private int score = 0;

    public StudentCodingStatus() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getStudent() { return student; }
    public void setStudent(User student) { this.student = student; }

    public CodingQuestion getCodingQuestion() { return codingQuestion; }
    public void setCodingQuestion(CodingQuestion codingQuestion) { this.codingQuestion = codingQuestion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSubmissionDate() { return submissionDate; }
    public void setSubmissionDate(LocalDateTime submissionDate) { this.submissionDate = submissionDate; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
}