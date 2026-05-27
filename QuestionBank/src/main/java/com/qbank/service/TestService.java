package com.qbank.service;

import com.qbank.dto.TestScheduleDTO;
import com.qbank.dto.TestSubmissionRequest;
import com.qbank.entity.Faculty;
import com.qbank.entity.Option;
import com.qbank.entity.Question;
import com.qbank.entity.Result;
import com.qbank.entity.Test;
import com.qbank.entity.User;
import com.qbank.repository.QuestionRepository;
import com.qbank.repository.ResultRepository;
import com.qbank.repository.TestRepository;
import com.qbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ResultRepository resultRepository;

    @Transactional
    public Test scheduleTest(TestScheduleDTO dto) {
        Faculty faculty = (Faculty) userRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty assignment failed"));

        Test test = new Test();
        test.setTitle(dto.getTitle());
        test.setDescription(dto.getDescription());
        test.setDuration(dto.getDuration());
        test.setStartTime(dto.getStartTime());
        test.setEndTime(dto.getEndTime());
        test.setTotalMarks(dto.getTotalMarks());
        test.setFaculty(faculty);

        List<Question> explicitQuestions = questionRepository.findAllById(dto.getQuestionIds());
        test.setQuestions(new HashSet<>(explicitQuestions));

        // Handle sections creation if present
        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            java.util.List<com.qbank.entity.TestSection> sections = new java.util.ArrayList<>();
            java.util.Set<Question> allQuestions = new java.util.HashSet<>();
            for (TestScheduleDTO.SectionDTO secDto : dto.getSections()) {
                com.qbank.entity.TestSection section = new com.qbank.entity.TestSection();
                section.setName(secDto.getName());
                section.setDescription(secDto.getDescription());
                section.setTest(test);
                List<Question> secQuestions = questionRepository.findAllById(secDto.getQuestionIds());
                section.setQuestions(new java.util.HashSet<>(secQuestions));
                sections.add(section);
                allQuestions.addAll(secQuestions);
            }
            test.setSections(sections);
            if (test.getQuestions() == null || test.getQuestions().isEmpty()) {
                test.setQuestions(allQuestions);
            }
        }

        // Handle selective/targeted scheduling logic
        if (dto.getAssignedStudentIds() != null && !dto.getAssignedStudentIds().isEmpty()) {
            List<User> students = userRepository.findAllById(dto.getAssignedStudentIds());
            test.setAssignedStudents(new HashSet<>(students));
        }

        return testRepository.save(test);
    }

    @Transactional
    public Test modifyTest(Long id, TestScheduleDTO dto) {
        Test test = testRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        test.setTitle(dto.getTitle());
        test.setDescription(dto.getDescription());
        test.setDuration(dto.getDuration());
        test.setStartTime(dto.getStartTime());
        test.setEndTime(dto.getEndTime());
        test.setTotalMarks(dto.getTotalMarks());

        // Update flat questions
        List<Question> explicitQuestions = questionRepository.findAllById(dto.getQuestionIds());
        test.setQuestions(new HashSet<>(explicitQuestions));

        // Update sections
        test.getSections().clear();
        if (dto.getSections() != null && !dto.getSections().isEmpty()) {
            java.util.Set<Question> allQuestions = new java.util.HashSet<>();
            for (TestScheduleDTO.SectionDTO secDto : dto.getSections()) {
                com.qbank.entity.TestSection section = new com.qbank.entity.TestSection();
                section.setName(secDto.getName());
                section.setDescription(secDto.getDescription());
                section.setTest(test);
                List<Question> secQuestionsList = questionRepository.findAllById(secDto.getQuestionIds());
                section.setQuestions(new java.util.HashSet<>(secQuestionsList));
                test.getSections().add(section);
                allQuestions.addAll(secQuestionsList);
            }
            if (!allQuestions.isEmpty()) {
                test.setQuestions(allQuestions);
            }
        }

        // Update targeted students
        test.getAssignedStudents().clear();
        if (dto.getAssignedStudentIds() != null && !dto.getAssignedStudentIds().isEmpty()) {
            List<User> students = userRepository.findAllById(dto.getAssignedStudentIds());
            test.setAssignedStudents(new HashSet<>(students));
        }

        return testRepository.save(test);
    }

    public List<Test> getTestsByFaculty(Long facultyId) {
        return testRepository.findByFacultyId(facultyId);
    }

    @Transactional
    public Result submitAndGradeTest(Long testId, TestSubmissionRequest request) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        int correct = 0;
        int incorrect = 0;
        int obtainedScore = 0;
        int totalQuestionsCount = test.getQuestions().size();

        Map<Long, String> answers = request.getAnswers();

        for (Question question : test.getQuestions()) {
            String answer = answers != null ? answers.get(question.getId()) : null;

            if ("MCQ".equalsIgnoreCase(question.getQuestionType()) || "TRUE_FALSE".equalsIgnoreCase(question.getQuestionType())) {
                boolean foundCorrect = false;
                if (answer != null && !answer.trim().isEmpty()) {
                    try {
                        Long selectedOptionId = Long.parseLong(answer);
                        for (Option opt : question.getOptions()) {
                            if (opt.getId().equals(selectedOptionId) && opt.isCorrect()) {
                                foundCorrect = true;
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        // ignore or check string comparison
                    }
                }
                
                if (foundCorrect) {
                    correct++;
                    obtainedScore += question.getMarks();
                } else {
                    incorrect++;
                }
            } else if ("NA".equalsIgnoreCase(question.getQuestionType())) {
                boolean foundCorrect = false;
                if (answer != null && !answer.trim().isEmpty()) {
                    String cleanUserAns = answer.trim().toLowerCase();
                    for (Option opt : question.getOptions()) {
                        if (opt.isCorrect() && opt.getOptionText().trim().toLowerCase().equals(cleanUserAns)) {
                            foundCorrect = true;
                            break;
                        }
                    }
                }
                
                if (foundCorrect) {
                    correct++;
                    obtainedScore += question.getMarks();
                } else {
                    incorrect++;
                }
            } else {
                incorrect++;
            }
        }

        double percentage = totalQuestionsCount > 0 ? ((double) obtainedScore / test.getTotalMarks()) * 100.0 : 0.0;
        if (percentage > 100.0) percentage = 100.0;

        Optional<Result> existingResultOpt = resultRepository.findByStudentIdAndTestId(request.getStudentId(), testId);
        Result result = existingResultOpt.orElse(new Result());
        
        result.setStudentId(request.getStudentId());
        result.setTest(test);
        result.setScore(obtainedScore);
        result.setPercentage(percentage);
        result.setCorrectCount(correct);
        result.setIncorrectCount(incorrect);
        result.setTotalQuestions(totalQuestionsCount);

        return resultRepository.save(result);
    }
}
