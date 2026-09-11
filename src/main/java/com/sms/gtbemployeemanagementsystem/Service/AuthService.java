package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Repository.UserRepository;
import com.sms.gtbemployeemanagementsystem.Security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> register(User user) {
        // Hash password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User saved = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User registered successfully");
        response.put("username", saved.getUsername());
        return response;
    }

    public Map<String, Object> login(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login successful");
        response.put("token", token);
        response.put("username", user.getUsername());

        // Hardcoded to match our Security configuration fallback
        response.put("role", "ADMIN");
        return response;
    }

    public void logout(String token) {
        // Stateless JWT — nothing to do server-side
        // Client just discards the token
    }

    public User getCurrentUser(String token) {
        String bearerToken = token.replace("Bearer ", "");
        String username = jwtUtil.extractUsername(bearerToken);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}