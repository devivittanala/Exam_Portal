package com.qbank.repository;

import com.qbank.entity.CodingQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CodingQuestionRepository extends JpaRepository<CodingQuestion, Long> {
    List<CodingQuestion> findBySubject(String subject);
    List<CodingQuestion> findByFacultyId(Long facultyId);
    long countBySubject(String subject);
}
