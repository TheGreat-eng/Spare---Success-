package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.Book;
import java.sql.*; // Import SQLException và các lớp JDBC khác
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    /**
     * Lấy tất cả sách từ database, sắp xếp theo tiêu đề.
     * 
     * @return Danh sách các đối tượng Book.
     */
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, isbn, title, author, genre, publication_year, description, quantity, available_quantity FROM books ORDER BY title ASC";
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseUtil.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Book book = mapResultSetToBook(rs); // Tách logic tạo Book ra hàm riêng
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách sách: " + e.getMessage());
            e.printStackTrace(); // In chi tiết lỗi
        } finally {
            DatabaseUtil.close(rs, stmt, conn);
        }
        return books;
    }

    /**
     * Tìm kiếm sách dựa trên từ khóa (tiêu đề, tác giả, ISBN).
     * 
     * @param keyword Từ khóa tìm kiếm.
     * @return Danh sách các sách phù hợp.
     */
    public List<Book> searchBooks(String keyword) {
        List<Book> books = new ArrayList<>();
        // Sử dụng LIKE và PreparedStatement để tránh SQL Injection
        String sql = "SELECT book_id, isbn, title, author, genre, publication_year, description, quantity, available_quantity "
                +
                "FROM books WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? ORDER BY title ASC";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String searchPattern = "%" + keyword + "%"; // Thêm ký tự đại diện %

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, searchPattern); // title
            pstmt.setString(2, searchPattern); // author
            pstmt.setString(3, searchPattern); // isbn
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Book book = mapResultSetToBook(rs);
                books.add(book);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm kiếm sách: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return books;
    }

    /**
     * Thêm một cuốn sách mới vào database.
     * 
     * @param book Đối tượng Book chứa thông tin sách mới.
     * @return true nếu thêm thành công, false nếu thất bại.
     */
    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, genre, publication_year, description, quantity, available_quantity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getGenre());
            // Xử lý null cho năm xuất bản nếu cần (ví dụ, nếu nhập là 0 hoặc trống)
            if (book.getPublicationYear() > 0) {
                pstmt.setInt(5, book.getPublicationYear());
            } else {
                pstmt.setNull(5, Types.INTEGER); // Hoặc set giá trị mặc định nếu cột không cho phép NULL
            }
            pstmt.setString(6, book.getDescription());
            pstmt.setInt(7, book.getQuantity());
            pstmt.setInt(8, book.getAvailableQuantity());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0); // Thành công nếu có ít nhất 1 hàng bị ảnh hưởng

        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm sách: " + e.getMessage());
            // Kiểm tra lỗi UNIQUE constraint (trùng ISBN)
            if (e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().contains("isbn")) {
                System.err.println("Lỗi: ISBN '" + book.getIsbn() + "' đã tồn tại.");
                // Có thể throw một exception tùy chỉnh hoặc trả về mã lỗi cụ thể
            }
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    /**
     * Cập nhật thông tin một cuốn sách đã có.
     * 
     * @param book Đối tượng Book chứa thông tin cập nhật (phải có bookId).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateBook(Book book) {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, genre = ?, publication_year = ?, description = ?, quantity = ?, available_quantity = ? WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, book.getIsbn());
            pstmt.setString(2, book.getTitle());
            pstmt.setString(3, book.getAuthor());
            pstmt.setString(4, book.getGenre());
            if (book.getPublicationYear() > 0) {
                pstmt.setInt(5, book.getPublicationYear());
            } else {
                pstmt.setNull(5, Types.INTEGER);
            }
            pstmt.setString(6, book.getDescription());
            pstmt.setInt(7, book.getQuantity());
            pstmt.setInt(8, book.getAvailableQuantity());
            pstmt.setInt(9, book.getBookId()); // Điều kiện WHERE

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật sách: " + e.getMessage());
            if (e.getMessage().toLowerCase().contains("duplicate entry") && e.getMessage().contains("isbn")) {
                System.err.println("Lỗi: ISBN '" + book.getIsbn() + "' đã tồn tại cho sách khác.");
            }
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    /**
     * Xóa một cuốn sách khỏi database dựa trên bookId.
     * 
     * @param bookId ID của sách cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     */
    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa sách: " + e.getMessage());
            // Có thể cần kiểm tra Foreign Key Constraint nếu sách đang được mượn (cần bảng
            // `borrows` sau này)
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        return success;
    }

    /**
     * Lấy thông tin chi tiết của một cuốn sách dựa trên ID.
     * 
     * @param bookId ID của sách cần tìm.
     * @return Đối tượng Book nếu tìm thấy, null nếu không tìm thấy hoặc có lỗi.
     */
    public Book findBookById(int bookId) {
        String sql = "SELECT book_id, isbn, title, author, genre, publication_year, description, quantity, available_quantity FROM books WHERE book_id = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Book book = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, bookId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                book = mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sách theo ID: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return book;
    }

    // --- Hàm tiện ích ---
    /**
     * Ánh xạ một hàng từ ResultSet sang đối tượng Book.
     * 
     * @param rs ResultSet đang trỏ đến một hàng dữ liệu sách.
     * @return Đối tượng Book.
     * @throws SQLException Nếu có lỗi đọc từ ResultSet.
     */
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        book.setBookId(rs.getInt("book_id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setAuthor(rs.getString("author"));
        book.setGenre(rs.getString("genre"));
        book.setPublicationYear(rs.getInt("publication_year")); // getInt trả về 0 nếu giá trị là NULL
        book.setDescription(rs.getString("description"));
        book.setQuantity(rs.getInt("quantity"));
        book.setAvailableQuantity(rs.getInt("available_quantity"));
        // Lấy thêm created_at, updated_at nếu cần
        // book.setCreatedAt(rs.getTimestamp("created_at"));
        // book.setUpdatedAt(rs.getTimestamp("updated_at"));
        return book;
    }

    public Book findBookByIsbn(String isbn) {
        String sql = "SELECT book_id, isbn, title, author, genre, publication_year, description, quantity, available_quantity FROM books WHERE isbn = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Book book = null;

        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, isbn);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                // Dùng lại hàm map đã có
                book = mapResultSetToBook(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm sách theo ISBN: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return book;
    }
}