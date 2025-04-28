package main.com.yourlibrary.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseUtil {

    // --- THAY ĐỔI CÁC THÔNG SỐ NÀY CHO PHÙ HỢP VỚI CẤU HÌNH MYSQL CỦA BẠN ---
    private static final String DB_URL = "jdbc:mysql://localhost:3306/library_db"; // Thay library_db nếu bạn đặt tên
                                                                                   // khác
    private static final String DB_USER = "root"; // Thay bằng username MySQL của bạn
    private static final String DB_PASSWORD = "root"; // Thay bằng mật khẩu MySQL của bạn
    // ---------------------------------------------------------------------

    // Optional: Tải driver một lần khi lớp được nạp
    static {
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver"); // Cần thiết cho JDBC cũ, thường
            // không cần với JDBC 4.0+ nhưng để cho chắc
        } catch (Exception e) {
            System.err.println("Không thể tải MySQL JDBC Driver!");
            e.printStackTrace();
        }
    }

    /**
     * Lấy một kết nối đến cơ sở dữ liệu.
     * Người gọi có trách nhiệm đóng kết nối này sau khi sử dụng xong.
     * 
     * @return Connection object hoặc null nếu có lỗi.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    /**
     * Đóng các tài nguyên JDBC một cách an toàn.
     * 
     * @param rs   ResultSet (có thể null)
     * @param stmt Statement hoặc PreparedStatement (có thể null)
     * @param conn Connection (có thể null)
     */
    public static void close(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (stmt != null)
                stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Đóng Statement và Connection một cách an toàn.
     * 
     * @param stmt Statement hoặc PreparedStatement (có thể null)
     * @param conn Connection (có thể null)
     */
    public static void close(Statement stmt, Connection conn) {
        close(null, stmt, conn); // Gọi lại hàm close trên với ResultSet là null
    }
}