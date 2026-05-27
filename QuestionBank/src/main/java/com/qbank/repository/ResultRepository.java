package com.qbank.repository;

import com.qbank.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import java.util.Optional;

public interface ResultRepository extends JpaRepository<Result, Long> {
    List<Result> findByStudentId(Long studentId);
    Optional<Result> findByStudentIdAndTestId(Long studentId, Long testId);
    List<Result> findByTestId(Long testId);
}
