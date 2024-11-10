package org.zakariafarih.quizme.controller;

import jakarta.validation.Valid;
import org.zakariafarih.quizme.dto.AnswerMessage;
import org.zakariafarih.quizme.dto.QuizStartMessage;
import org.zakariafarih.quizme.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @MessageMapping("/quiz/start")
    public void startQuiz(@Payload @Valid QuizStartMessage message) {
        webSocketService.initializeQuiz(message.getQuizId());
    }

    @MessageMapping("/quiz/answer")
    public void receiveAnswer(@Payload @Valid AnswerMessage message, Principal principal) {
        String username = principal.getName();
        webSocketService.handleUserAnswer(username, message.getQuestionId(), message.getSelectedOptionId());
    }

    @MessageExceptionHandler
    public void handleException(Exception e) {
        System.err.println("Error handling message: " + e.getMessage());
    }
}