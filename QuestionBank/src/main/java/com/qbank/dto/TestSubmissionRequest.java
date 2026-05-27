package com.qbank.dto;

import java.util.Map;

public class TestSubmissionRequest {
    private Long studentId;
    private Map<Long, String> answers; // questionId -> optionId (for MCQ) or text/numerical answer (for NA)

    public TestSubmissionRequest() {}

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}
