package com.sms.gtbemployeemanagementsystem;

import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("gtb_admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("gtb_admin");
            admin.setPassword(passwordEncoder.encode("GTB123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("Seeded default admin account: gtb_admin");
        }
    }
}