package org.zakariafarih.quizme.repository;

import org.zakariafarih.quizme.entity.Score;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Long> {
    Optional<Score> findByUserAndQuiz(User user, Quiz quiz);
    List<Score> findAllByQuizIdOrderByPointsDesc(Long quizId);
}
