package com.sms.gtbemployeemanagementsystem.Entity;

import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String DEFAULT_EMPLOYEE_USERNAME = "gtb_user";
    private static final String DEFAULT_EMPLOYEE_PASSWORD = "gtb_password";

    private static final String DEFAULT_ADMIN_USERNAME = "GTB admin";
    private static final String DEFAULT_ADMIN_PASSWORD = "GTB123";

    @Override
    public void run(String... args) {
        seedIfMissing(DEFAULT_EMPLOYEE_USERNAME, DEFAULT_EMPLOYEE_PASSWORD, "EMPLOYEE");
        seedIfMissing(DEFAULT_ADMIN_USERNAME, DEFAULT_ADMIN_PASSWORD, "ADMIN");
    }

    private void seedIfMissing(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return; // already exists — don't overwrite a password the admin may have changed
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        userRepository.save(user);
    }
}