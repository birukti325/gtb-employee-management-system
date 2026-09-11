package com.sms.gtbemployeemanagementsystem.Entity;

import com.sms.gtbemployeemanagementsystem.Entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserSession {

    private User loggedInUser;

    public void setLoggedInUser(User user) {
        this.loggedInUser = user;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public void clear() {
        this.loggedInUser = null;
    }

    public boolean isAdmin() {
        return loggedInUser != null && "ADMIN".equalsIgnoreCase(loggedInUser.getRole());
    }

    public Long getCurrentEmployeeId() {
        if (loggedInUser != null && loggedInUser.getEmployee() != null) {
            return loggedInUser.getEmployee().getId();
        }
        return null;
    }
}