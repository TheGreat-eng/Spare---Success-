package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.Book; // Cần Book để trả về sách mượn nhiều
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDao {

    /** Lấy tổng số đầu sách (distinct books) */
    public int getTotalBookCount() {
        String sql = "SELECT COUNT(DISTINCT book_id) AS total FROM books";
        return getSingleCountResult(sql);
    }

    /** Lấy tổng số bản sao sách (sum of quantity) */
    public int getTotalBookCopies() {
        String sql = "SELECT SUM(quantity) AS total FROM books";
        return getSingleCountResult(sql);
    }

    /** Lấy tổng số thành viên */
    public int getTotalMemberCount() {
        String sql = "SELECT COUNT(*) AS total FROM users";
        return getSingleCountResult(sql);
    }

    /** Lấy tổng số lượt mượn (tất cả, kể cả đã trả) */
    public int getTotalBorrowCount() {
        String sql = "SELECT COUNT(*) AS total FROM borrows";
        return getSingleCountResult(sql);
    }

    /** Lấy số sách đang được mượn */
    public int getCurrentlyBorrowedCount() {
        String sql = "SELECT COUNT(*) AS total FROM borrows WHERE status != 'RETURNED'";
        return getSingleCountResult(sql);
    }

    /**
     * Lấy danh sách các sách được mượn nhiều nhất.
     * 
     * @param limit Số lượng sách top cần lấy.
     * @return Danh sách các đối tượng Book, có thể thêm trường borrowCount nếu cần,
     *         hoặc dùng Map<Book, Integer>. Hiện tại trả về List<Book> đơn giản.
     */
    public List<Book> getMostBorrowedBooks(int limit) {
        List<Book> mostBorrowed = new ArrayList<>();
        // JOIN borrows với books, GROUP BY sách, COUNT số lượt mượn, ORDER DESC, LIMIT
        String sql = "SELECT b.book_id, b.isbn, b.title, b.author, b.genre, b.publication_year, " +
                "b.description, b.quantity, b.available_quantity, COUNT(br.loan_id) AS borrow_count " +
                "FROM books b JOIN borrows br ON b.book_id = br.book_id " +
                "GROUP BY b.book_id, b.isbn, b.title, b.author, b.genre, b.publication_year, b.description, b.quantity, b.available_quantity "
                + // Group by tất cả cột non-aggregate của books
                "ORDER BY borrow_count DESC " +
                "LIMIT ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BookDao tempBookDao = new BookDao(); // Để gọi hàm map

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, limit); // Đặt giới hạn
            rs = pstmt.executeQuery();

            while (rs.next()) {
                // Có thể tạo DTO BookBorrowStats(Book book, int borrowCount)
                // Hoặc đơn giản chỉ lấy thông tin Book
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                // Lấy thêm borrow_count nếu cần hiển thị
                // int borrowCount = rs.getInt("borrow_count");
                mostBorrowed.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy sách mượn nhiều nhất: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return mostBorrowed;
    }

    // --- Hàm tiện ích để lấy kết quả COUNT ---
    private int getSingleCountResult(String sql) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if (rs.next()) {
                count = rs.getInt("total"); // Lấy giá trị từ cột có alias là total
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi thực hiện query count: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
        return count;
    }
}