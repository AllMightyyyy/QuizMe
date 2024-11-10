package org.zakariafarih.quizme.exception;

public class QuizNotActiveException extends RuntimeException {
    public QuizNotActiveException(String message) {
        super(message);
    }
}
