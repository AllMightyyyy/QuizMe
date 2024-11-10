package org.zakariafarih.quizme.service;

import org.zakariafarih.quizme.entity.Lobby;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.repository.LobbyRepository;
import org.zakariafarih.quizme.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
public class LobbyService {

    @Autowired
    private LobbyRepository lobbyRepository;

    @Autowired
    private UserRepository userRepository;

    // Assuming a single lobby for simplicity
    public Lobby getLobby() {
        Optional<Lobby> lobbyOpt = lobbyRepository.findAll().stream().findFirst();
        if (lobbyOpt.isPresent()) {
            return lobbyOpt.get();
        } else {
            Lobby newLobby = Lobby.builder().users(new HashSet<>()).build();
            return lobbyRepository.save(newLobby);
        }
    }

    public void joinLobby(User user) {
        Lobby lobby = getLobby();
        lobby.getUsers().add(user);
        lobbyRepository.save(lobby);
    }

    public void leaveLobby(User user) {
        Lobby lobby = getLobby();
        lobby.getUsers().remove(user);
        lobbyRepository.save(lobby);
    }
}
