package org.zakariafarih.quizme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.zakariafarih.quizme.dto.ApiResponse;
import org.zakariafarih.quizme.entity.Lobby;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.exception.LobbyFullException;
import org.zakariafarih.quizme.service.LobbyService;
import org.zakariafarih.quizme.service.UserService;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private UserService userService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse> joinLobby(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", null));
        }

        if (lobbyService.isUserInLobby(user)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "User already in lobby", null));
        }

        try {
            lobbyService.joinLobby(user);
            return ResponseEntity.ok(new ApiResponse(true, "Joined the lobby", null));
        } catch (LobbyFullException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Lobby is full", null));
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse> leaveLobby(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found", null));
        }

        if (!lobbyService.isUserInLobby(user)) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "User is not in the lobby", null));
        }

        lobbyService.leaveLobby(user);
        return ResponseEntity.ok(new ApiResponse(true, "Left the lobby", null));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse> getLobbyUsers() {
        Lobby lobby = lobbyService.getLobby();
        return ResponseEntity.ok(new ApiResponse(true, "Lobby users retrieved successfully", lobby.getUsers()));
    }
}
