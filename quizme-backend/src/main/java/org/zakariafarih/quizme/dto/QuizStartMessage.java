package org.zakariafarih.quizme.dto;

import jakarta.validation.constraints.NotNull;

public class QuizStartMessage {

    @NotNull(message = "Quiz ID is required")
    private Long quizId;

    // Getters and Setters
    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }
}