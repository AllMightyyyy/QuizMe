package org.zakariafarih.quizme.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.zakariafarih.quizme.dto.ApiResponse;
import org.zakariafarih.quizme.dto.QuizDTO;
import org.zakariafarih.quizme.entity.Option;
import org.zakariafarih.quizme.entity.Question;
import org.zakariafarih.quizme.entity.Quiz;
import org.zakariafarih.quizme.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> createQuiz(@RequestBody QuizDTO quizDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Validation errors", errors));
        }
        try {
            Quiz quiz = convertToEntity(quizDTO);
            Quiz createdQuiz = quizService.createQuiz(quiz);
            return ResponseEntity.ok(new ApiResponse(true, "Quiz created successfully", createdQuiz));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, "Error creating quiz", null));
        }
    }

    private Quiz convertToEntity(QuizDTO quizDTO) {
        Quiz quiz = Quiz.builder()
                .title(quizDTO.getTitle())
                .description(quizDTO.getDescription())
                .build();

        List<Question> questions = quizDTO.getQuestions().stream().map(questionDTO -> {
            List<Option> options = questionDTO.getOptions().stream().map(optionDTO -> Option.builder()
                    .text(optionDTO.getText())
                    .isCorrect(optionDTO.isCorrect())
                    .build()).collect(Collectors.toList());
            return Question.builder()
                    .content(questionDTO.getContent())
                    .timeLimit(questionDTO.getTimeLimit())
                    .quiz(quiz)
                    .options(options)  // Pass List<Option> here
                    .build();
        }).collect(Collectors.toList());

        quiz.setQuestions(questions);  // Pass List<Question> here
        return quiz;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllQuizzes(Pageable pageable) {
        Page<Quiz> quizzes = quizService.getAllQuizzes(pageable);
        return ResponseEntity.ok(new ApiResponse(true, "Quizzes retrieved successfully", quizzes));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getQuizById(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);
        if (quiz != null) {
            return ResponseEntity.ok(new ApiResponse(true, "Quiz retrieved successfully", quiz));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Quiz not found", null));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateQuiz(@PathVariable Long id, @RequestBody QuizDTO quizDTO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Validation errors", errors));
        }

        Quiz updatedQuiz = quizService.updateQuiz(id, convertToEntity(quizDTO));
        if (updatedQuiz != null) {
            return ResponseEntity.ok(new ApiResponse(true, "Quiz updated successfully", updatedQuiz));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Quiz not found", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteQuiz(@PathVariable Long id) {
        boolean deleted = quizService.deleteQuiz(id);
        if (deleted) {
            return ResponseEntity.ok(new ApiResponse(true, "Quiz deleted successfully", null));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse(false, "Quiz not found", null));
    }
}
