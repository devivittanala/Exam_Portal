package com.qbank.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.qbank.entity.Faculty;
import com.qbank.entity.Result;
import com.qbank.entity.Test;
import com.qbank.entity.User;
import com.qbank.repository.CodingQuestionRepository;
import com.qbank.repository.QuestionRepository;
import com.qbank.repository.ResultRepository;
import com.qbank.repository.TestRepository;
import com.qbank.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CodingQuestionRepository codingQuestionRepository;

    @Autowired
    private ResultRepository resultRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User loggedIn = (User) session.getAttribute("user");
        if (loggedIn == null || !"ADMIN".equalsIgnoreCase(loggedIn.getRole())) {
            return "redirect:/";
        }

        Long adminId = loggedIn.getId();
        User admin = userRepository.findById(adminId).orElse(loggedIn);

        // 1. KPI Statistics
        long totalUsers = userRepository.count();
        long totalQuestions = questionRepository.count() + codingQuestionRepository.count();
        long totalTests = testRepository.count();
        List<Result> allResults = resultRepository.findAll();
        long completedTests = allResults.size();
        
        double avgScore = 0.0;
        if (completedTests > 0) {
            double sumPercentage = allResults.stream().mapToDouble(Result::getPercentage).sum();
            avgScore = sumPercentage / completedTests;
            avgScore = Math.round(avgScore * 100.0) / 100.0;
        }

        // 2. Fetch User and Test lists
        List<User> users = userRepository.findAll();
        List<Test> tests = testRepository.findAll();

        // 3. Preload detailed test analytical rosters
        Map<Long, Map<String, Object>> testAnalysis = new HashMap<>();
        for (Test test : tests) {
            List<Result> testResults = resultRepository.findByTestId(test.getId());
            
            long completedCount = testResults.size();
            double classAvg = 0.0;
            double highScore = 0.0;
            double lowScore = 100.0;
            
            if (completedCount > 0) {
                double sumPercentage = testResults.stream().mapToDouble(Result::getPercentage).sum();
                classAvg = sumPercentage / completedCount;
                highScore = testResults.stream().mapToDouble(Result::getPercentage).max().orElse(0.0);
                lowScore = testResults.stream().mapToDouble(Result::getPercentage).min().orElse(0.0);
            } else {
                lowScore = 0.0;
            }

            Map<String, Object> analysis = new HashMap<>();
            analysis.put("testId", test.getId());
            analysis.put("title", test.getTitle());
            analysis.put("totalMarks", test.getTotalMarks());
            analysis.put("completedCount", completedCount);
            analysis.put("averageScore", Math.round(classAvg * 100.0) / 100.0);
            analysis.put("highScore", Math.round(highScore * 100.0) / 100.0);
            analysis.put("lowScore", Math.round(lowScore * 100.0) / 100.0);
            
            List<Map<String, Object>> studentRoster = new ArrayList<>();
            for (Result r : testResults) {
                Map<String, Object> map = new HashMap<>();
                Optional<User> studentOpt = userRepository.findById(r.getStudentId());
                map.put("studentName", studentOpt.isPresent() ? studentOpt.get().getName() : "Unknown Student");
                map.put("studentEmail", studentOpt.isPresent() ? studentOpt.get().getEmail() : "N/A");
                map.put("score", r.getScore());
                map.put("percentage", r.getPercentage());
                map.put("correctCount", r.getCorrectCount());
                map.put("incorrectCount", r.getIncorrectCount());
                studentRoster.add(map);
            }
            analysis.put("results", studentRoster);
            testAnalysis.put(test.getId(), analysis);
        }

        // Bind attributes
        model.addAttribute("admin", admin);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalTests", totalTests);
        model.addAttribute("completedTests", completedTests);
        model.addAttribute("averageScore", avgScore);
        model.addAttribute("users", users);
        model.addAttribute("tests", tests);
        model.addAttribute("testAnalysis", testAnalysis);

        return "admin-dashboard";
    }

    @PostMapping("/admin/create-user")
    public String createUser(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("role") String role,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "specialization", required = false) String specialization,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");
        if (admin == null) return "redirect:/";

        if (userRepository.findByEmail(email.trim()).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Email already exists.");
            return "redirect:/admin/dashboard";
        }

        User user;
        if ("FACULTY".equalsIgnoreCase(role)) {
            Faculty faculty = new Faculty();
            faculty.setDepartment(department != null && !department.isEmpty() ? department.trim() : "Computer Science");
            faculty.setSpecialization(specialization != null && !specialization.isEmpty() ? specialization.trim() : "General");
            user = faculty;
        } else {
            user = new User();
        }

        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPassword(password);
        user.setRole(role.toUpperCase());
        user.setActive(true);
        if ("STUDENT".equalsIgnoreCase(role)) {
            user.setBatch("Batch A");
        }

        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(
            @RequestParam("id") Long id,
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam("role") String role,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "specialization", required = false) String specialization,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        User admin = (User) session.getAttribute("user");
        if (admin == null) return "redirect:/";

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!email.equalsIgnoreCase(user.getEmail())) {
            Optional<User> existing = userRepository.findByEmail(email.trim());
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Email is already taken by another account.");
                return "redirect:/admin/dashboard";
            }
            user.setEmail(email.trim());
        }

        user.setName(name.trim());
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }
        
        if (user instanceof Faculty && "FACULTY".equalsIgnoreCase(role)) {
            Faculty faculty = (Faculty) user;
            faculty.setDepartment(department != null && !department.isEmpty() ? department.trim() : "Computer Science");
            faculty.setSpecialization(specialization != null && !specialization.isEmpty() ? specialization.trim() : "General");
        }

        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam("id") Long id, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null) return "redirect:/";

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/toggle-user-status")
    public String toggleUserStatus(@RequestParam("id") Long id, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null) return "redirect:/";

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/admin/delete-test")
    public String deleteTest(@RequestParam("testId") Long testId, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null) return "redirect:/";

        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));
        testRepository.delete(test);
        return "redirect:/admin/dashboard";
    }
}
