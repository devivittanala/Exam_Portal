package com.qbank.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.qbank.dto.TestSubmissionRequest;
import com.qbank.entity.CodingQuestion;
import com.qbank.entity.Question;
import com.qbank.entity.Result;
import com.qbank.entity.StudentCodingStatus;
import com.qbank.entity.Test;
import com.qbank.entity.User;
import com.qbank.entity.UserAchievement;
import com.qbank.entity.UserCodingProfile;
import com.qbank.repository.CodingQuestionRepository;
import com.qbank.repository.QuestionRepository;
import com.qbank.repository.ResultRepository;
import com.qbank.repository.StudentCodingStatusRepository;
import com.qbank.repository.TestRepository;
import com.qbank.repository.UserAchievementRepository;
import com.qbank.repository.UserCodingProfileRepository;
import com.qbank.repository.UserRepository;
import com.qbank.service.CodingPlatformSyncService;
import com.qbank.service.NotificationService;
import com.qbank.service.TestService;
import com.qbank.service.UserAchievementService;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CodingQuestionRepository codingQuestionRepository;

    @Autowired
    private UserCodingProfileRepository userCodingProfileRepository;

    @Autowired
    private StudentCodingStatusRepository studentCodingStatusRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private CodingPlatformSyncService codingPlatformSyncService;

    @Autowired
    private UserAchievementService userAchievementService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TestService testService;

    @GetMapping("/student/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"STUDENT".equalsIgnoreCase(loggedIn.getRole())) {
            return "redirect:/";
        }

        Long studentId = loggedIn.getId();
        // Refresh student entity to get latest updates
        User student = userRepository.findById(studentId).orElse(loggedIn);

        // 1. KPI Statistics
        List<Test> allTests = testRepository.findAll();
        List<Result> studentResults = resultRepository.findByStudentId(studentId);
        
        Set<Long> completedTestIds = studentResults.stream()
                .map(r -> r.getTest().getId())
                .collect(Collectors.toSet());

        List<Test> availableTests = allTests.stream()
                .filter(t -> !completedTestIds.contains(t.getId()))
                .filter(t -> t.getAssignedStudents().isEmpty() || t.getAssignedStudents().stream().anyMatch(u -> u.getId().equals(studentId)))
                .collect(Collectors.toList());

        double averageScore = 0.0;
        if (!studentResults.isEmpty()) {
            double sum = studentResults.stream().mapToDouble(Result::getPercentage).sum();
            averageScore = sum / studentResults.size();
            averageScore = Math.round(averageScore * 100.0) / 100.0;
        }

        long totalCodingQuestions = codingQuestionRepository.count();
        long totalCodingSolved = studentCodingStatusRepository.countByStudentIdAndStatus(studentId, "SOLVED");
        int codingSolvedPercent = 0;
        if (totalCodingQuestions > 0) {
            codingSolvedPercent = (int) Math.round(((double) totalCodingSolved / totalCodingQuestions) * 100.0);
        }

        // 2. Badges List
        List<UserAchievement> earned = userAchievementService.getAchievementsForStudent(studentId);
        Map<String, Boolean> badges = new HashMap<>();
        badges.put("Java Master", false);
        badges.put("DSA Expert", false);
        badges.put("Streak Expert", false);
        for (UserAchievement ach : earned) {
            if ("Java Master".equalsIgnoreCase(ach.getBadgeName())) badges.put("Java Master", true);
            if ("DSA Expert".equalsIgnoreCase(ach.getBadgeName())) badges.put("DSA Expert", true);
            if ("30-Day Streak".equalsIgnoreCase(ach.getBadgeName()) || "Streak Expert".equalsIgnoreCase(ach.getBadgeName())) {
                badges.put("Streak Expert", true);
            }
        }

        // 3. User Coding profile handles
        UserCodingProfile profile = userCodingProfileRepository.findByUserId(studentId).orElse(null);

        // 4. Subjects dynamic stats & Preloaded details for JavaScript Modal swapper
        List<Question> allQuestions = questionRepository.findAll();
        Map<String, Map<String, Object>> subjectStats = new HashMap<>();
        Map<String, Object> subjectsData = new HashMap<>();

        for (String subName : List.of("Java", "JavaScript", "Python", "Spring Boot", "MySQL", "DSA")) {
            // MCQ counts & list
            List<Question> mcqs = allQuestions.stream()
                    .filter(q -> q.getSubject() != null && q.getSubject().equalsIgnoreCase(subName))
                    .collect(Collectors.toList());

            long mcqCount = mcqs.size();

            // Coding counts & list
            List<CodingQuestion> questions = codingQuestionRepository.findBySubject(subName);
            long codingCount = questions.size();
            long solvedCount = studentCodingStatusRepository.countByStudentIdAndCodingQuestionSubjectAndStatus(studentId, subName, "SOLVED");
            
            int solvedPercent = 0;
            if (codingCount > 0) {
                solvedPercent = (int) Math.round(((double) solvedCount / codingCount) * 100.0);
            }

            // Populate Stats grid
            Map<String, Object> stats = new HashMap<>();
            stats.put("mcqCount", mcqCount);
            stats.put("codingCount", codingCount);
            stats.put("solvedCount", solvedCount);
            stats.put("solvedPercent", solvedPercent);
            subjectStats.put(subName, stats);

            // Populate Preloaded JSON map
            Map<String, Object> details = new HashMap<>();
            details.put("mcqs", mcqs);

            List<Map<String, Object>> codingList = new ArrayList<>();
            for (CodingQuestion q : questions) {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("title", q.getTitle());
                qMap.put("difficulty", q.getDifficulty());
                qMap.put("platform", q.getPlatform());
                qMap.put("score", q.getScore());
                qMap.put("problemLink", q.getProblemLink());
                qMap.put("topic", q.getTopic());
                
                Optional<StudentCodingStatus> statusOpt = studentCodingStatusRepository.findByStudentIdAndCodingQuestionId(studentId, q.getId());
                qMap.put("status", statusOpt.isPresent() ? statusOpt.get().getStatus() : "NOT_STARTED");
                codingList.add(qMap);
            }
            details.put("coding", codingList);
            subjectsData.put(subName, details);
        }

        // 5. Streaks matrix (28 days calendar grid)
        List<Integer> streakMatrix = new ArrayList<>();
        for (int i = 0; i < 28; i++) {
            if (i < totalCodingSolved) {
                streakMatrix.add(1);
            } else {
                streakMatrix.add(0);
            }
        }

        // 6. In-Memory notification pull (REST polling replacement)
        List<Map<String, String>> rawAlerts = notificationService.fetchAndClearNotifications(studentId);
        List<Map<String, String>> unreadNotifications = new ArrayList<>();
        for (Map<String, String> alert : rawAlerts) {
            Map<String, String> map = new HashMap<>();
            map.put("message", alert.get("title") + " " + alert.get("detail"));
            unreadNotifications.add(map);
        }

        // 7. Chart.js statistics
        List<StudentCodingStatus> allStatuses = studentCodingStatusRepository.findByStudentId(studentId);
        long lcCount = 0;
        long hrCount = 0;
        long ccCount = 0;
        for (StudentCodingStatus status : allStatuses) {
            if ("SOLVED".equalsIgnoreCase(status.getStatus())) {
                String plat = status.getCodingQuestion().getPlatform();
                if ("LeetCode".equalsIgnoreCase(plat)) lcCount++;
                else if ("HackerRank".equalsIgnoreCase(plat)) hrCount++;
                else if ("CodeChef".equalsIgnoreCase(plat)) ccCount++;
            }
        }
        Map<String, Long> platformRatios = new HashMap<>();
        platformRatios.put("leetcode", lcCount);
        platformRatios.put("hackerrank", hrCount);
        platformRatios.put("codechef", ccCount);

        List<Long> dailyActivity = List.of(0L, 0L, 0L, 0L, 0L, 0L, totalCodingSolved);
        List<Integer> weaknessRadar = List.of(8, 7, 9, 8, 6, 9); // default topic matrix

        // Bind attributes to Thymeleaf model
        model.addAttribute("student", student);
        model.addAttribute("availableTests", availableTests);
        model.addAttribute("testHistory", studentResults);
        model.addAttribute("averageScore", averageScore);
        model.addAttribute("totalCodingSolved", totalCodingSolved);
        model.addAttribute("codingSolvedPercent", codingSolvedPercent);
        model.addAttribute("badges", badges);
        model.addAttribute("userCodingProfile", profile);
        model.addAttribute("subjectStats", subjectStats);
        model.addAttribute("subjectsData", subjectsData);
        model.addAttribute("streakMatrix", streakMatrix);
        model.addAttribute("unreadNotifications", unreadNotifications);
        model.addAttribute("platformRatios", platformRatios);
        model.addAttribute("dailyActivity", dailyActivity);
        model.addAttribute("weaknessRadar", weaknessRadar);

        return "student-dashboard";
    }

    @PostMapping("/student/connect-handles")
    public String connectHandles(
            @RequestParam(value = "leetcodeUsername", required = false) String leetcode,
            @RequestParam(value = "hackerrankUsername", required = false) String hackerrank,
            @RequestParam(value = "codechefUsername", required = false) String codechef,
            HttpSession session) {
        
        User student = (User) session.getAttribute("user");
        if (student == null) return "redirect:/";

        codingPlatformSyncService.linkProfile(student.getId(), leetcode, hackerrank, codechef);
        return "redirect:/student/dashboard";
    }

    @PostMapping("/student/sync-now")
    public String syncNow(HttpSession session) {
        User student = (User) session.getAttribute("user");
        if (student == null) return "redirect:/";

        codingPlatformSyncService.syncProfile(student.getId());
        return "redirect:/student/dashboard";
    }

    @PostMapping("/student/submit-test")
    public String submitTest(
            @RequestParam("testId") Long testId,
            @RequestParam Map<String, String> allParams,
            HttpSession session) {
        
        User student = (User) session.getAttribute("user");
        if (student == null) return "redirect:/";

        TestSubmissionRequest request = new TestSubmissionRequest();
        request.setStudentId(student.getId());

        Map<Long, String> answers = new HashMap<>();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            if (entry.getKey().startsWith("answers[")) {
                String qIdStr = entry.getKey().substring(8, entry.getKey().length() - 1);
                answers.put(Long.parseLong(qIdStr), entry.getValue());
            }
        }
        request.setAnswers(answers);
        
        testService.submitAndGradeTest(testId, request);
        return "redirect:/student/dashboard";
    }
}
