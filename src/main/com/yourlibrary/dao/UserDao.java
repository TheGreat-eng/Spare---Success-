package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// Thêm thư viện băm mật khẩu (Ví dụ: BCrypt)
// Bạn cần thêm thư viện này vào Build Path giống như JDBC driver
// Ví dụ dùng jBCrypt: https://github.com/patrickfav/bcrypt
// import org.mindrot.jbcrypt.BCrypt; // Bỏ comment nếu dùng jBCrypt

public class UserDao {

    /**
     * Xác thực người dùng dựa trên username và password (dạng rõ).
     * So sánh password nhập vào với password hash trong database.
     * 
     * @param username      Tên đăng nhập
     * @param plainPassword Mật khẩu dạng rõ người dùng nhập
     * @return Đối tượng User nếu xác thực thành công, null nếu thất bại hoặc có
     *         lỗi.
     */
    public User authenticateUser(String username, String plainPassword) {
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
                String storedHash = rs.getString("password_hash");

                // --- PHẦN QUAN TRỌNG: SO SÁNH MẬT KHẨU ---
                // Bạn cần sử dụng thư viện băm mật khẩu ở đây. Ví dụ với jBCrypt:
                // if (BCrypt.checkpw(plainPassword, storedHash)) { // Bỏ comment nếu dùng
                // jBCrypt
                // --- Giả định tạm thời là so sánh chuỗi (KHÔNG AN TOÀN, CHỈ ĐỂ TEST BAN ĐẦU)
                // ---
                if (plainPassword.equals(storedHash)) { // <<< !!! THAY BẰNG HÀM CHECK BĂM !!!
                    // --- Kết thúc phần giả định ---
                    user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setPasswordHash(storedHash); // Lưu hash, không lưu plain text
                    user.setEmail(rs.getString("email"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRole(rs.getString("role"));
                    // Lấy thêm các cột khác nếu cần (createdAt, updatedAt...)
                }
                // } // Đóng if của BCrypt.checkpw
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xác thực người dùng: " + e.getMessage());
            // Xử lý lỗi tốt hơn trong ứng dụng thực tế (ví dụ: logging)
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return user;
    }

    /**
     * TODO: Thêm các phương thức khác sau này
     * - public boolean addUser(User user) { ... } // Nhớ băm mật khẩu trước khi lưu
     * - public boolean updateUserProfile(User user) { ... }
     * - public User findUserById(int userId) { ... }
     * - public boolean changePassword(int userId, String oldPassword, String
     * newPassword) { ... }
     */

    // --- Hàm tiện ích băm mật khẩu (Nên đặt ở lớp Util riêng hoặc dùng thư viện)
    // ---
    /*
     * // Bỏ comment nếu dùng jBCrypt
     * public static String hashPassword(String plainPassword) {
     * return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
     * }
     */
}