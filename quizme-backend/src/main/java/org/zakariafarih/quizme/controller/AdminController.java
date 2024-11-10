package org.zakariafarih.quizme.controller;

import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.zakariafarih.quizme.dto.ApiResponse;
import org.zakariafarih.quizme.service.AdminService;
import org.zakariafarih.quizme.service.WebSocketService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private WebSocketService webSocketService;

    @Autowired
    private AdminService adminService;

    @PostMapping("/start-quiz")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> startQuiz(@RequestParam Long quizId) {
        webSocketService.initializeQuiz(quizId);
        ApiResponse response = new ApiResponse(true, "Quiz started successfully.", null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-quiz")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetQuiz() {
        webSocketService.resetQuiz();
        return ResponseEntity.ok("Quiz reset successfully.");
    }

    @PostMapping("/load-sample-data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> loadSampleData() {
        try {
            adminService.loadPredefinedData();
            ApiResponse response = new ApiResponse(true, "Sample data loaded successfully.", null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse errorResponse = new ApiResponse(false, "Error loading sample data: " + e.getMessage(), null);
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
