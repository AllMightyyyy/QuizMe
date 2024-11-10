package org.zakariafarih.quizme.util;

import org.springframework.boot.CommandLineRunner;
import org.zakariafarih.quizme.entity.*;
import org.zakariafarih.quizme.repository.RoleRepository;
import org.zakariafarih.quizme.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private QuizService quizService;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        loadRoles();
        loadUsers();
        loadQuizzes();
    }

    private void loadRoles() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
        }
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
        }
    }

    private void loadUsers() {
        if (userService.findByUsername("testuser") == null) {
            Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));
            User testUser = User.builder()
                    .username("testuser")
                    .password(passwordEncoder.encode("password123"))
                    .profilePhoto("default-profile.png")
                    .roles(Set.of(userRole))
                    .build();
            userService.registerUser(testUser);
        }

        if (userService.findByUsername("admin") == null) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Role ROLE_ADMIN not found"));
            User adminUser = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("adminpassword"))
                    .profilePhoto("default-admin.png")
                    .roles(Set.of(adminRole))
                    .build();
            userService.registerUser(adminUser);
        }
    }

    private void loadQuizzes() {
        if (quizService.getAllQuizzes().isEmpty()) {
            Quiz quiz = Quiz.builder()
                    .title("Sample Quiz")
                    .description("Test your knowledge!")
                    .build();

            Question question1 = Question.builder()
                    .content("What is the capital of France?")
                    .timeLimit(30L)
                    .quiz(quiz)
                    .build();

            Option option1 = Option.builder()
                    .text("Paris")
                    .isCorrect(true)
                    .question(question1)
                    .build();

            Option option2 = Option.builder()
                    .text("Berlin")
                    .isCorrect(false)
                    .question(question1)
                    .build();

            question1.setOptions(new HashSet<>(Arrays.asList(option1, option2)));

            quiz.setQuestions(new HashSet<>(Arrays.asList(question1)));

            quizService.createQuiz(quiz);
        }
    }
}
