package main.com.yourlibrary.model;

import java.sql.Timestamp;
import java.sql.Date; // Dùng java.sql.Date cho due_date và return_date

public class BorrowRecord {
    private int loanId;
    private int userId;
    private int bookId;
    private Timestamp borrowDate; // Ngày giờ mượn
    private Date dueDate; // Ngày hẹn trả
    private Date returnDate; // Ngày trả thực tế
    private String status; // BORROWED, RETURNED, OVERDUE

    // --- Trường thông tin bổ sung (lấy từ JOIN) ---
    private String borrowerUsername;
    private String bookTitle;
    private String bookIsbn; // Thêm ISBN có thể hữu ích

    // Constructors
    public BorrowRecord() {
    }

    // Getters and Setters cho tất cả các trường
    public int getLoanId() {
        return loanId;
    }

    public void setLoanId(int loanId) {
        this.loanId = loanId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public Timestamp getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(Timestamp borrowDate) {
        this.borrowDate = borrowDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBorrowerUsername() {
        return borrowerUsername;
    }

    public void setBorrowerUsername(String borrowerUsername) {
        this.borrowerUsername = borrowerUsername;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public String getBookIsbn() {
        return bookIsbn;
    }

    public void setBookIsbn(String bookIsbn) {
        this.bookIsbn = bookIsbn;
    }

    @Override
    public String toString() {
        return "BorrowRecord{" + "loanId=" + loanId + ", userId=" + userId + ", bookId=" + bookId + ", status='"
                + status + '\'' + '}';
    }
}