package com.qbank.service;

import com.qbank.entity.User;
import com.qbank.entity.UserAchievement;
import com.qbank.repository.StudentCodingStatusRepository;
import com.qbank.repository.UserAchievementRepository;
import com.qbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserAchievementService {

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private StudentCodingStatusRepository studentCodingStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    public List<UserAchievement> getAchievementsForStudent(Long studentId) {
        return userAchievementRepository.findByStudentId(studentId);
    }

    @Transactional
    public void checkForBadges(Long studentId) {
        User student = userRepository.findById(studentId)
                .orElse(null);
        if (student == null) return;

        // 1. Check for Java Master badge (if solved >= 2 Java questions)
        long javaSolved = studentCodingStatusRepository.countByStudentIdAndCodingQuestionSubjectAndStatus(studentId, "Java", "SOLVED");
        if (javaSolved >= 2) {
            awardBadge(student, "Java Master", "Awarded for successfully solving at least 2 Java coding challenges on connected platforms!");
        }

        // 2. Check for DSA Expert badge (if solved >= 2 DSA questions)
        long dsaSolved = studentCodingStatusRepository.countByStudentIdAndCodingQuestionSubjectAndStatus(studentId, "DSA", "SOLVED");
        if (dsaSolved >= 2) {
            awardBadge(student, "DSA Expert", "Awarded for successfully solving at least 2 DSA coding challenges on connected platforms!");
        }

        // 3. Check for 30-Day Streak badge (if solved >= 3 coding questions of any subject)
        long totalSolved = studentCodingStatusRepository.countByStudentIdAndStatus(studentId, "SOLVED");
        if (totalSolved >= 3) {
            awardBadge(student, "30-Day Streak", "Demonstrated exceptional learning consistency by solving 3+ programming tasks!");
        }
    }

    private void awardBadge(User student, String badgeName, String description) {
        if (!userAchievementRepository.existsByStudentIdAndBadgeName(student.getId(), badgeName)) {
            UserAchievement badge = new UserAchievement();
            badge.setStudent(student);
            badge.setBadgeName(badgeName);
            badge.setDescription(description);
            badge.setAwardedAt(LocalDateTime.now());
            userAchievementRepository.save(badge);

            // Add notification alert for REST polling
            notificationService.addNotification(student.getId(), "BADGE", badgeName, description);
        }
    }
}
