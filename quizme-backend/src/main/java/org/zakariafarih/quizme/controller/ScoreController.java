package org.zakariafarih.quizme.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.zakariafarih.quizme.dto.ApiResponse;
import org.zakariafarih.quizme.entity.Score;
import org.zakariafarih.quizme.repository.ScoreRepository;

import java.util.List;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

    @Autowired
    private ScoreRepository scoreRepository;

    @GetMapping("/leaderboard/{quizId}")
    public ResponseEntity<ApiResponse> getLeaderboard(@PathVariable Long quizId) {
        List<Score> leaderboard = scoreRepository.findAllByQuizIdOrderByPointsDesc(quizId);
        ApiResponse response = new ApiResponse(true, "Leaderboard retrieved successfully", leaderboard);
        return ResponseEntity.ok(response);
    }
}
