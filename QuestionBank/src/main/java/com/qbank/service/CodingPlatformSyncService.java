package com.qbank.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.qbank.entity.CodingQuestion;
import com.qbank.entity.StudentCodingStatus;
import com.qbank.entity.User;
import com.qbank.entity.UserCodingProfile;
import com.qbank.repository.CodingQuestionRepository;
import com.qbank.repository.StudentCodingStatusRepository;
import com.qbank.repository.UserCodingProfileRepository;
import com.qbank.repository.UserRepository;

@Service
public class CodingPlatformSyncService {

    @Autowired
    private UserCodingProfileRepository userCodingProfileRepository;

    @Autowired
    private CodingQuestionRepository codingQuestionRepository;

    @Autowired
    private StudentCodingStatusRepository studentCodingStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private NotificationService notificationService;

    private final Random random = new Random();

    @Transactional
    public UserCodingProfile linkProfile(Long userId, String leetcode, String hackerrank, String codechef) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserCodingProfile profile = userCodingProfileRepository.findByUserId(userId)
                .orElse(new UserCodingProfile());

        profile.setUser(user);
        profile.setLeetcodeUsername(leetcode != null && !leetcode.trim().isEmpty() ? leetcode.trim() : null);
        profile.setHackerrankUsername(hackerrank != null && !hackerrank.trim().isEmpty() ? hackerrank.trim() : null);
        profile.setCodechefUsername(codechef != null && !codechef.trim().isEmpty() ? codechef.trim() : null);
        profile.setConnectedAt(LocalDateTime.now());

        UserCodingProfile saved = userCodingProfileRepository.save(profile);

        // Auto-award 30-Day Streak or Welcome badge on connecting profiles
        userAchievementService.checkForBadges(userId);

        return saved;
    }

    public Optional<UserCodingProfile> getProfile(Long userId) {
        return userCodingProfileRepository.findByUserId(userId);
    }

    @Transactional
    public void syncProfile(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        UserCodingProfile profile = userCodingProfileRepository.findByUserId(studentId)
                .orElse(null);

        if (profile == null) {
            return; // No linked profiles to sync
        }

        List<CodingQuestion> questions = codingQuestionRepository.findAll();

        for (CodingQuestion q : questions) {
            boolean platformLinked = false;
            if ("LeetCode".equalsIgnoreCase(q.getPlatform()) && profile.getLeetcodeUsername() != null) {
                platformLinked = true;
            } else if ("HackerRank".equalsIgnoreCase(q.getPlatform()) && profile.getHackerrankUsername() != null) {
                platformLinked = true;
            } else if ("CodeChef".equalsIgnoreCase(q.getPlatform()) && profile.getCodechefUsername() != null) {
                platformLinked = true;
            }

            if (platformLinked) {
                StudentCodingStatus status = studentCodingStatusRepository
                        .findByStudentIdAndCodingQuestionId(studentId, q.getId())
                        .orElse(new StudentCodingStatus());

                status.setStudent(student);
                status.setCodingQuestion(q);

                // Simulate realistic problem solving statuses based on difficulty
                if ("Easy".equalsIgnoreCase(q.getDifficulty())) {
                    if (!"SOLVED".equals(status.getStatus())) {
                        status.setStatus("SOLVED");
                        status.setSubmissionDate(LocalDateTime.now().minusHours(random.nextInt(48)));
                        status.setScore(q.getScore());
                        studentCodingStatusRepository.save(status);

                        // Broadcast solved alert
                        notifySolve(studentId, q.getTitle(), q.getPlatform(), "SOLVED");
                    }
                } else if ("Medium".equalsIgnoreCase(q.getDifficulty())) {
                    if ("NOT_STARTED".equals(status.getStatus())) {
                        status.setStatus("ATTEMPTED");
                        status.setSubmissionDate(LocalDateTime.now().minusHours(random.nextInt(12)));
                        status.setScore(0);
                        studentCodingStatusRepository.save(status);

                        notifySolve(studentId, q.getTitle(), q.getPlatform(), "ATTEMPTED");
                    } else if ("ATTEMPTED".equals(status.getStatus())) {
                        status.setStatus("SOLVED");
                        status.setSubmissionDate(LocalDateTime.now());
                        status.setScore(q.getScore());
                        studentCodingStatusRepository.save(status);

                        notifySolve(studentId, q.getTitle(), q.getPlatform(), "SOLVED");
                    }
                } else if ("Hard".equalsIgnoreCase(q.getDifficulty())) {
                    if ("NOT_STARTED".equals(status.getStatus())) {
                        status.setStatus("PENDING_REVIEW");
                        status.setSubmissionDate(LocalDateTime.now());
                        status.setScore(q.getScore() / 2);
                        studentCodingStatusRepository.save(status);

                        notifySolve(studentId, q.getTitle(), q.getPlatform(), "PENDING_REVIEW");
                    }
                }
            }
        }

        userAchievementService.checkForBadges(studentId);
    }

    private void notifySolve(Long studentId, String problemName, String platform, String status) {
        // detail stores "status|platform" which the frontend can easily split
        notificationService.addNotification(studentId, "SOLVE", problemName, status + "|" + platform);
    }

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void runBackgroundSyncScheduler() {
        List<UserCodingProfile> profiles = userCodingProfileRepository.findAll();
        if (profiles.isEmpty()) return;

        UserCodingProfile profile = profiles.get(random.nextInt(profiles.size()));
        Long studentId = profile.getUser().getId();

        List<CodingQuestion> questions = codingQuestionRepository.findAll();
        if (questions.isEmpty()) return;

        for (CodingQuestion q : questions) {
            boolean platformLinked = false;
            if ("LeetCode".equalsIgnoreCase(q.getPlatform()) && profile.getLeetcodeUsername() != null) {
                platformLinked = true;
            } else if ("HackerRank".equalsIgnoreCase(q.getPlatform()) && profile.getHackerrankUsername() != null) {
                platformLinked = true;
            } else if ("CodeChef".equalsIgnoreCase(q.getPlatform()) && profile.getCodechefUsername() != null) {
                platformLinked = true;
            }

            if (platformLinked) {
                StudentCodingStatus status = studentCodingStatusRepository
                        .findByStudentIdAndCodingQuestionId(studentId, q.getId())
                        .orElse(null);

                if (status == null) {
                    status = new StudentCodingStatus();
                    status.setStudent(profile.getUser());
                    status.setCodingQuestion(q);
                    status.setStatus("ATTEMPTED");
                    status.setSubmissionDate(LocalDateTime.now());
                    status.setScore(0);
                    studentCodingStatusRepository.save(status);

                    notifySolve(studentId, q.getTitle(), q.getPlatform(), "ATTEMPTED");
                    userAchievementService.checkForBadges(studentId);
                    break;
                } else if ("ATTEMPTED".equals(status.getStatus()) || "PENDING_REVIEW".equals(status.getStatus())) {
                    status.setStatus("SOLVED");
                    status.setSubmissionDate(LocalDateTime.now());
                    status.setScore(q.getScore());
                    studentCodingStatusRepository.save(status);

                    notifySolve(studentId, q.getTitle(), q.getPlatform(), "SOLVED");
                    userAchievementService.checkForBadges(studentId);
                    break;
                }
            }
        }
    }
}