package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.SupportRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types; // Để dùng cho setNull

public class SupportRequestDao {

    /**
     * Thêm một yêu cầu hỗ trợ mới vào cơ sở dữ liệu.
     * 
     * @param request Đối tượng SupportRequest chứa thông tin cần lưu.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addRequest(SupportRequest request) {
        if (request == null || request.getUserId() <= 0 || request.getMessage() == null
                || request.getMessage().trim().isEmpty()) {
            System.err.println("Thông tin yêu cầu hỗ trợ không hợp lệ.");
            return false;
        }

        // Chỉ lưu các trường do người dùng nhập, các trường khác có default hoặc sẽ
        // null ban đầu
        String sql = "INSERT INTO support_requests (user_id, subject, message) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, request.getUserId());

            // Xử lý subject có thể null
            if (request.getSubject() != null && !request.getSubject().trim().isEmpty()) {
                pstmt.setString(2, request.getSubject().trim());
            } else {
                pstmt.setNull(2, Types.VARCHAR); // Đặt giá trị NULL cho cột subject
            }

            pstmt.setString(3, request.getMessage().trim());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi khi gửi yêu cầu hỗ trợ: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    // --- TODO: Thêm các phương thức khác sau này ---
    // public List<SupportRequest> getAllRequests() { ... }
    // public boolean updateRequestStatus(int requestId, String status, int
    // resolverUserId) { ... }
    // ... các phương thức khác ...
}