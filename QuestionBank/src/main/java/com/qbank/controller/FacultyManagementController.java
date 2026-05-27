package com.qbank.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qbank.dto.OptionDTO;
import com.qbank.dto.QuestionDTO;
import com.qbank.dto.TestScheduleDTO;
import com.qbank.entity.CodingQuestion;
import com.qbank.entity.Faculty;
import com.qbank.entity.Question;
import com.qbank.entity.Test;
import com.qbank.entity.User;
import com.qbank.entity.UserCodingProfile;
import com.qbank.repository.CodingQuestionRepository;
import com.qbank.repository.QuestionRepository;
import com.qbank.repository.ResultRepository;
import com.qbank.repository.StudentCodingStatusRepository;
import com.qbank.repository.TestRepository;
import com.qbank.repository.UserCodingProfileRepository;
import com.qbank.repository.UserRepository;
import com.qbank.service.QuestionService;
import com.qbank.service.TestService;

import jakarta.servlet.http.HttpSession;

@Controller
public class FacultyManagementController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ResultRepository resultRepository;

    @Autowired
    private UserCodingProfileRepository userCodingProfileRepository;

    @Autowired
    private StudentCodingStatusRepository studentCodingStatusRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private CodingQuestionRepository codingQuestionRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TestService testService;

    @GetMapping("/faculty/dashboard")
    public String dashboard(HttpSession session, Model model) {

        User loggedIn = (User) session.getAttribute("user");

        if (loggedIn == null || !"FACULTY".equalsIgnoreCase(loggedIn.getRole())) {
            return "redirect:/";
        }

        Long facultyId = loggedIn.getId();

        Faculty faculty = (Faculty) userRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        List<Question> questions = questionRepository.findAll();

        List<CodingQuestion> codingQuestions = codingQuestionRepository.findAll();

        List<Test> scheduledTests = testRepository.findAll();

        List<User> students = userRepository.findByRole("STUDENT");

        List<Map<String, Object>> roster = new ArrayList<>();

        for (User s : students) {

            Map<String, Object> map = new HashMap<>();

            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("email", s.getEmail());
            map.put("batch", s.getBatch() != null ? s.getBatch() : "Batch A");

            UserCodingProfile profile =
                    userCodingProfileRepository.findByUserId(s.getId()).orElse(null);

            if (profile != null) {
                map.put("leetcodeUsername", profile.getLeetcodeUsername());
                map.put("hackerrankUsername", profile.getHackerrankUsername());
                map.put("codechefUsername", profile.getCodechefUsername());
            } else {
                map.put("leetcodeUsername", null);
                map.put("hackerrankUsername", null);
                map.put("codechefUsername", null);
            }

            long solvedCount =
                    studentCodingStatusRepository.countByStudentIdAndStatus(
                            s.getId(),
                            "SOLVED"
                    );

            map.put("solvedCount", solvedCount);

            roster.add(map);
        }

        model.addAttribute("faculty", faculty);
        model.addAttribute("questions", questions);
        model.addAttribute("codingQuestions", codingQuestions);
        model.addAttribute("scheduledTests", scheduledTests);
        model.addAttribute("roster", roster);

        return "faculty-dashboard";
    }

    @PostMapping("/faculty/create-question")
    public String createQuestion(

            @RequestParam("questionType") String questionType,

            @RequestParam(value = "questionText", required = false)
            String questionText,

            @RequestParam(value = "subject", required = false)
            String subject,

            @RequestParam(value = "difficulty", required = false)
            String difficulty,

            @RequestParam(value = "marks", required = false, defaultValue = "1")
            int marks,

            @RequestParam(value = "topic", required = false)
            String topic,

            @RequestParam(value = "optionA", required = false)
            String optionA,

            @RequestParam(value = "optionB", required = false)
            String optionB,

            @RequestParam(value = "optionC", required = false)
            String optionC,

            @RequestParam(value = "optionD", required = false)
            String optionD,

            @RequestParam(value = "correctOption", required = false)
            String correctOption,

            @RequestParam(value = "title", required = false)
            String title,

            @RequestParam(value = "platform", required = false)
            String platform,

            @RequestParam(value = "problemLink", required = false)
            String problemLink,

            @RequestParam(value = "score", required = false, defaultValue = "10")
            int score,

            HttpSession session) {

        User facultyUser = (User) session.getAttribute("user");

        if (facultyUser == null) {
            return "redirect:/";
        }

        Faculty faculty = (Faculty) userRepository.findById(facultyUser.getId())
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        if ("MCQ".equalsIgnoreCase(questionType)) {

            QuestionDTO dto = new QuestionDTO();

            dto.setQuestion(questionText);
            dto.setQuestionType("MCQ");
            dto.setSubject(subject);
            dto.setDifficulty(difficulty);
            dto.setMarks(marks);
            dto.setTopic(topic);
            dto.setFacultyId(faculty.getId());

            List<OptionDTO> options = new ArrayList<>();

            options.add(new OptionDTO(optionA, "A".equals(correctOption)));
            options.add(new OptionDTO(optionB, "B".equals(correctOption)));
            options.add(new OptionDTO(optionC, "C".equals(correctOption)));
            options.add(new OptionDTO(optionD, "D".equals(correctOption)));

            dto.setOptions(options);

            questionService.createQuestion(dto);

        } else if ("CODING".equalsIgnoreCase(questionType)) {

            CodingQuestion c = new CodingQuestion();

            c.setTitle(title);
            c.setPlatform(platform);
            c.setProblemLink(problemLink);
            c.setSubject(subject);
            c.setDifficulty(difficulty);
            c.setScore(score);
            c.setTopic(topic);
            c.setFaculty(faculty);

            codingQuestionRepository.save(c);
        }

        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/faculty/delete-question")
    public String deleteQuestion(

            @RequestParam("questionId") Long questionId,

            @RequestParam("questionType") String questionType,

            HttpSession session) {

        User faculty = (User) session.getAttribute("user");

        if (faculty == null) {
            return "redirect:/";
        }

        if ("MCQ".equalsIgnoreCase(questionType)) {

            questionService.deleteQuestion(questionId, faculty.getId());

        } else if ("CODING".equalsIgnoreCase(questionType)) {

            codingQuestionRepository.deleteById(questionId);
        }

        return "redirect:/faculty/dashboard";
    }

    @PostMapping("/faculty/import-csv")
    public String importCSV(

            @RequestParam("csvContent") String csvContent,

            HttpSession session) {

        User faculty = (User) session.getAttribute("user");

        if (faculty == null) {
            return "redirect:/";
        }

        questionService.importQuestionsFromCSV(csvContent, faculty.getId());

        return "redirect:/faculty/dashboard";
    }

    @GetMapping("/faculty/export-csv")
    @ResponseBody
    public ResponseEntity<byte[]> exportCSV() {

        String csv = questionService.exportQuestionsToCSV("Java");

        byte[] csvBytes = csv.getBytes();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=questions_Java.csv"
                )
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csvBytes);
    }

    @PostMapping("/faculty/schedule-test")
    public String scheduleTest(

            @RequestParam("title") String title,

            @RequestParam("duration") int duration,

            @RequestParam("totalMarks") int totalMarks,

            @RequestParam(value = "questionIds", required = false)
            Set<Long> questionIds,

            @RequestParam(value = "assignedStudentIds", required = false)
            Set<Long> assignedStudentIds,

            HttpSession session) {

        User faculty = (User) session.getAttribute("user");

        if (faculty == null) {
            return "redirect:/";
        }

        TestScheduleDTO dto = new TestScheduleDTO();

        dto.setTitle(title);
        dto.setDescription("Academic Assessment Exam Session");
        dto.setDuration(duration);
        dto.setTotalMarks(totalMarks);
        dto.setFacultyId(faculty.getId());

        dto.setQuestionIds(
                questionIds != null
                        ? questionIds
                        : Collections.emptySet()
        );

        dto.setAssignedStudentIds(
                assignedStudentIds != null
                        ? assignedStudentIds
                        : Collections.emptySet()
        );

        dto.setStartTime(LocalDateTime.now());
        dto.setEndTime(LocalDateTime.now().plusDays(7));

        testService.scheduleTest(dto);

        return "redirect:/faculty/dashboard";
    }
}