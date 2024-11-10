package org.zakariafarih.quizme.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zakariafarih.quizme.entity.*;
import org.zakariafarih.quizme.exception.RoleNotFoundException;
import org.zakariafarih.quizme.repository.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class AdminService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuizService quizService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private QuestionRepository questionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public void loadPredefinedData() throws IOException {
        JsonNode root = objectMapper.readTree(new ClassPathResource("sample-data.json").getFile());

        loadRoles(root.get("roles"));
        loadUsers(root.get("users"));
        loadQuizzes(root.get("quizzes"));
    }

    private void loadRoles(JsonNode rolesNode) {
        for (JsonNode roleName : rolesNode) {
            String role = roleName.asText();
            if (!roleRepository.findByName(role).isPresent()) {
                roleRepository.save(Role.builder().name(role).build());
            }
        }
    }

    private void loadUsers(JsonNode usersNode) {
        for (JsonNode userNode : usersNode) {
            String username = userNode.get("username").asText();
            if (userRepository.findByUsername(username).isEmpty()) {
                String password = userNode.get("password").asText();
                String profilePhoto = userNode.get("profilePhoto").asText();

                Set<Role> userRoles = new HashSet<>();
                for (JsonNode roleName : userNode.get("roles")) {
                    Role role = roleRepository.findByName(roleName.asText())
                            .orElseThrow(() -> new RoleNotFoundException("Role " + roleName.asText() + " not found"));
                    userRoles.add(role);
                }

                User user = User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(password))
                        .profilePhoto(profilePhoto)
                        .roles(userRoles)
                        .build();

                userRepository.save(user);
            }
        }
    }

    private void loadQuizzes(JsonNode quizzesNode) {
        for (JsonNode quizNode : quizzesNode) {
            String title = quizNode.get("title").asText();
            if (quizService.getAllQuizzes().stream().noneMatch(q -> q.getTitle().equals(title))) {
                String description = quizNode.get("description").asText();
                Quiz quiz = Quiz.builder()
                        .title(title)
                        .description(description)
                        .questions(new ArrayList<>())
                        .build();

                for (JsonNode questionNode : quizNode.get("questions")) {
                    String content = questionNode.get("content").asText();
                    Long timeLimit = questionNode.get("timeLimit").asLong();

                    Question question = Question.builder()
                            .content(content)
                            .timeLimit(timeLimit)
                            .quiz(quiz) // Set the back-reference to the quiz
                            .build();

                    for (JsonNode optionNode : questionNode.get("options")) {
                        String optionText = optionNode.get("text").asText();
                        boolean isCorrect = optionNode.get("isCorrect").asBoolean();

                        Option option = Option.builder()
                                .text(optionText)
                                .isCorrect(isCorrect)
                                .question(question) // Set the back-reference to the question
                                .build();

                        question.addOption(option); // This should set the question for the option
                    }

                    quiz.addQuestion(question); // Add question to quiz
                }

                quizService.createQuiz(quiz); // Save the entire quiz, which should cascade to save all questions and options
            }
        }
    }
}
