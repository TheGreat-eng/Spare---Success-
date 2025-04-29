package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.SupportRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SupportRequestDao {

    /**
     * Thêm một yêu cầu hỗ trợ mới vào cơ sở dữ liệu.
     */
    public boolean addRequest(SupportRequest request) {
        if (request == null || request.getUserId() <= 0 ||
                request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            System.err.println("Thông tin yêu cầu hỗ trợ không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO support_requests (user_id, subject, message) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getUserId());
            pstmt.setString(2, request.getSubject() != null ? request.getSubject().trim() : null);
            pstmt.setString(3, request.getMessage().trim());

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi gửi yêu cầu hỗ trợ: " + e.getMessage());
            return false;
        }
    }

    /**
     * Lấy tất cả yêu cầu hỗ trợ (cho admin)
     */
    public List<SupportRequest> getAllRequests() {
        List<SupportRequest> requests = new ArrayList<>();
        String sql = "SELECT sr.*, u.username AS requester_username " +
                "FROM support_requests sr LEFT JOIN users u ON sr.user_id = u.user_id " +
                "ORDER BY sr.requested_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                requests.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả yêu cầu hỗ trợ: " + e.getMessage());
        }
        return requests;
    }

    /**
     * Lấy yêu cầu hỗ trợ theo user_id
     */
    public List<SupportRequest> getRequestsByUser(int userId) {
        List<SupportRequest> requests = new ArrayList<>();
        String sql = "SELECT sr.*, u.username AS requester_username " +
                "FROM support_requests sr LEFT JOIN users u ON sr.user_id = u.user_id " +
                "WHERE sr.user_id = ? ORDER BY sr.requested_at DESC";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(mapResultSetToRequest(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy yêu cầu theo user: " + e.getMessage());
        }
        return requests;
    }

    /**
     * Lấy yêu cầu hỗ trợ theo ID
     */
    public SupportRequest getRequestById(int requestId) {
        SupportRequest request = null;
        // Lấy thêm username người gửi và người giải quyết nếu cần hiển thị chi tiết hơn
        String sql = "SELECT sr.*, u_req.username AS requester_username, u_res.username AS resolver_username " +
                "FROM support_requests sr " +
                "JOIN users u_req ON sr.user_id = u_req.user_id " + // Join để lấy tên người gửi
                "LEFT JOIN users u_res ON sr.resolver_user_id = u_res.user_id " + // LEFT JOIN để lấy tên người giải
                                                                                  // quyết (có thể NULL)
                "WHERE sr.request_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        System.out.println("[SupportDao DEBUG] Executing getRequestById for requestId: " + requestId); // DEBUG

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, requestId);
            rs = pstmt.executeQuery();
            System.out.println("[SupportDao DEBUG] ResultSet obtained for getRequestById."); // DEBUG

            if (rs.next()) {
                System.out.println("[SupportDao DEBUG] Found request. Mapping details..."); // DEBUG
                // Gọi hàm map, yêu cầu lấy cả requester username
                request = mapResultSetToRequestWithResolver(rs); // Tạo hàm map mới nếu cần resolver username
            } else {
                System.out.println("[SupportDao DEBUG] No request found for ID: " + requestId); // DEBUG
            }
        } catch (SQLException e) {
            System.err.println("[SupportDao DEBUG] SQLException in getRequestById: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
            System.out.println("[SupportDao DEBUG] Resources closed for getRequestById."); // DEBUG
        }
        return request;
    }

    // --- Hàm map mới (hoặc sửa hàm cũ) để lấy cả resolver_username ---
    // Hàm map này sẽ được gọi bởi getRequestById
    private SupportRequest mapResultSetToRequestWithResolver(ResultSet rs) throws SQLException {
        SupportRequest request = new SupportRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setSubject(rs.getString("subject"));
        request.setMessage(rs.getString("message")); // Lấy nội dung message
        request.setStatus(rs.getString("status"));
        request.setRequestedAt(rs.getTimestamp("requested_at"));
        request.setResolvedAt(rs.getTimestamp("resolved_at"));
        request.setResolverUserId(rs.getObject("resolver_user_id", Integer.class));

        // Lấy tên từ JOIN
        request.setRequesterUsername(rs.getString("requester_username"));
        request.setResolverUsername(rs.getString("resolver_username")); // Có thể NULL

        System.out.println("[SupportDao MAP DEBUG] Mapped details for request #" + request.getRequestId()); // DEBUG
        return request;
    }

    /**
     * Cập nhật trạng thái yêu cầu hỗ trợ
     */
    public boolean updateRequestStatus(int requestId, String newStatus, int resolverUserId) {
        String sql = "UPDATE support_requests SET status = ?, resolver_user_id = ?, " +
                "resolved_at = ? WHERE request_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, resolverUserId);
            pstmt.setTimestamp(3,
                    "RESOLVED".equalsIgnoreCase(newStatus) ? new Timestamp(System.currentTimeMillis()) : null);
            pstmt.setInt(4, requestId);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái: " + e.getMessage());
            return false;
        }
    }

    /**
     * Ánh xạ ResultSet thành đối tượng SupportRequest
     */
    private SupportRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        SupportRequest request = new SupportRequest();
        request.setRequestId(rs.getInt("request_id"));
        request.setUserId(rs.getInt("user_id"));
        request.setSubject(rs.getString("subject"));
        request.setMessage(rs.getString("message"));
        request.setStatus(rs.getString("status"));
        request.setRequestedAt(rs.getTimestamp("requested_at"));
        request.setResolvedAt(rs.getTimestamp("resolved_at"));
        request.setResolverUserId(rs.getObject("resolver_user_id", Integer.class));
        request.setRequesterUsername(rs.getString("requester_username"));
        return request;
    }
}