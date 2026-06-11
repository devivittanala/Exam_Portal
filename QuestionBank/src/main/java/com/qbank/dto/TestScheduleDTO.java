package com.qbank.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class TestScheduleDTO {
    private String title;
    private String description;
    private int duration;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalMarks;
    private Long facultyId;
    private Set<Long> questionIds;
    private Set<Long> assignedStudentIds;

    public static class SectionDTO {
        private String name;
        private String description;
        private Set<Long> questionIds;

        public SectionDTO() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Set<Long> getQuestionIds() { return questionIds; }
        public void setQuestionIds(Set<Long> questionIds) { this.questionIds = questionIds; }
    }
    private java.util.List<SectionDTO> sections = new java.util.ArrayList<>();

    public TestScheduleDTO() {}

    // Explicit Getters and Setters
    public Set<Long> getAssignedStudentIds() { return assignedStudentIds; }
    public void setAssignedStudentIds(Set<Long> assignedStudentIds) { this.assignedStudentIds = assignedStudentIds; }

    public java.util.List<SectionDTO> getSections() { return sections; }
    public void setSections(java.util.List<SectionDTO> sections) { this.sections = sections; }

    // Explicit Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public int getTotalMarks() { return totalMarks; }
    public void setTotalMarks(int totalMarks) { this.totalMarks = totalMarks; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public Set<Long> getQuestionIds() { return questionIds; }
    public void setQuestionIds(Set<Long> questionIds) { this.questionIds = questionIds; }
}
