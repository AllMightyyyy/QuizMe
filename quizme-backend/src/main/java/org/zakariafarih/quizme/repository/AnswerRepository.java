package org.zakariafarih.quizme.repository;

import org.zakariafarih.quizme.entity.Answer;
import org.zakariafarih.quizme.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    List<Answer> findByUser(User user);
}
