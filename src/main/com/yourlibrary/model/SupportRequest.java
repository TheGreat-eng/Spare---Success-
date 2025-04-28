package main.com.yourlibrary.model;

import java.sql.Timestamp;

public class SupportRequest {
    private int requestId;
    private int userId; // ID người gửi
    private String subject;
    private String message;
    private String status;
    private Timestamp requestedAt;
    private Timestamp resolvedAt;
    private Integer resolverUserId; // Dùng Integer để cho phép giá trị NULL

    // Optional: Thêm các trường để hiển thị thông tin liên quan (lấy từ JOIN)
    private String requesterUsername;
    private String resolverUsername;

    // Constructors
    public SupportRequest() {
    }

    // Getters and Setters cho tất cả các thuộc tính (bao gồm cả optional)
    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Timestamp requestedAt) {
        this.requestedAt = requestedAt;
    }

    public Timestamp getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Timestamp resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Integer getResolverUserId() {
        return resolverUserId;
    }

    public void setResolverUserId(Integer resolverUserId) {
        this.resolverUserId = resolverUserId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public void setRequesterUsername(String requesterUsername) {
        this.requesterUsername = requesterUsername;
    }

    public String getResolverUsername() {
        return resolverUsername;
    }

    public void setResolverUsername(String resolverUsername) {
        this.resolverUsername = resolverUsername;
    }

    @Override
    public String toString() {
        return "SupportRequest{" + "requestId=" + requestId + ", userId=" + userId + ", subject='" + subject + '\''
                + ", status='" + status + '\'' + '}';
    }
}