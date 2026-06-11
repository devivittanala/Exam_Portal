package com.qbank.service;

import com.qbank.dto.QuestionDTO;
import com.qbank.entity.Faculty;
import com.qbank.entity.Option;
import com.qbank.entity.Question;
import com.qbank.repository.QuestionRepository;
import com.qbank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Question createQuestion(QuestionDTO dto) {
        Faculty faculty = (Faculty) userRepository.findById(dto.getFacultyId())
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        Question question = new Question();
        question.setQuestion(dto.getQuestion());
        question.setQuestionType(dto.getQuestionType());
        question.setSubject(dto.getSubject());
        question.setTopic(dto.getTopic());
        question.setDifficulty(dto.getDifficulty());
        question.setMarks(dto.getMarks());
        question.setFaculty(faculty);

        if (dto.getOptions() != null) {
            dto.getOptions().forEach(optDto -> {
                Option option = new Option(optDto.getOptionText(), optDto.isCorrect());
                question.addOption(option);
            });
        }

        return questionRepository.save(question);
    }

    public List<Question> getQuestionsByFaculty(Long facultyId) {
        return questionRepository.findByFacultyId(facultyId);
    }

    @Transactional
    public void deleteQuestion(Long questionId, Long facultyId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!q.getFaculty().getId().equals(facultyId)) {
            throw new RuntimeException("Unauthorized: You can only delete your own questions");
        }

        questionRepository.delete(q);
    }

    @Transactional
    public void importQuestionsFromCSV(String csvContent, Long facultyId) {
        Faculty faculty = (Faculty) userRepository.findById(facultyId)
                .orElseThrow(() -> new RuntimeException("Faculty not found"));

        String[] lines = csvContent.split("\n");
        boolean isHeader = true;

        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            if (isHeader) {
                isHeader = false; // skip header
                continue;
            }

            try {
                // Splits by comma outside of quotes to support option text with commas
                String[] cols = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (cols.length < 5) continue;

                String stem = cols[0].replace("\"", "").trim();
                String subject = cols[1].replace("\"", "").trim();
                String difficulty = cols[2].replace("\"", "").trim();
                int marks = Integer.parseInt(cols[3].replace("\"", "").trim());
                String topic = cols[4].replace("\"", "").trim();

                Question question = new Question();
                question.setQuestion(stem);
                question.setQuestionType("MCQ");
                question.setSubject(subject);
                question.setDifficulty(difficulty);
                question.setMarks(marks);
                question.setTopic(topic);
                question.setFaculty(faculty);

                // Options (columns 5 to end, in pairs of optionText, isCorrect)
                for (int i = 5; i < cols.length - 1; i += 2) {
                    String optText = cols[i].replace("\"", "").trim();
                    if (optText.isEmpty()) continue;
                    boolean isCorrect = Boolean.parseBoolean(cols[i + 1].replace("\"", "").trim());
                    Option option = new Option(optText, isCorrect);
                    question.addOption(option);
                }

                questionRepository.save(question);
            } catch (Exception e) {
                System.err.println("Malformed CSV Row skipped: " + line + ". Error: " + e.getMessage());
            }
        }
    }

    public String exportQuestionsToCSV(String subject) {
        List<Question> questions = questionRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("Question,Subject,Difficulty,Marks,Topic,OptionA,IsCorrectA,OptionB,IsCorrectB,OptionC,IsCorrectC,OptionD,IsCorrectD\n");

        for (Question q : questions) {
            if (subject != null && !subject.trim().isEmpty() && !subject.equalsIgnoreCase(q.getSubject())) {
                continue;
            }
            csv.append("\"").append(q.getQuestion().replace("\"", "\"\"")).append("\",");
            csv.append("\"").append(q.getSubject()).append("\",");
            csv.append("\"").append(q.getDifficulty()).append("\",");
            csv.append(q.getMarks()).append(",");
            csv.append("\"").append(q.getTopic() != null ? q.getTopic() : "General").append("\",");

            List<Option> opts = q.getOptions();
            for (int i = 0; i < 4; i++) {
                if (i < opts.size()) {
                    Option opt = opts.get(i);
                    csv.append("\"").append(opt.getOptionText().replace("\"", "\"\"")).append("\",");
                    csv.append(opt.isCorrect());
                } else {
                    csv.append("\"\",false");
                }
                if (i < 3) csv.append(",");
            }
            csv.append("\n");
        }
        return csv.toString();
    }
}