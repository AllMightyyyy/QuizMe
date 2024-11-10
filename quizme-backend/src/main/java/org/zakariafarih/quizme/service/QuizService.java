package org.zakariafarih.quizme.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zakariafarih.quizme.entity.Question;
import org.zakariafarih.quizme.entity.Quiz;
import org.zakariafarih.quizme.entity.Option;
import org.zakariafarih.quizme.exception.QuizValidationException;
import org.zakariafarih.quizme.repository.QuizRepository;

import java.util.List;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private WebSocketService webSocketService;

    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        validateQuiz(quiz);
        return quizRepository.save(quiz);
    }

    public Page<Quiz> getAllQuizzes(Pageable pageable) {
        return quizRepository.findAll(pageable);
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id).orElse(null);
    }

    @Transactional
    public Quiz updateQuiz(Long id, Quiz updatedQuiz) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz != null) {
            quiz.setTitle(updatedQuiz.getTitle());
            quiz.setDescription(updatedQuiz.getDescription());
            quiz.setQuestions(updatedQuiz.getQuestions());
            return quizRepository.save(quiz);
        }
        return null;
    }

    @Transactional
    public boolean deleteQuiz(Long id) {
        if (quizRepository.existsById(id)) {
            quizRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private void validateQuiz(Quiz quiz) {
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            throw new QuizValidationException("Quiz must contain at least one question.");
        }
        for (Question question : quiz.getQuestions()) {
            if (question.getOptions() == null || question.getOptions().size() < 2) {
                throw new QuizValidationException("Each question must have at least two options.");
            }
            boolean hasCorrectOption = question.getOptions().stream().anyMatch(Option::isCorrect);
            if (!hasCorrectOption) {
                throw new QuizValidationException("Each question must have at least one correct option.");
            }
        }
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

}
