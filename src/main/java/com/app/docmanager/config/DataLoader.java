package com.app.docmanager.config;

import com.app.docmanager.entity.Role;
import com.app.docmanager.entity.User;
import com.app.docmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            loadInitialData();
        }
    }

    private void loadInitialData() {
        log.info("Loading initial data...");

        // Create admin user
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(Role.ROLE_ADMIN);
        adminRoles.add(Role.ROLE_USER);

        User admin = User.builder()
                .username("admin")
                .email("admin@docmanager.com")
                .password(passwordEncoder.encode("T3st1ng"))
                .firstName("Bernhard")
                .lastName("Scheucher")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(adminRoles)
                .build();

        userRepository.save(admin);
        log.info("Created admin user: admin / admin123");

        // Create test user
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(Role.ROLE_USER);

        User testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(userRoles)
                .build();

        userRepository.save(testUser);
        log.info("Created test user: testuser / password123");

        log.info("Initial data loading completed.");
    }
}