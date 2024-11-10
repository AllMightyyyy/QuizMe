package org.zakariafarih.quizme.service;

import org.zakariafarih.quizme.entity.Quiz;
import org.zakariafarih.quizme.repository.QuizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private WebSocketService webSocketService;

    public Quiz createQuiz(Quiz quiz) {
        Quiz savedQuiz = quizRepository.save(quiz);
        webSocketService.initializeQuiz(savedQuiz.getId());
        return savedQuiz;
    }

    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id).orElse(null);
    }

    public Quiz updateQuiz(Long id, Quiz updatedQuiz) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz != null) {
            quiz.setTitle(updatedQuiz.getTitle());
            quiz.setDescription(updatedQuiz.getDescription());
            // Update other fields as necessary
            return quizRepository.save(quiz);
        }
        return null;
    }

    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }
}
