package org.zakariafarih.quizme.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "lobbies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lobby {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "lobby")
    private Set<User> users;
}
