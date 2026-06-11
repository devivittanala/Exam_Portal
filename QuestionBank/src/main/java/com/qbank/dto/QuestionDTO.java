package com.qbank.dto;

import java.util.List;

public class QuestionDTO {
    private String question;
    private String questionType;
    private String subject;
    private String topic;
    private String difficulty;
    private int marks;
    private Long facultyId;
    private List<OptionDTO> options;

    public QuestionDTO() {}

    // Explicit Getters and Setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getQuestionType() { return questionType; }
    public void setQuestionType(String questionType) { this.questionType = questionType; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getMarks() { return marks; }
    public void setMarks(int marks) { this.marks = marks; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public List<OptionDTO> getOptions() { return options; }
    public void setOptions(List<OptionDTO> options) { this.options = options; }
}
