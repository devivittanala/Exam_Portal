package com.qbank.dto;

public class AdminStatsDTO {
    private long totalUsers;
    private long totalQuestions;
    private long totalTests;

    public AdminStatsDTO(long totalUsers, long totalQuestions, long totalTests) {
        this.totalUsers = totalUsers;
        this.totalQuestions = totalQuestions;
        this.totalTests = totalTests;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalQuestions() { return totalQuestions; }
    public long getTotalTests() { return totalTests; }
}
