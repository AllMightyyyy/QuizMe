package org.zakariafarih.quizme.controller;

import org.zakariafarih.quizme.dto.AnswerMessage;
import org.zakariafarih.quizme.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private WebSocketService webSocketService;

    @MessageMapping("/quiz/answer")
    public void receiveAnswer(@Payload AnswerMessage message) {
        webSocketService.handleAnswer(message.getUsername(), message.getQuestionId(), message.getSelectedOptionId());
    }

    // Add this method for initializing the quiz
    @MessageMapping("/quiz/initialize")
    public void initializeQuiz(@Payload Map<String, Object> payload) {
        Long quizId = ((Number) payload.get("quizId")).longValue();
        webSocketService.initializeQuiz(quizId);
    }
}