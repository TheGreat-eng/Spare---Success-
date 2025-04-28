package main.com.yourlibrary.dao;

import main.com.yourlibrary.model.BorrowRecord;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
// import java.sql.Date; // Import nếu không dùng *

public class BorrowDao {

    private static final String FIND_ACTIVE_LOANS_SQL = "SELECT br.loan_id, br.user_id, br.book_id, br.borrow_date, br.due_date, br.return_date, br.status, "
            +
            "b.title AS book_title, b.isbn AS book_isbn " +
            "FROM borrows br JOIN books b ON br.book_id = b.book_id " +
            "WHERE br.user_id = ? AND (br.status = 'BORROWED' OR br.status = 'OVERDUE') " +
            "ORDER BY br.due_date ASC";

    private static final String LOAN_HISTORY_SQL = "SELECT br.loan_id, br.user_id, br.book_id, br.borrow_date, br.due_date, br.return_date, br.status, "
            +
            "b.title AS book_title, b.isbn AS book_isbn " +
            "FROM borrows br JOIN books b ON br.book_id = b.book_id " +
            "WHERE br.user_id = ? ORDER BY br.borrow_date DESC";

    // --- Borrow Book (with Transaction) ---
    public boolean borrowBook(int userId, int bookId, Date dueDate) {
        Connection conn = null;
        PreparedStatement pstmtCheck = null, pstmtUpdateBook = null, pstmtInsertBorrow = null;
        ResultSet rsCheck = null;
        boolean success = false;
        String checkSql = "SELECT available_quantity FROM books WHERE book_id = ? FOR UPDATE";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity - 1 WHERE book_id = ? AND available_quantity > 0";
        String insertBorrowSql = "INSERT INTO borrows (user_id, book_id, due_date, status) VALUES (?, ?, ?, 'BORROWED')";

        System.out.println("[BorrowDao] Attempting to borrow bookId: " + bookId + " for userId: " + userId); // DEBUG

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            System.out.println("[BorrowDao] Transaction started."); // DEBUG

            // Step 1: Check and Lock book
            pstmtCheck = conn.prepareStatement(checkSql);
            pstmtCheck.setInt(1, bookId);
            rsCheck = pstmtCheck.executeQuery();
            int availableQuantity = 0;
            if (rsCheck.next())
                availableQuantity = rsCheck.getInt("available_quantity");
            System.out.println("[BorrowDao] BookId: " + bookId + ", Available Quantity: " + availableQuantity); // DEBUG

            if (availableQuantity <= 0) {
                System.err.println("[BorrowDao] Book (ID: " + bookId + ") is out of stock or does not exist.");
                conn.rollback();
                System.out.println("[BorrowDao] Transaction rolled back (out of stock)."); // DEBUG
                return false;
            }

            // Step 2: Decrease available quantity
            pstmtUpdateBook = conn.prepareStatement(updateBookSql);
            pstmtUpdateBook.setInt(1, bookId);
            int bookRowsAffected = pstmtUpdateBook.executeUpdate();
            System.out.println("[BorrowDao] Updated book quantity affected rows: " + bookRowsAffected); // DEBUG

            if (bookRowsAffected <= 0) {
                System.err.println("[BorrowDao] Failed to update book quantity (ID: " + bookId
                        + "). Maybe it became unavailable.");
                conn.rollback();
                System.out.println("[BorrowDao] Transaction rolled back (update book failed)."); // DEBUG
                return false;
            }

            // Step 3: Insert borrow record
            pstmtInsertBorrow = conn.prepareStatement(insertBorrowSql);
            pstmtInsertBorrow.setInt(1, userId);
            pstmtInsertBorrow.setInt(2, bookId);
            pstmtInsertBorrow.setDate(3, dueDate);
            int borrowRowsAffected = pstmtInsertBorrow.executeUpdate();
            System.out.println("[BorrowDao] Inserted borrow record affected rows: " + borrowRowsAffected); // DEBUG

            if (borrowRowsAffected > 0) {
                conn.commit();
                System.out.println("[BorrowDao] Transaction committed successfully."); // DEBUG
                success = true;
            } else {
                System.err.println("[BorrowDao] Failed to insert borrow record.");
                conn.rollback();
                System.out.println("[BorrowDao] Transaction rolled back (insert borrow failed)."); // DEBUG
            }

        } catch (SQLException e) {
            System.err.println("[BorrowDao] SQLException during borrow transaction: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("[BorrowDao] Transaction rolled back due to SQLException.");
                }
            } catch (SQLException ex) {
                System.err.println("[BorrowDao] Error during rollback: " + ex.getMessage());
            }
        } finally {
            DatabaseUtil.close(rsCheck, pstmtCheck, null);
            try {
                if (pstmtUpdateBook != null)
                    pstmtUpdateBook.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (pstmtInsertBorrow != null)
                    pstmtInsertBorrow.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[BorrowDao] borrowBook finished. Result: " + success); // DEBUG
        return success;
    }

    // --- Return Book (with Transaction) ---
    public boolean returnBook(int loanId) {
        Connection conn = null;
        PreparedStatement pstmtFindBookId = null, pstmtUpdateBorrow = null, pstmtUpdateBook = null;
        ResultSet rsFindBookId = null;
        boolean success = false;
        String findBookSql = "SELECT book_id FROM borrows WHERE loan_id = ? AND status != 'RETURNED' FOR UPDATE"; // Lock
                                                                                                                  // borrow
                                                                                                                  // record
        String updateBorrowSql = "UPDATE borrows SET return_date = CURDATE(), status = 'RETURNED' WHERE loan_id = ?";
        String updateBookSql = "UPDATE books SET available_quantity = available_quantity + 1 WHERE book_id = ?";

        System.out.println("[BorrowDao] Attempting to return loanId: " + loanId); // DEBUG

        try {
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);
            System.out.println("[BorrowDao] Return transaction started."); // DEBUG

            // Step 1: Find book_id and lock borrow record
            pstmtFindBookId = conn.prepareStatement(findBookSql);
            pstmtFindBookId.setInt(1, loanId);
            rsFindBookId = pstmtFindBookId.executeQuery();
            int bookId = -1;
            if (rsFindBookId.next())
                bookId = rsFindBookId.getInt("book_id");
            System.out.println("[BorrowDao] Found bookId: " + bookId + " for loanId: " + loanId); // DEBUG

            if (bookId <= 0) {
                System.err.println("[BorrowDao] Active loan not found for ID: " + loanId);
                conn.rollback();
                System.out.println("[BorrowDao] Return transaction rolled back (loan not found/already returned)."); // DEBUG
                return false;
            }

            // Step 2: Update borrow record
            pstmtUpdateBorrow = conn.prepareStatement(updateBorrowSql);
            pstmtUpdateBorrow.setInt(1, loanId);
            int borrowRowsAffected = pstmtUpdateBorrow.executeUpdate();
            System.out.println("[BorrowDao] Updated borrow record affected rows: " + borrowRowsAffected); // DEBUG

            if (borrowRowsAffected <= 0) {
                System.err.println("[BorrowDao] Failed to update borrow record (ID: " + loanId + ").");
                conn.rollback();
                System.out.println("[BorrowDao] Return transaction rolled back (update borrow failed)."); // DEBUG
                return false;
            }

            // Step 3: Increase available quantity
            pstmtUpdateBook = conn.prepareStatement(updateBookSql);
            pstmtUpdateBook.setInt(1, bookId);
            int bookRowsAffected = pstmtUpdateBook.executeUpdate();
            System.out.println("[BorrowDao] Updated book quantity affected rows: " + bookRowsAffected); // DEBUG

            if (bookRowsAffected > 0) {
                conn.commit();
                System.out.println("[BorrowDao] Return transaction committed successfully."); // DEBUG
                success = true;
            } else {
                // This case is less critical, maybe log a warning but commit the return?
                // For consistency, we rollback.
                System.err.println("[BorrowDao] Failed to update book quantity (ID: " + bookId + ") on return.");
                conn.rollback();
                System.out.println("[BorrowDao] Return transaction rolled back (update book failed)."); // DEBUG
            }

        } catch (SQLException e) {
            System.err.println("[BorrowDao] SQLException during return transaction: " + e.getMessage());
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("[BorrowDao] Return transaction rolled back due to SQLException.");
                }
            } catch (SQLException ex) {
                System.err.println("[BorrowDao] Error during rollback: " + ex.getMessage());
            }
        } finally {
            DatabaseUtil.close(rsFindBookId, pstmtFindBookId, null);
            try {
                if (pstmtUpdateBorrow != null)
                    pstmtUpdateBorrow.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (pstmtUpdateBook != null)
                    pstmtUpdateBook.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[BorrowDao] returnBook finished. Result: " + success); // DEBUG
        return success;
    }

    // --- Renew Loan ---
    public boolean renewLoan(int loanId, Date newDueDate) {
        String sql = "UPDATE borrows SET due_date = ? WHERE loan_id = ? AND status != 'RETURNED'";
        Connection conn = null;
        PreparedStatement pstmt = null;
        boolean success = false;
        System.out.println("[BorrowDao] Attempting to renew loanId: " + loanId + " to " + newDueDate); // DEBUG
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setDate(1, newDueDate);
            pstmt.setInt(2, loanId);
            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);
            if (!success)
                System.err.println(
                        "[BorrowDao] Failed to renew loan (ID: " + loanId + "). Already returned or not found.");
        } catch (SQLException e) {
            System.err.println("[BorrowDao] SQLException during renewLoan: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(pstmt, conn);
        }
        System.out.println("[BorrowDao] renewLoan finished. Result: " + success); // DEBUG
        return success;
    }

    // --- Find Active Loans By User ---
    public List<BorrowRecord> findActiveLoansByUser(int userId) {
        System.out.println("[BorrowDao] Executing findActiveLoansByUser for userId: " + userId);
        List<BorrowRecord> activeLoans = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(FIND_ACTIVE_LOANS_SQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("[BorrowDao] Executed query: " + FIND_ACTIVE_LOANS_SQL);

                while (rs.next()) {
                    try {
                        BorrowRecord record = mapResultSetToBorrowRecord(rs, true);
                        System.out.println("[BorrowDao] Mapped record: " + record);
                        activeLoans.add(record);
                    } catch (SQLException e) {
                        System.err.println("[BorrowDao] Error mapping record: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BorrowDao] SQLException in findActiveLoansByUser: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[BorrowDao] Returning " + activeLoans.size() + " active loans");
        return activeLoans;
    }

    // --- Get Loan History By User ---
    public List<BorrowRecord> getLoanHistoryByUser(int userId) {
        System.out.println("[BorrowDao] Executing getLoanHistoryByUser for userId: " + userId);
        List<BorrowRecord> history = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(LOAN_HISTORY_SQL)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("[BorrowDao] Executed query: " + LOAN_HISTORY_SQL);

                while (rs.next()) {
                    try {
                        BorrowRecord record = mapResultSetToBorrowRecord(rs, true);
                        System.out.println("[BorrowDao] Mapped history record: " + record);
                        history.add(record);
                    } catch (SQLException e) {
                        System.err.println("[BorrowDao] Error mapping history record: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[BorrowDao] SQLException in getLoanHistoryByUser: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("[BorrowDao] Returning " + history.size() + " history records");
        return history;
    }

    // --- Check if User Has Active Loans ---
    public boolean hasActiveLoans(int userId) {
        String sql = "SELECT 1 FROM borrows WHERE user_id = ? AND status != 'RETURNED' LIMIT 1";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        boolean hasActive = false;
        System.out.println("[BorrowDao] Executing hasActiveLoans for userId: " + userId); // DEBUG
        try {
            conn = DatabaseUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            rs = pstmt.executeQuery();
            hasActive = rs.next();
            System.out.println("[BorrowDao] hasActiveLoans result: " + hasActive); // DEBUG
        } catch (SQLException e) {
            System.err.println("[BorrowDao] SQLException in hasActiveLoans: " + e.getMessage());
            e.printStackTrace();
        } finally {
            DatabaseUtil.close(rs, pstmt, conn);
        }
        return hasActive;
    }

    // --- Helper Method to Map ResultSet ---
    private BorrowRecord mapResultSetToBorrowRecord(ResultSet rs, boolean includeBookDetails) throws SQLException {
        BorrowRecord record = new BorrowRecord();

        // Required fields
        record.setLoanId(rs.getInt("loan_id"));
        record.setUserId(rs.getInt("user_id"));
        record.setBookId(rs.getInt("book_id"));
        record.setStatus(rs.getString("status"));

        // Date fields with null checks
        Timestamp borrowTimestamp = rs.getTimestamp("borrow_date");
        if (!rs.wasNull()) {
            record.setBorrowDate(borrowTimestamp);
        }

        Date dueDate = rs.getDate("due_date");
        if (!rs.wasNull()) {
            record.setDueDate(dueDate);
        }

        Date returnDate = rs.getDate("return_date");
        if (!rs.wasNull()) {
            record.setReturnDate(returnDate);
        }

        // Optional book details
        if (includeBookDetails) {
            record.setBookTitle(rs.getString("book_title"));
            record.setBookIsbn(rs.getString("book_isbn"));
        }

        return record;
    }
}