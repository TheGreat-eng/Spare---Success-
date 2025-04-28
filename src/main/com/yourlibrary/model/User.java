// File: model/User.java
package main.com.yourlibrary.model;

import java.sql.Timestamp; // Dùng nếu bạn có cột created_at/updated_at

public class User {
    private int userId;
    private String username;
    private String passwordHash; // Chỉ lưu hash, không lưu mật khẩu thật
    private String email;
    private String fullName;
    private String role;
    private Timestamp createdAt; // Optional
    private Timestamp updatedAt; // Optional

    // Constructors (ít nhất một constructor mặc định và/hoặc một constructor đầy
    // đủ)
    public User() {
    }

    public User(int userId, String username, String passwordHash, String email, String fullName, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    // Getters and Setters cho tất cả các thuộc tính
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() { // Hữu ích cho debugging
        return "User{" + "userId=" + userId + ", username='" + username + '\'' + ", role='" + role + '\'' + '}';
    }
}
