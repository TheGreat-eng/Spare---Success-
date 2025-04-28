package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.dao.BorrowDao;
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date; // Dùng java.sql.Date
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar; // Để tính ngày mặc định

public class IssueBookDialog extends JDialog {

    private JTextField bookIdentifierField; // Nhập ID hoặc ISBN
    private JButton findBookButton;
    private JLabel bookInfoLabel; // Hiển thị thông tin sách tìm được
    private JTextField dueDateField; // Nhập ngày hẹn trả
    private JButton issueButton;
    private JButton cancelButton;

    private BookDao bookDao;
    private BorrowDao borrowDao;
    private User borrower; // Người dùng được chọn để mượn sách
    private Book selectedBook = null; // Sách được chọn để cho mượn

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // --- Constructor ĐÚNG ---
    // Đảm bảo tham số đầu tiên là Frame (hoặc Window) và có tham số boolean modal
    public IssueBookDialog(Frame owner, boolean modal, User borrower, BookDao bookDao, BorrowDao borrowDao) {
        super(owner, modal); // Gọi super với owner và modal
        this.borrower = borrower;
        this.bookDao = bookDao;
        this.borrowDao = borrowDao;

        setTitle("Cho mượn sách cho: " + borrower.getUsername());
        setSize(450, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Tìm sách ---
        JPanel bookSearchPanel = new JPanel(new GridBagLayout());
        bookSearchPanel.setBorder(BorderFactory.createTitledBorder("Tìm sách cần mượn"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        bookSearchPanel.add(new JLabel("Nhập ID hoặc ISBN:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.8;
        bookIdentifierField = new JTextField(15);
        bookSearchPanel.add(bookIdentifierField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0.2;
        findBookButton = new JButton("Tìm sách");
        bookSearchPanel.add(findBookButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        bookInfoLabel = new JLabel("Chưa tìm thấy sách.");
        bookInfoLabel.setFont(bookInfoLabel.getFont().deriveFont(Font.ITALIC));
        bookSearchPanel.add(bookInfoLabel, gbc);

        // --- Panel Ngày hẹn trả và Nút bấm ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));
        JPanel dueDatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dueDatePanel.add(new JLabel("Ngày hẹn trả (yyyy-MM-dd)*:"));
        dueDateField = new JTextField(10);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 14);
        dueDateField.setText(dateFormat.format(cal.getTime()));
        dueDatePanel.add(dueDateField);
        bottomPanel.add(dueDatePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        issueButton = new JButton("Xác nhận cho mượn");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(issueButton);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Thêm vào Dialog ---
        add(bookSearchPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Trạng thái ban đầu ---
        issueButton.setEnabled(false);

        // --- Action Listeners ---
        findBookButton.addActionListener(e -> findBook());
        bookIdentifierField.addActionListener(e -> findBook());
        issueButton.addActionListener(e -> issueBook());
        cancelButton.addActionListener(e -> dispose());
    } // --- Kết thúc Constructor ---

    // --- Các phương thức findBook() và issueBook() giữ nguyên như trước ---
    private void findBook() {
        String identifier = bookIdentifierField.getText().trim();
        if (identifier.isEmpty()) {
            bookInfoLabel.setText("Vui lòng nhập ID hoặc ISBN.");
            bookInfoLabel.setForeground(Color.RED);
            selectedBook = null;
            issueButton.setEnabled(false);
            return;
        }

        Book foundBook = null;
        try {
            int bookId = Integer.parseInt(identifier);
            foundBook = bookDao.findBookById(bookId);
        } catch (NumberFormatException e) {
            // Tìm theo ISBN nếu không phải số
            foundBook = bookDao.findBookByIsbn(identifier); // Đảm bảo findBookByIsbn tồn tại trong BookDao
            if (foundBook == null) { // Xử lý nếu tìm theo ISBN cũng không thấy
                bookInfoLabel.setText("Không tìm thấy sách với ID/ISBN: " + identifier);
                bookInfoLabel.setForeground(Color.RED);
                selectedBook = null;
                issueButton.setEnabled(false);
                return; // Thoát khỏi hàm nếu không tìm thấy
            }
        }

        // Kiểm tra sách tìm được (dù là theo ID hay ISBN)
        if (foundBook.getAvailableQuantity() > 0) {
            selectedBook = foundBook;
            bookInfoLabel.setText(
                    "Sách: '" + selectedBook.getTitle() + "' (Còn lại: " + selectedBook.getAvailableQuantity() + ")");
            bookInfoLabel.setForeground(Color.BLUE);
            issueButton.setEnabled(true);
        } else {
            bookInfoLabel.setText("Sách: '" + foundBook.getTitle() + "' đã hết!");
            bookInfoLabel.setForeground(Color.RED);
            selectedBook = null;
            issueButton.setEnabled(false);
        }
    }

    private void issueBook() {
        if (selectedBook == null || borrower == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Chưa chọn sách hoặc người mượn hợp lệ.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String dueDateStr = dueDateField.getText().trim();
        Date dueDateSql = null;

        if (dueDateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ngày hẹn trả không được để trống.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            java.util.Date utilDate = dateFormat.parse(dueDateStr);
            // Có thể thêm kiểm tra ngày trong tương lai
            if (utilDate.before(new java.util.Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000))) {
                JOptionPane.showMessageDialog(this, "Ngày hẹn trả phải là ngày trong tương lai.", "Ngày không hợp lệ",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dueDateSql = new Date(utilDate.getTime());
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày hẹn trả không hợp lệ (yyyy-MM-dd).", "Lỗi định dạng",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = borrowDao.borrowBook(borrower.getUserId(), selectedBook.getBookId(), dueDateSql);

        if (success) {
            JOptionPane.showMessageDialog(this, "Cho mượn sách '" + selectedBook.getTitle() + "' thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Cho mượn sách thất bại. Vui lòng kiểm tra lại thông tin hoặc số lượng sách.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
} // --- Kết thúc lớp IssueBookDialog ---