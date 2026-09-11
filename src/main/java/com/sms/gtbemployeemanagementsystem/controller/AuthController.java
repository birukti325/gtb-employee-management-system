package com.sms.gtbemployeemanagementsystem.controller;

import com.sms.gtbemployeemanagementsystem.Entity.User;
import com.sms.gtbemployeemanagementsystem.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AuthController {

    @Autowired
    private AuthService authService;

    public Map<String, Object> register(User user) {
        return authService.register(user);
    }

    public Map<String, Object> login(String username, String password) {
        return authService.login(username, password);
    }

    public Map<String, String> logout(String token) {
        authService.logout(token);
        return Map.of("message", "Logged out successfully");
    }

    public User getCurrentUser(String token) {
        return authService.getCurrentUser(token);
    }
}