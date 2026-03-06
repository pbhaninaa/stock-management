package com.example.finance;

public class User {
    private final long id;
    private final String fullName;
    private final String username;
    private final String role;
    private final boolean active;
    private final String createdAt;

    public User(long id, String fullName, String username, String role, boolean active, String createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
