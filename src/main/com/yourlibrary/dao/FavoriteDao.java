package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.Book; // Cần Book model
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {

    /**
     * Thêm một cuốn sách vào danh sách yêu thích của người dùng.
     * 
     * @param userId ID người dùng
     * @param bookId ID sách
     * @return true nếu thêm thành công, false nếu đã tồn tại hoặc lỗi.
     */
    public boolean addFavorite(int userId, int bookId) {
        String sql = "INSERT INTO user_favorites (user_id, book_id) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
        } catch (SQLException e) {
            // Lỗi UNIQUE constraint nghĩa là đã tồn tại
            if (!e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("Lỗi khi thêm sách yêu thích: " + e.getMessage());
                e.printStackTrace();
            } else {
                System.out.println("Sách đã có trong danh sách yêu thích."); // Thông báo nhẹ nhàng
            }
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    /**
     * Xóa một cuốn sách khỏi danh sách yêu thích của người dùng.
     * 
     * @param userId ID người dùng
     * @param bookId ID sách
     * @return true nếu xóa thành công, false nếu không tìm thấy hoặc lỗi.
     */
    public boolean removeFavorite(int userId, int bookId) {
        String sql = "DELETE FROM user_favorites WHERE user_id = ? AND book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sách yêu thích: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    /**
     * Kiểm tra xem một cuốn sách có trong danh sách yêu thích của người dùng không.
     * 
     * @param userId ID người dùng
     * @param bookId ID sách
     * @return true nếu là sách yêu thích, false nếu không.
     */
    public boolean isFavorite(int userId, int bookId) {
        String sql = "SELECT 1 FROM user_favorites WHERE user_id = ? AND book_id = ? LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean isFav = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, bookId);
            rs = pstmt.executeQuery();
            isFav = rs.next(); // true nếu có kết quả
        } catch (SQLException e) {
            System.err.println("Lỗi khi kiểm tra sách yêu thích: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return isFav;
    }

    /**
     * Lấy danh sách các cuốn sách yêu thích của một người dùng.
     * 
     * @param userId ID người dùng
     * @return Danh sách các đối tượng Book.
     */
    public List<Book> getFavoriteBooksByUser(int userId) {
        List<Book> favoriteBooks = new ArrayList<>();
        String sql = "SELECT b.book_id, b.isbn, b.title, b.author, b.genre, b.publication_year, " +
                "b.description, b.quantity, b.available_quantity " +
                "FROM books b JOIN user_favorites uf ON b.book_id = uf.book_id " +
                "WHERE uf.user_id = ? ORDER BY b.title ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        BookDao tempBookDao = new BookDao(); // Tạm dùng để gọi hàm map

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                // Cần hàm map trong BookDao hoặc map trực tiếp ở đây
                // Để đơn giản, gọi lại hàm map của BookDao (cần làm nó public hoặc protected,
                // hoặc copy code)
                // Giả sử bạn có một hàm map public trong BookDao
                // Book book = tempBookDao.mapResultSetToBookPublic(rs); // Hoặc copy code map
                // vào đây
                // --- Cách map trực tiếp ---
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                book.setGenre(rs.getString("genre"));
                book.setPublicationYear(rs.getInt("publication_year"));
                book.setDescription(rs.getString("description"));
                book.setQuantity(rs.getInt("quantity"));
                book.setAvailableQuantity(rs.getInt("available_quantity"));
                // --- Kết thúc map ---
                favoriteBooks.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách sách yêu thích: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return favoriteBooks;
    }
}