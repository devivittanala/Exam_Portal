package com.qbank.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_coding_profiles")
public class UserCodingProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "leetcode_username")
    private String leetcodeUsername;

    @Column(name = "hackerrank_username")
    private String hackerrankUsername;

    @Column(name = "codechef_username")
    private String codechefUsername;

    @Column(name = "connected_at")
    private LocalDateTime connectedAt;

    public UserCodingProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getLeetcodeUsername() { return leetcodeUsername; }
    public void setLeetcodeUsername(String leetcodeUsername) { this.leetcodeUsername = leetcodeUsername; }

    public String getHackerrankUsername() { return hackerrankUsername; }
    public void setHackerrankUsername(String hackerrankUsername) { this.hackerrankUsername = hackerrankUsername; }

    public String getCodechefUsername() { return codechefUsername; }
    public void setCodechefUsername(String codechefUsername) { this.codechefUsername = codechefUsername; }

    public LocalDateTime getConnectedAt() { return connectedAt; }
    public void setConnectedAt(LocalDateTime connectedAt) { this.connectedAt = connectedAt; }
}
