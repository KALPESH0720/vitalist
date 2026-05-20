package com.hospital.config;

import com.hospital.model.User;
import com.hospital.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Always upsert default users with freshly encoded passwords
        // This ensures password is always correct regardless of DB state
        upsertUser("admin",        "password", "System Admin",   User.Role.ADMIN,        "admin@hospital.com");
        upsertUser("dr.smith",     "password", "Dr. John Smith", User.Role.DOCTOR,       "smith@hospital.com");
        upsertUser("receptionist", "password", "Sarah Johnson",  User.Role.RECEPTIONIST, "sarah@hospital.com");
        System.out.println("[HMS] Default users ready. Login: admin/password | dr.smith/password | receptionist/password");
    }

    private void upsertUser(String username, String rawPassword, String fullName,
                             User.Role role, String email) {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        userRepository.findByUsername(username).ifPresentOrElse(
            existing -> {
                // Always update password hash to ensure it matches current encoder
                existing.setPassword(encodedPassword);
                existing.setFullName(fullName);
                existing.setRole(role);
                existing.setActive(true);
                userRepository.save(existing);
                System.out.println("[HMS] Updated user: " + username);
            },
            () -> {
                User user = User.builder()
                    .username(username)
                    .password(encodedPassword)
                    .fullName(fullName)
                    .role(role)
                    .email(email)
                    .active(true)
                    .build();
                userRepository.save(user);
                System.out.println("[HMS] Created user: " + username);
            }
        );
    }
}
