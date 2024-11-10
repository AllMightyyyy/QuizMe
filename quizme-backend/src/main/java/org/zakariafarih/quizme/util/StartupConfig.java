package org.zakariafarih.quizme.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zakariafarih.quizme.entity.Role;
import org.zakariafarih.quizme.entity.User;
import org.zakariafarih.quizme.repository.RoleRepository;
import org.zakariafarih.quizme.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class StartupConfig {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public StartupConfig(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        if (roleRepository.findByName("ROLE_USER").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_USER"));
        }

        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role(null, "ROLE_ADMIN"));
        }
    }

    private void initializeAdminUser() {
        String adminUsername = "admin";

        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Admin role not found."));

            User adminUser = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode("adminpassword"))  // Replace with a secure password
                    .profilePhoto("default-admin.png")
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            userRepository.save(adminUser);
            System.out.println("Admin user created with username: 'admin' and password: 'adminpassword'");
        }
    }
}
