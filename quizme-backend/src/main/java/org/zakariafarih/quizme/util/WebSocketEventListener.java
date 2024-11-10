package org.zakariafarih.quizme.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.zakariafarih.quizme.service.WebSocketService;

@Component
public class WebSocketEventListener {

    @Autowired
    private WebSocketService webSocketService;

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String username = headers.getFirstNativeHeader("username");
        String sessionId = headers.getSessionId();

        if (username != null) {
            webSocketService.handleUserConnection(username, sessionId);
            logger.info("User Connected: {}, Session ID: {}", username, sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String username = headers.getFirstNativeHeader("username");
        String sessionId = headers.getSessionId();

        if (username != null) {
            webSocketService.handleUserDisconnection(username, sessionId);
            logger.info("User Disconnected: {}, Session ID: {}", username, sessionId);
        }
    }
}
