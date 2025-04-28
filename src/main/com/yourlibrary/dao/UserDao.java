package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

    // --- Xác thực người dùng (SO SÁNH PLAIN TEXT) ---
    public User authenticateUser(String username, String plainPassword) {
        // Đổi tên cột password_hash thành password để rõ ràng hơn (TÙY CHỌN, nếu bạn
        // đổi cả trong DB)
        // Nếu giữ nguyên tên cột là password_hash thì dùng tên đó trong SQL.
        String sql = "SELECT user_id, username, password_hash, email, full_name, role FROM users WHERE username = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Lấy mật khẩu đã lưu (dạng plain text)
                String storedPassword = rs.getString("password_hash"); // Hoặc rs.getString("password") nếu đổi tên cột

                // --- SO SÁNH TRỰC TIẾP PLAIN TEXT ---
                if (plainPassword.equals(storedPassword)) { // <- So sánh chuỗi trực tiếp
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(storedPassword); // Lưu lại mật khẩu plain text (không an toàn!)
                    user.setEmail(rs.getString("email"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                }
                // --- KẾT THÚC SO SÁNH ---
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xác thực người dùng: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return user;
    }

    // --- Thêm người dùng mới (LƯU PLAIN TEXT) ---
    public boolean addUser(User user, String plainPassword) {
        if (user == null || user.getUsername() == null || user.getEmail() == null || plainPassword == null
                || plainPassword.isEmpty()) {
            System.err.println("Thông tin thêm người dùng không hợp lệ.");
            return false;
        }

        String sql = "INSERT INTO users (username, password_hash, email, full_name, role) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, plainPassword); // <- Lưu trực tiếp mật khẩu plain text
            pstmt.setString(3, user.getEmail());
            pstmt.setString(4, user.getFullName());
            pstmt.setString(5, user.getRole() != null ? user.getRole() : "READER");

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm người dùng: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("Lỗi: Username hoặc Email đã tồn tại.");
            }
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    // --- Cập nhật thông tin hồ sơ (không gồm mật khẩu) ---
    // Phương thức này không thay đổi so với phiên bản trước
    public boolean updateUserProfile(User user) {
        if (user == null || user.getUserId() <= 0) {
            System.err.println("Thông tin cập nhật người dùng không hợp lệ.");
            return false;
        }
        String sql = "UPDATE users SET email = ?, full_name = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, user.getEmail());
            pstmt.setString(2, user.getFullName());
            pstmt.setInt(3, user.getUserId());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật hồ sơ người dùng: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().contains("email")) {
                System.err.println("Lỗi: Email '" + user.getEmail() + "' đã tồn tại cho tài khoản khác.");
            }
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    // --- Thay đổi mật khẩu (LƯU PLAIN TEXT MỚI) ---
    public boolean changePassword(int userId, String oldPlainPassword, String newPlainPassword) {
        if (userId <= 0 || oldPlainPassword == null || newPlainPassword == null || newPlainPassword.isEmpty()) {
            System.err.println("Thông tin đổi mật khẩu không hợp lệ.");
            return false;
        }

        String sqlSelect = "SELECT password_hash FROM users WHERE user_id = ?"; // Lấy mật khẩu cũ (plain text)
        String sqlUpdate = "UPDATE users SET password_hash = ? WHERE user_id = ?"; // Cập nhật mật khẩu mới (plain text)
        Connection conn = null;
        PreparedStatement pstmtSelect = null;
        PreparedStatement pstmtUpdate = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            // Bước 1: Lấy mật khẩu cũ (plain text) và kiểm tra
            pstmtSelect = conn.prepareStatement(sqlSelect);
            pstmtSelect.setInt(1, userId);
            rs = pstmtSelect.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");
                // Kiểm tra mật khẩu cũ có đúng không (so sánh plain text)
                if (oldPlainPassword.equals(storedPassword)) {
                    // Bước 2: Nếu đúng, cập nhật mật khẩu mới (dạng plain text)
                    pstmtUpdate = conn.prepareStatement(sqlUpdate);
                    pstmtUpdate.setString(1, newPlainPassword); // <- Lưu plain text mới
                    pstmtUpdate.setInt(2, userId);

                    int rowsAffected = pstmtUpdate.executeUpdate();
                    success = (rowsAffected > 0);
                } else {
                    // Mật khẩu cũ không đúng
                    System.err.println("Mật khẩu cũ không chính xác cho user ID: " + userId);
                }
            } else {
                // Không tìm thấy user
                System.err.println("Không tìm thấy người dùng với ID: " + userId);
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi thay đổi mật khẩu: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng tất cả tài nguyên
            DatabaseUtil.close(rs, pstmtSelect, conn); // Đóng riêng select
            // Đảm bảo conn chỉ đóng 1 lần nếu cả select và update cùng dùng nó
            Connection connForUpdate = null; // Biến tạm để đóng pstmtUpdate
            try {
                if (pstmtUpdate != null) {
                    connForUpdate = pstmtUpdate.getConnection(); // Lấy connection từ update statement nếu có
                    pstmtUpdate.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            // Không cần đóng connForUpdate vì nó chính là conn đã đóng ở close đầu tiên
        }
        return success;
    }

    // --- Lấy thông tin User theo ID ---
    // Phương thức này không thay đổi so với phiên bản trước
    public User findUserById(int userId) {
        String sql = "SELECT user_id, username, password_hash, email, full_name, role FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        User user = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash")); // Lấy mật khẩu plain text
                user.setEmail(rs.getString("email"));
                user.setFullName(rs.getString("full_name"));
                user.setRole(rs.getString("role"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm người dùng theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return user;
    }
}