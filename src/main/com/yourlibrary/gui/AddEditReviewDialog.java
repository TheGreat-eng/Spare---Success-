package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.ReviewDao;
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.BookReview;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddEditReviewDialog extends JDialog {

    private JComboBox<Integer> ratingComboBox; // Chọn sao (1-5)
    private JTextArea reviewTextArea;
    private JButton saveButton;
    private JButton cancelButton;

    private User currentUser;
    private Book currentBook;
    private ReviewDao reviewDao;
    private BookReview existingReview; // Đánh giá cũ (nếu có) để điền vào form

    public AddEditReviewDialog(Dialog owner, boolean modal, User user, Book book, ReviewDao dao) {
        super(owner, modal);
        this.currentUser = user;
        this.currentBook = book;
        this.reviewDao = dao;

        setTitle("Đánh giá sách: " + currentBook.getTitle());
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Lấy đánh giá cũ nếu có
        this.existingReview = reviewDao.getReviewByUserAndBook(currentUser.getUserId(), currentBook.getBookId());

        // --- Panel Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Rating
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Điểm đánh giá (1-5)*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Integer[] ratings = { 1, 2, 3, 4, 5 };
        ratingComboBox = new JComboBox<>(ratings);
        if (existingReview != null) { // Chọn rating cũ nếu có
            ratingComboBox.setSelectedItem(existingReview.getRating());
        } else {
            ratingComboBox.setSelectedItem(5); // Mặc định 5 sao
        }
        formPanel.add(ratingComboBox, gbc);

        // Review Text
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Nhận xét:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        reviewTextArea = new JTextArea(5, 20);
        reviewTextArea.setLineWrap(true);
        reviewTextArea.setWrapStyleWord(true);
        if (existingReview != null) { // Điền text cũ nếu có
            reviewTextArea.setText(existingReview.getReviewText());
        }
        JScrollPane scrollPane = new JScrollPane(reviewTextArea);
        formPanel.add(scrollPane, gbc);

        // --- Panel Nút ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu đánh giá");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Add Panels ---
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Listeners ---
        saveButton.addActionListener(e -> saveReview());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveReview() {
        Integer selectedRating = (Integer) ratingComboBox.getSelectedItem();
        String reviewText = reviewTextArea.getText().trim();

        if (selectedRating == null) { // Nên luôn có giá trị nhưng kiểm tra cho chắc
            JOptionPane.showMessageDialog(this, "Vui lòng chọn điểm đánh giá.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Tạo đối tượng review
        BookReview review = new BookReview();
        review.setUserId(currentUser.getUserId());
        review.setBookId(currentBook.getBookId());
        review.setRating(selectedRating);
        review.setReviewText(reviewText); // Có thể null hoặc rỗng

        // Gọi DAO (hàm này xử lý cả thêm mới và cập nhật)
        boolean success = reviewDao.addOrUpdateReview(review);

        if (success) {
            JOptionPane.showMessageDialog(this, "Lưu đánh giá thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng dialog sau khi lưu
        } else {
            JOptionPane.showMessageDialog(this, "Lưu đánh giá thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}