package com.qbank.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.qbank.entity.Test;

public interface TestRepository extends JpaRepository<Test, Long> {
    List<Test> findByFacultyId(Long facultyId);
}
