package com.sms.gtbemployeemanagementsystem.Security;

import com.sms.gtbemployeemanagementsystem.Entity.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Component
    public static class SessionContext {

        private User currentUser;

        public User getCurrentUser() {
            return currentUser;
        }

        public void setCurrentUser(User user) {
            this.currentUser = user;
        }

        public boolean isAdmin() {
            return currentUser != null && "ADMIN".equalsIgnoreCase(currentUser.getRole());
        }

        public Long getCurrentEmployeeId() {
            if (currentUser != null && currentUser.getEmployee() != null) {
                return currentUser.getEmployee().getId();
            }
            return null;
        }
    }
}