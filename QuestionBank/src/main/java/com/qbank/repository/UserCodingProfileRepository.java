package com.qbank.repository;

import com.qbank.entity.UserCodingProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserCodingProfileRepository extends JpaRepository<UserCodingProfile, Long> {
    Optional<UserCodingProfile> findByUserId(Long userId);
}