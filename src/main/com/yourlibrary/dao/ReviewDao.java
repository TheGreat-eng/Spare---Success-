package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.BookReview;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    /**
     * Thêm hoặc cập nhật đánh giá của người dùng cho một cuốn sách.
     * Sử dụng INSERT ... ON DUPLICATE KEY UPDATE.
     * 
     * @param review Đối tượng BookReview chứa thông tin.
     * @return true nếu thành công, false nếu lỗi.
     */
    public boolean addOrUpdateReview(BookReview review) {
        // Câu lệnh này sẽ INSERT nếu cặp (user_id, book_id) chưa có,
        // hoặc UPDATE rating và review_text nếu cặp đó đã tồn tại.
        String sql = "INSERT INTO book_reviews (user_id, book_id, rating, review_text) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE rating = VALUES(rating), review_text = VALUES(review_text), reviewed_at = CURRENT_TIMESTAMP";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, review.getUserId());
            pstmt.setInt(2, review.getBookId());
            pstmt.setInt(3, review.getRating());

            if (review.getReviewText() != null && !review.getReviewText().trim().isEmpty()) {
                pstmt.setString(4, review.getReviewText().trim());
            } else {
                pstmt.setNull(4, Types.VARCHAR);
            }

            int rowsAffected = pstmt.executeUpdate();
            // executeUpdate trả về 1 cho INSERT thành công, 2 cho UPDATE thành công (trên
            // MySQL)
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm/cập nhật đánh giá: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    /**
     * Lấy danh sách tất cả đánh giá cho một cuốn sách cụ thể.
     * Bao gồm tên người đánh giá.
     * 
     * @param bookId ID của sách.
     * @return Danh sách các BookReview.
     */
    public List<BookReview> getReviewsByBook(int bookId) {
        List<BookReview> reviews = new ArrayList<>();
        String sql = "SELECT r.review_id, r.user_id, r.book_id, r.rating, r.review_text, r.reviewed_at, u.username AS reviewer_username "
                +
                "FROM book_reviews r JOIN users u ON r.user_id = u.user_id " +
                "WHERE r.book_id = ? ORDER BY r.reviewed_at DESC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                BookReview review = mapResultSetToReview(rs, true); // true = include username
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy đánh giá sách: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return reviews;
    }

    /**
     * Lấy đánh giá cụ thể của một người dùng cho một cuốn sách.
     * 
     * @param userId ID người dùng.
     * @param bookId ID sách.
     * @return Đối tượng BookReview nếu có, null nếu không.
     */
    public BookReview getReviewByUserAndBook(int userId, int bookId) {
        BookReview review = null;
        String sql = "SELECT review_id, user_id, book_id, rating, review_text, reviewed_at FROM book_reviews WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                review = mapResultSetToReview(rs, false); // false = không cần username
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy đánh giá cụ thể: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return review;
    }

    /**
     * Tính điểm đánh giá trung bình cho một cuốn sách.
     * 
     * @param bookId ID sách.
     * @return Điểm trung bình (double), hoặc 0.0 nếu chưa có đánh giá.
     */
    public double getAverageRatingForBook(int bookId) {
        String sql = "SELECT AVG(rating) AS avg_rating FROM book_reviews WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double average = 0.0;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                // AVG có thể trả về NULL nếu không có hàng nào, getDouble sẽ trả về 0
                average = rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tính đánh giá trung bình: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return average;
    }

    // --- Hàm map tiện ích ---
    private BookReview mapResultSetToReview(ResultSet rs, boolean includeUsername) throws SQLException {
        BookReview review = new BookReview();
        review.setReviewId(rs.getInt("review_id"));
        review.setUserId(rs.getInt("user_id"));
        review.setBookId(rs.getInt("book_id"));
        review.setRating(rs.getInt("rating"));
        review.setReviewText(rs.getString("review_text"));
        review.setReviewedAt(rs.getTimestamp("reviewed_at"));
        if (includeUsername) {
            review.setReviewerUsername(rs.getString("reviewer_username"));
        }
        return review;
    }
}