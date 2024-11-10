package org.zakariafarih.quizme.controller;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.zakariafarih.quizme.dto.ApiResponse;
import org.zakariafarih.quizme.dto.LoginRequest;
import org.zakariafarih.quizme.dto.RegistrationRequest;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.service.FileStorageService;
import org.zakariafarih.quizme.service.UserService;
import org.zakariafarih.quizme.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
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

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(
            @Valid @ModelAttribute RegistrationRequest registrationRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            ApiResponse errorResponse = new ApiResponse(false, "Validation errors", errors);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            String username = registrationRequest.getUsername();
            String password = registrationRequest.getPassword();
            MultipartFile profilePhoto = registrationRequest.getProfilePhoto();

            String photoPath = "default-profile.png";
            if (profilePhoto != null && !profilePhoto.isEmpty()) {
                photoPath = fileStorageService.storeFile(profilePhoto);
            }

            User user = User.builder()
                    .username(username)
                    .password(password)
                    .profilePhoto(photoPath)
                    .build();

            userService.registerUser(user);

            ApiResponse response = new ApiResponse(true, "User registered successfully", null);
            return ResponseEntity.ok(response);

        } catch (DataIntegrityViolationException e) {
            ApiResponse errorResponse = new ApiResponse(false, "Username already exists", null);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);

        } catch (Exception e) {
            e.printStackTrace();

            ApiResponse errorResponse = new ApiResponse(false, "Error registering user: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }
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
