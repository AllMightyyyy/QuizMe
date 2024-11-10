package org.zakariafarih.quizme.service;

import org.zakariafarih.quizme.entity.Answer;
import org.zakariafarih.quizme.entity.Question;
import org.zakariafarih.quizme.entity.Score;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.entity.Option;
import org.zakariafarih.quizme.repository.AnswerRepository;
import org.zakariafarih.quizme.repository.QuestionRepository;
import org.zakariafarih.quizme.repository.ScoreRepository;
import org.zakariafarih.quizme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.Optional;

@Service
public class WebSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private UserRepository userRepository;

    private Map<Long, List<Question>> quizQuestionsMap = new HashMap<>(); // Quiz ID to questions

    private Map<String, Long> userStartTimeMap = new HashMap<>(); // username to start time of question

    // Broadcast a question to all users in the lobby
    public void broadcastQuestion(Long quizId, int questionIndex) {
        List<Question> questions = quizQuestionsMap.get(quizId);
        if (questions == null || questionIndex >= questions.size()) {
            System.out.println("No more questions or quiz not started, quizId: " + quizId);
            messagingTemplate.convertAndSend("/topic/quiz/end", "Quiz Ended");
            return;
        }
        Question currentQuestion = questions.get(questionIndex);
        System.out.println("Broadcasting question ID: " + currentQuestion.getId() + " for quiz ID: " + quizId);
        messagingTemplate.convertAndSend("/topic/quiz/question", currentQuestion);
    }

    // Handle answer submission
    public void handleAnswer(String username, Long questionId, Long selectedOptionId) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Question> questionOpt = questionRepository.findById(questionId);
        if (userOpt.isEmpty() || questionOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        Question question = questionOpt.get();

        boolean isCorrect = question.getOptions().stream()
                .filter(opt -> opt.getId().equals(selectedOptionId))
                .findFirst()
                .map(Option::isCorrect)
                .orElse(false);

        LocalDateTime answeredAt = LocalDateTime.now();
        Answer answer = Answer.builder()
                .user(user)
                .question(question)
                .selectedOptionId(selectedOptionId)
                .isCorrect(isCorrect)
                .answeredAt(answeredAt)
                .build();
        answerRepository.save(answer);

        // Calculate points based on speed and correctness
        Long startTime = userStartTimeMap.getOrDefault(username, System.currentTimeMillis());
        long responseTime = System.currentTimeMillis() - startTime; // in milliseconds

        int points = 0;
        if (isCorrect) {
            // Example scoring: 1000 - responseTime (for speed), minimum 100 points
            points = (int) Math.max(100, 1000 - responseTime / 10);
        }

        // Update or create score
        Optional<Score> scoreOpt = scoreRepository.findByUserAndQuiz(user, question.getQuiz());
        Score score;
        if (scoreOpt.isPresent()) {
            score = scoreOpt.get();
            score.setPoints(score.getPoints() + points);
        } else {
            score = Score.builder()
                    .user(user)
                    .quiz(question.getQuiz())
                    .points(points)
                    .build();
        }
        scoreRepository.save(score);

        // Broadcast updated leaderboard
        List<Score> leaderboard = scoreRepository.findAllByQuizIdOrderByPointsDesc(question.getQuiz().getId());
        messagingTemplate.convertAndSend("/topic/quiz/leaderboard", leaderboard);
    }

    // Initialize quiz questions
    public void initializeQuiz(Long quizId) {
        List<Question> questions = questionRepository.findByQuizId(quizId);
        if (questions == null || questions.isEmpty()) {
            System.out.println("No questions found for quiz ID: " + quizId);
        } else {
            quizQuestionsMap.put(quizId, questions);
            System.out.println("Broadcasting first question for quiz ID: " + quizId);
            broadcastQuestion(quizId, 0); // Start with the first question
        }
    }

    // Get leaderboard sorted by points descending
    public List<Score> getLeaderboard(Long quizId) {
        return scoreRepository.findAllByQuizIdOrderByPointsDesc(quizId);
    }
}
