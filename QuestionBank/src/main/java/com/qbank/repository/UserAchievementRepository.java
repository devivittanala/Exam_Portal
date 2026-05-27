package com.qbank.repository;

import com.qbank.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByStudentId(Long studentId);
    boolean existsByStudentIdAndBadgeName(Long studentId, String badgeName);
}
