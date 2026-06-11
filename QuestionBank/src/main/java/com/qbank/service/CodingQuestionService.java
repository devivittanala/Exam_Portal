package com.qbank.service;

import com.qbank.entity.CodingQuestion;
import com.qbank.entity.Faculty;
import com.qbank.entity.StudentCodingStatus;
import com.qbank.entity.User;
import com.qbank.repository.CodingQuestionRepository;
import com.qbank.repository.StudentCodingStatusRepository;
import com.qbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CodingQuestionService {

    @Autowired
    private CodingQuestionRepository codingQuestionRepository;

    @Autowired
    private StudentCodingStatusRepository studentCodingStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public CodingQuestion createCodingQuestion(CodingQuestion question, Long facultyId) {
        Faculty faculty = (Faculty) userRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));
        question.setFaculty(faculty);
        return codingQuestionRepository.save(question);
    }

    public List<CodingQuestion> getCodingQuestionsBySubject(String subject) {
        return codingQuestionRepository.findBySubject(subject);
    }

    public List<CodingQuestion> getCodingQuestionsByFaculty(Long facultyId) {
        return codingQuestionRepository.findByFacultyId(facultyId);
    }

    @Transactional
    public void deleteCodingQuestion(Long questionId, Long facultyId) {
        CodingQuestion q = codingQuestionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Coding question not found"));

        if (!q.getFaculty().getId().equals(facultyId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own questions");
        }

        codingQuestionRepository.delete(q);
    }

    public List<Map<String, Object>> getCodingQuestionsForStudent(Long studentId, String subject) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        List<CodingQuestion> questions = codingQuestionRepository.findBySubject(subject);
        List<StudentCodingStatus> statuses = studentCodingStatusRepository.findByStudentIdAndCodingQuestionSubject(studentId, subject);

        Map<Long, StudentCodingStatus> statusMap = new HashMap<>();
        for (StudentCodingStatus s : statuses) {
            statusMap.put(s.getCodingQuestion().getId(), s);
        }

        List<Map<String, Object>> response = new ArrayList<>();
        for (CodingQuestion q : questions) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("title", q.getTitle());
            map.put("problemLink", q.getProblemLink());
            map.put("difficulty", q.getDifficulty());
            map.put("platform", q.getPlatform());
            map.put("topic", q.getTopic());
            map.put("subject", q.getSubject());
            map.put("score", q.getScore());
            map.put("deadline", q.getDeadline());

            StudentCodingStatus status = statusMap.get(q.getId());
            if (status != null) {
                map.put("status", status.getStatus());
                map.put("submissionDate", status.getSubmissionDate());
                map.put("studentScore", status.getScore());
            } else {
                map.put("status", "NOT_STARTED");
                map.put("submissionDate", null);
                map.put("studentScore", 0);
            }
            response.add(map);
        }

        return response;
    }

    public Map<String, Object> getStudentCodingSummary(Long studentId) {
        Map<String, Object> summary = new HashMap<>();
        long solved = studentCodingStatusRepository.countByStudentIdAndStatus(studentId, "SOLVED");
        long attempted = studentCodingStatusRepository.countByStudentIdAndStatus(studentId, "ATTEMPTED");
        long notStarted = studentCodingStatusRepository.countByStudentIdAndStatus(studentId, "NOT_STARTED");
        long pending = studentCodingStatusRepository.countByStudentIdAndStatus(studentId, "PENDING_REVIEW");

        summary.put("solvedCount", solved);
        summary.put("attemptedCount", attempted);
        summary.put("notStartedCount", notStarted);
        summary.put("pendingCount", pending);

        long totalCodingQuestions = codingQuestionRepository.count();
        summary.put("totalCodingQuestions", totalCodingQuestions);

        double solvedPercentage = totalCodingQuestions == 0 ? 0.0 : (double) solved / totalCodingQuestions * 100.0;
        summary.put("solvedPercentage", Math.round(solvedPercentage * 10.0) / 10.0);

        return summary;
    }
}