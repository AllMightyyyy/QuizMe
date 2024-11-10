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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class QuizService {

    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);

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
        logger.debug("Validating Quiz: {}", quiz.getTitle());

        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            logger.error("Validation failed: Quiz '{}' has no questions.", quiz.getTitle());
            throw new QuizValidationException("Quiz must contain at least one question.");
        }

        for (Question question : quiz.getQuestions()) {
            logger.debug("Validating Question '{}' with options count: {}", question.getContent(), question.getOptions().size());
            if (question.getOptions() == null || question.getOptions().size() < 2) {
                logger.error("Validation failed: Question '{}' has less than two options.", question.getContent());
                throw new QuizValidationException("Each question must have at least two options.");
            }

            long correctOptions = question.getOptions().stream().filter(Option::isCorrect).count();
            if (correctOptions < 1) {
                logger.error("Validation failed: Question '{}' has no correct options.", question.getContent());
                throw new QuizValidationException("Each question must have at least one correct option.");
            }

            logger.debug("Question '{}' passed validation with {} options and {} correct option(s).",
                    question.getContent(),
                    question.getOptions().size(),
                    correctOptions);
        }
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

}
