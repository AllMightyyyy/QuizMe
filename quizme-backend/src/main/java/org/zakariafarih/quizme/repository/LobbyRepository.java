package org.zakariafarih.quizme.repository;

import org.zakariafarih.quizme.entity.Lobby;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LobbyRepository extends JpaRepository<Lobby, Long> {
    // Additional query methods if needed
}
