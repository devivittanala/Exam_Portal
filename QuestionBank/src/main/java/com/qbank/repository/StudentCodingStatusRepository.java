package com.qbank.repository;

import com.qbank.entity.StudentCodingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentCodingStatusRepository extends JpaRepository<StudentCodingStatus, Long> {
    List<StudentCodingStatus> findByStudentId(Long studentId);
    Optional<StudentCodingStatus> findByStudentIdAndCodingQuestionId(Long studentId, Long codingQuestionId);
    List<StudentCodingStatus> findByStudentIdAndCodingQuestionSubject(Long studentId, String subject);
    long countByStudentIdAndStatus(Long studentId, String status);
    long countByStudentIdAndCodingQuestionSubjectAndStatus(Long studentId, String subject, String status);
}