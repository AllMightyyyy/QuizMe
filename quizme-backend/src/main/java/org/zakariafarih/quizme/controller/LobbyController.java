package org.zakariafarih.quizme.controller;

import org.zakariafarih.quizme.entity.Lobby;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.service.LobbyService;
import org.zakariafarih.quizme.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lobby")
public class LobbyController {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> joinLobby(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user != null) {
            lobbyService.joinLobby(user);
            return ResponseEntity.ok("Joined the lobby");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @PostMapping("/leave")
    public ResponseEntity<?> leaveLobby(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user != null) {
            lobbyService.leaveLobby(user);
            return ResponseEntity.ok("Left the lobby");
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getLobbyUsers() {
        Lobby lobby = lobbyService.getLobby();
        return ResponseEntity.ok(lobby.getUsers());
    }
}
