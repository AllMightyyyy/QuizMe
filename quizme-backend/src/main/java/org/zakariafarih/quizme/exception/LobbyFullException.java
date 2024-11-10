package org.zakariafarih.quizme.exception;

public class LobbyFullException extends RuntimeException {
    public LobbyFullException(String message) {
        super(message);
    }
}
