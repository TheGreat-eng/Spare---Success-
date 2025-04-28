package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.model.Book;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BookDialog extends JDialog {

    private JTextField isbnField;
    private JTextField titleField;
    private JTextField authorField;
    private JTextField genreField;
    private JTextField yearField;
    private JTextArea descriptionArea; // Dùng JTextArea cho mô tả dài
    private JTextField quantityField;
    private JTextField availableQuantityField;
    private JButton saveButton;
    private JButton cancelButton;

    private BookDao bookDao;
    private Book bookToEdit; // Sách đang được sửa, null nếu là thêm mới
    private boolean saved = false; // Cờ để biết dialog đóng do Lưu hay Hủy

    public BookDialog(Frame owner, boolean modal, Book bookToEdit, BookDao bookDao) {
        super(owner, modal);
        this.bookToEdit = bookToEdit;
        this.bookDao = bookDao;

        setTitle(bookToEdit == null ? "Thêm sách mới" : "Sửa thông tin sách");
        setSize(450, 450); // Điều chỉnh kích thước
        setLocationRelativeTo(owner); // Hiển thị dialog ở giữa cửa sổ cha
        setLayout(new BorderLayout(10, 10));

        // --- Panel chứa các trường nhập liệu ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Các trường nhập liệu
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("ISBN*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        isbnField = new JTextField(20);
        formPanel.add(isbnField, gbc);
        gbc.weightx = 0; // Reset weightx

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tiêu đề*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        titleField = new JTextField();
        formPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Tác giả*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        authorField = new JTextField();
        formPanel.add(authorField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Thể loại:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        genreField = new JTextField();
        formPanel.add(genreField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Năm XB:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        yearField = new JTextField(6);
        formPanel.add(yearField, gbc); // Giới hạn chiều rộng ô năm

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Mô tả:"), gbc); // Căn lề trên trái cho nhãn Mô tả
        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Cho phép description mở rộng theo chiều dọc
        descriptionArea = new JTextArea(5, 20); // 5 hàng, 20 cột
        descriptionArea.setLineWrap(true); // Tự động xuống dòng
        descriptionArea.setWrapStyleWord(true); // Xuống dòng theo từ
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        formPanel.add(descriptionScrollPane, gbc);
        gbc.weighty = 0; // Reset weighty
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor

        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("SL Tổng*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        quantityField = new JTextField(5);
        formPanel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("SL Có sẵn*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        availableQuantityField = new JTextField(5);
        formPanel.add(availableQuantityField, gbc);

        // --- Panel chứa nút bấm ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // Thêm các panel vào dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Điền dữ liệu nếu là sửa ---
        if (bookToEdit != null) {
            populateFields();
        }

        // --- Xử lý sự kiện nút ---
        saveButton.addActionListener(e -> saveBook());
        cancelButton.addActionListener(e -> dispose()); // Đóng dialog khi nhấn Hủy

    }

    /**
     * Điền dữ liệu từ bookToEdit vào các trường nhập liệu.
     */
    private void populateFields() {
        isbnField.setText(bookToEdit.getIsbn());
        titleField.setText(bookToEdit.getTitle());
        authorField.setText(bookToEdit.getAuthor());
        genreField.setText(bookToEdit.getGenre() != null ? bookToEdit.getGenre() : "");
        yearField.setText(bookToEdit.getPublicationYear() > 0 ? String.valueOf(bookToEdit.getPublicationYear()) : "");
        descriptionArea.setText(bookToEdit.getDescription() != null ? bookToEdit.getDescription() : "");
        quantityField.setText(String.valueOf(bookToEdit.getQuantity()));
        availableQuantityField.setText(String.valueOf(bookToEdit.getAvailableQuantity()));
    }

    /**
     * Xử lý lưu thông tin sách (thêm mới hoặc cập nhật).
     */
    private void saveBook() {
        // --- Validation cơ bản ---
        String isbn = isbnField.getText().trim();
        String title = titleField.getText().trim();
        String author = authorField.getText().trim();
        String quantityStr = quantityField.getText().trim();
        String availableQuantityStr = availableQuantityField.getText().trim();

        if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || quantityStr.isEmpty()
                || availableQuantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Các trường có dấu * là bắt buộc.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity = 0;
        int availableQuantity = 0;
        int publicationYear = 0;

        try {
            quantity = Integer.parseInt(quantityStr);
            if (quantity < 0)
                throw new NumberFormatException("Số lượng không thể âm.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng tổng không hợp lệ.", "Lỗi nhập liệu",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            availableQuantity = Integer.parseInt(availableQuantityStr);
            if (availableQuantity < 0)
                throw new NumberFormatException("Số lượng có sẵn không thể âm.");
            if (availableQuantity > quantity)
                throw new NumberFormatException("Số lượng có sẵn không thể lớn hơn số lượng tổng.");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng có sẵn không hợp lệ hoặc lớn hơn số lượng tổng.",
                    "Lỗi nhập liệu", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String yearStr = yearField.getText().trim();
        if (!yearStr.isEmpty()) {
            try {
                publicationYear = Integer.parseInt(yearStr);
                // Có thể thêm kiểm tra năm hợp lệ (ví dụ: không quá tương lai)
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Năm xuất bản không hợp lệ.", "Lỗi nhập liệu",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // --- Tạo đối tượng Book ---
        Book book = new Book();
        book.setIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genreField.getText().trim());
        book.setPublicationYear(publicationYear);
        book.setDescription(descriptionArea.getText().trim());
        book.setQuantity(quantity);
        book.setAvailableQuantity(availableQuantity);

        boolean success = false;
        try {
            if (bookToEdit == null) {
                // Thêm mới
                success = bookDao.addBook(book);
                if (!success) {
                    // Kiểm tra lại thông báo lỗi từ DAO nếu có thể
                    JOptionPane.showMessageDialog(this, "Thêm sách thất bại. ISBN có thể đã tồn tại.", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Cập nhật
                book.setBookId(bookToEdit.getBookId()); // Đặt ID cho sách cần cập nhật
                success = bookDao.updateBook(book);
                if (!success) {
                    JOptionPane.showMessageDialog(this, "Cập nhật sách thất bại. ISBN có thể đã tồn tại cho sách khác.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) { // Bắt các lỗi không mong muốn khác
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lưu sách:\n" + ex.getMessage(), "Lỗi hệ thống",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        if (success) {
            saved = true; // Đánh dấu là đã lưu thành công
            dispose(); // Đóng dialog
        }
        // Nếu không thành công, dialog vẫn mở để người dùng sửa lại
    }

    // Optional: Cung cấp phương thức để cửa sổ cha biết dialog có được lưu hay
    // không
    public boolean isSaved() {
        return saved;
    }
}