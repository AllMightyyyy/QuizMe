package org.zakariafarih.quizme.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.service.UserService;
import org.zakariafarih.quizme.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto
    ) {
        try {
            String photoPath = "default-profile.png";
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                // Implement photo saving logic here
            }

            User user = User.builder()
                    .username(username)
                    .password(password)
                    .profilePhoto(photoPath)
                    .build();

            userService.registerUser(user);

            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully");
            return ResponseEntity.ok(response);
        } catch (DataIntegrityViolationException e) {
            // Handle unique constraint violation (e.g., username already exists)
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Error registering user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody User loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            String token = jwtUtil.generateToken(authentication);
            Map<String, String> response = new HashMap<>();
            response.put("token", token);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid Credentials");
        }
    }
}
