package org.zakariafarih.quizme.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zakariafarih.quizme.entity.Lobby;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.exception.LobbyFullException;
import org.zakariafarih.quizme.repository.LobbyRepository;

import java.util.HashSet;
import java.util.Optional;

@Service
public class LobbyService {

    @Autowired
    private LobbyRepository lobbyRepository;

    private static final int MAX_LOBBY_SIZE = 100;

    public synchronized Lobby getLobby() {
        Optional<Lobby> lobbyOpt = lobbyRepository.findAll().stream().findFirst();
        return lobbyOpt.orElseGet(() -> lobbyRepository.save(
                Lobby.builder().users(new HashSet<>()).build())
        );
    }

    @Transactional
    public void joinLobby(User user) {
        Lobby lobby = getLobby();

        if (isLobbyFull()) {
            throw new LobbyFullException("Lobby is full.");
        }

        lobby.getUsers().add(user);
        user.setLobby(lobby); // Set the lobby in the user entity
        lobbyRepository.save(lobby);
    }

    @Transactional
    public void leaveLobby(User user) {
        Lobby lobby = getLobby();
        lobby.getUsers().remove(user);
        user.setLobby(null); // Remove the lobby from the user entity
        lobbyRepository.save(lobby);
    }

    public boolean isUserInLobby(User user) {
        Lobby lobby = getLobby();
        return lobby.getUsers().contains(user);
    }

    public boolean isLobbyFull() {
        Lobby lobby = getLobby();
        return lobby.getUsers().size() >= MAX_LOBBY_SIZE;
    }
}
