package org.zakariafarih.quizme.service;

import jakarta.annotation.PreDestroy;
import org.zakariafarih.quizme.entity.*;
import org.zakariafarih.quizme.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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

    @Autowired
    private LobbyService lobbyService;

    private LocalDateTime questionStartTime;

    private boolean isQuizActive = false;
    private Long activeQuizId = null;
    private List<Question> activeQuizQuestions = new ArrayList<>();
    private int currentQuestionIndex = 0;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private final Map<Long, Set<Long>> userAnswersMap = new ConcurrentHashMap<>();

    private final Map<Long, ScheduledFuture<?>> quizTimers = new ConcurrentHashMap<>();

    private final Map<String, String> userSessionMap = new ConcurrentHashMap<>();

    public synchronized void initializeQuiz(Long quizId) {
        if (isQuizActive) {
            System.out.println("A quiz is already active. Cannot start another.");
            messagingTemplate.convertAndSend("/topic/quiz/error", "A quiz is already in progress.");
            return;
        }

        List<Question> questions = questionRepository.findByQuizId(quizId);
        if (questions == null || questions.isEmpty()) {
            System.out.println("No questions found for quiz ID: " + quizId);
            messagingTemplate.convertAndSend("/topic/quiz/error", "No questions available for this quiz.");
            return;
        }

        isQuizActive = true;
        activeQuizId = quizId;
        activeQuizQuestions = new ArrayList<>(questions);
        currentQuestionIndex = 0;
        userAnswersMap.clear();

        broadcastQuestion();
    }

    private void broadcastQuestion() {
        if (currentQuestionIndex >= activeQuizQuestions.size()) {
            endQuiz();
            return;
        }

        Question currentQuestion = activeQuizQuestions.get(currentQuestionIndex);
        questionStartTime = LocalDateTime.now();

        Lobby lobby = lobbyService.getLobby();
        Set<User> lobbyUsers = lobby.getUsers();

        for (User user : lobbyUsers) {
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/topic/quiz/question", currentQuestion);
        }

        ScheduledFuture<?> revealTask = scheduler.schedule(() -> {
            revealAnswer(currentQuestion);
            updateLeaderboard();
            currentQuestionIndex++;
            broadcastQuestion();
        }, currentQuestion.getTimeLimit(), TimeUnit.SECONDS);

        quizTimers.put(activeQuizId, revealTask);
    }

    private void revealAnswer(Question question) {
        List<Option> correctOptions = question.getOptions().stream()
                .filter(Option::isCorrect)
                .collect(Collectors.toList());
        Lobby lobby = lobbyService.getLobby();
        Set<User> lobbyUsers = lobby.getUsers();
        for (User user : lobbyUsers) {
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/topic/quiz/correctAnswer", correctOptions);
        }
    }

    public synchronized void handleUserAnswer(String username, Long questionId, Long selectedOptionId) {
        try {
            if (!isQuizActive || activeQuizId == null) {
                messagingTemplate.convertAndSendToUser(username, "/topic/quiz/error", "No active quiz.");
                return;
            }

            Question currentQuestion = activeQuizQuestions.get(currentQuestionIndex);
            if (!currentQuestion.getId().equals(questionId)) {
                messagingTemplate.convertAndSendToUser(username, "/topic/quiz/error", "Invalid question.");
                return;
            }

            userAnswersMap.putIfAbsent(questionId, ConcurrentHashMap.newKeySet());

            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null) {
                messagingTemplate.convertAndSendToUser(username, "/topic/quiz/error", "User not found.");
                return;
            }

            Long userId = user.getId();
            if (userAnswersMap.get(questionId).contains(userId)) {
                messagingTemplate.convertAndSendToUser(username, "/topic/quiz/error", "Multiple answers are not allowed.");
                return;
            }

            userAnswersMap.get(questionId).add(userId);

            Optional<Option> selectedOption = currentQuestion.getOptions().stream()
                    .filter(opt -> opt.getId().equals(selectedOptionId))
                    .findFirst();

            if (selectedOption.isEmpty()) {
                messagingTemplate.convertAndSendToUser(username, "/topic/quiz/error", "Invalid option selected.");
                return;
            }

            boolean isCorrect = selectedOption.get().isCorrect();
            LocalDateTime answeredAt = LocalDateTime.now();

            Answer answer = Answer.builder()
                    .user(user)
                    .question(currentQuestion)
                    .selectedOptionId(selectedOptionId)
                    .isCorrect(isCorrect)
                    .answeredAt(answeredAt)
                    .build();
            answerRepository.save(answer);

            long timeTaken = Duration.between(questionStartTime, answeredAt).getSeconds();
            int maxPoints = 100;
            int points = isCorrect ? (int) Math.max(0, (maxPoints - (int)(timeTaken * 10))) : 0;
            points = Math.max(points, 0);

            Score score = scoreRepository.findByUserAndQuiz(user, currentQuestion.getQuiz())
                    .orElse(Score.builder()
                            .user(user)
                            .quiz(currentQuestion.getQuiz())
                            .points(0)
                            .build());

            score.setPoints(score.getPoints() + points);
            scoreRepository.save(score);
        } catch (Exception e) {
            e.printStackTrace();
            messagingTemplate.convertAndSendToUser(username, "/topic/quiz/error", "An error occurred while processing your answer.");
        }
    }

    private void updateLeaderboard() {
        if (activeQuizId == null) return;

        List<Score> leaderboard = scoreRepository.findAllByQuizIdOrderByPointsDesc(activeQuizId);
        Lobby lobby = lobbyService.getLobby();
        Set<User> lobbyUsers = lobby.getUsers();
        for (User user : lobbyUsers) {
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/topic/quiz/leaderboard", leaderboard);
        }
    }

    public synchronized void handleUserConnection(String username, String sessionId) {
        if (userSessionMap.containsKey(username)) {
            String oldSessionId = userSessionMap.get(username);
            handleUserReconnection(username, oldSessionId, sessionId);
        } else {
            userSessionMap.put(username, sessionId);
            if (isQuizActive && activeQuizId != null) {
                if (currentQuestionIndex < activeQuizQuestions.size()) {
                    Question currentQuestion = activeQuizQuestions.get(currentQuestionIndex);
                    messagingTemplate.convertAndSendToUser(username, "/topic/quiz/question", currentQuestion);
                }
            }
        }
    }

    private void handleUserReconnection(String username, String oldSessionId, String newSessionId) {
        userSessionMap.put(username, newSessionId);

        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        for (int i = 0; i < currentQuestionIndex; i++) {
            Question question = activeQuizQuestions.get(i);

            boolean hasAnswered = !answerRepository.findByUserAndQuestionQuiz(user, question.getQuiz())
                    .stream()
                    .anyMatch(answer -> answer.getQuestion().getId().equals(question.getId()));

            if (!hasAnswered) {
                Score score = scoreRepository.findByUserAndQuiz(user, question.getQuiz())
                        .orElse(Score.builder()
                                .user(user)
                                .quiz(question.getQuiz())
                                .points(0)
                                .build());
                score.setPoints(score.getPoints() + 0);
                scoreRepository.save(score);
            }
        }

        if (isQuizActive && currentQuestionIndex < activeQuizQuestions.size()) {
            Question currentQuestion = activeQuizQuestions.get(currentQuestionIndex);
            messagingTemplate.convertAndSendToUser(username, "/topic/quiz/question", currentQuestion);
        }
    }

    public synchronized void handleUserDisconnection(String username, String sessionId) {
        userSessionMap.remove(username);
        messagingTemplate.convertAndSend("/topic/lobby/users", lobbyService.getLobby().getUsers());
    }

    private void endQuiz() {
        ScheduledFuture<?> revealTask = quizTimers.get(activeQuizId);
        if (revealTask != null && !revealTask.isDone()) {
            revealTask.cancel(true);
        }

        isQuizActive = false;
        activeQuizId = null;
        activeQuizQuestions.clear();
        currentQuestionIndex = 0;
        userAnswersMap.clear();
        messagingTemplate.convertAndSend("/topic/quiz/end", "Quiz Ended.");
    }

    @PreDestroy
    public void shutdown() {
        scheduler.shutdownNow();
    }

    public synchronized void resetQuiz() {
        if (isQuizActive) {
            scheduler.shutdownNow();
            endQuiz();
            messagingTemplate.convertAndSend("/topic/quiz/reset", "Quiz has been reset by admin.");
        }
    }

    private Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(User::getId)
                .orElse(null);
    }

    private Long getActiveQuizId() {
        return activeQuizId;
    }
}
