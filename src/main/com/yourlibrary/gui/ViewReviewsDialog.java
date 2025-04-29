package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.ReviewDao;
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.BookReview;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

public class ViewReviewsDialog extends JDialog {

    private JLabel avgRatingLabel;
    private JTable reviewsTable;
    private DefaultTableModel tableModel;
    private JButton addEditButton;
    private JButton closeButton;

    private User currentUser;
    private Book currentBook;
    private ReviewDao reviewDao;

    public ViewReviewsDialog(Frame owner, boolean modal, User user, Book book, ReviewDao dao) {
        super(owner, modal);
        this.currentUser = user;
        this.currentBook = book;
        this.reviewDao = dao;

        setTitle("Đánh giá cho sách: " + currentBook.getTitle());
        setSize(600, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Thông tin trên cùng ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));
        avgRatingLabel = new JLabel("Điểm trung bình: Đang tải...", SwingConstants.LEFT);
        avgRatingLabel.setFont(avgRatingLabel.getFont().deriveFont(Font.BOLD));
        topPanel.add(avgRatingLabel, BorderLayout.WEST);

        addEditButton = new JButton("Thêm/Sửa đánh giá của bạn");
        JPanel buttonTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Panel để nút căn phải
        buttonTopPanel.add(addEditButton);
        topPanel.add(buttonTopPanel, BorderLayout.EAST);

        // --- Bảng hiển thị Reviews ---
        String[] columnNames = { "Người đánh giá", "Điểm", "Ngày", "Nhận xét" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reviewsTable = new JTable(tableModel);
        // Cho phép xem nhận xét dài
        reviewsTable.getColumnModel().getColumn(3).setPreferredWidth(300);
        // Có thể ẩn cột ngày nếu không cần thiết
        // reviewsTable.getColumnModel().getColumn(2).setMinWidth(80);
        // reviewsTable.getColumnModel().getColumn(2).setMaxWidth(100);

        JScrollPane scrollPane = new JScrollPane(reviewsTable);

        // --- Nút Đóng dưới cùng ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeButton = new JButton("Đóng");
        bottomPanel.add(closeButton);

        // --- Thêm vào Dialog ---
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- Load dữ liệu ---
        loadReviewsAndRating();

        // --- Listeners ---
        addEditButton.addActionListener(e -> openAddEditReviewDialog());
        closeButton.addActionListener(e -> dispose());
    }

    private void loadReviewsAndRating() {
        // Load trên luồng khác để không block UI (đặc biệt nếu có nhiều review)
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            double avgRating;
            List<BookReview> reviews;

            @Override
            protected Void doInBackground() throws Exception {
                System.out.println("[ViewReviews] Loading reviews and rating for bookId: " + currentBook.getBookId());
                avgRating = reviewDao.getAverageRatingForBook(currentBook.getBookId());
                reviews = reviewDao.getReviewsByBook(currentBook.getBookId());
                System.out.println("[ViewReviews] Found " + (reviews != null ? reviews.size() : 0)
                        + " reviews. Avg Rating: " + avgRating);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Check for exceptions from doInBackground

                    // Cập nhật điểm trung bình
                    if (avgRating > 0) {
                        avgRatingLabel.setText(String.format("Điểm trung bình: %.1f / 5.0", avgRating));
                    } else {
                        avgRatingLabel.setText("Điểm trung bình: Chưa có đánh giá");
                    }

                    // Cập nhật bảng đánh giá
                    tableModel.setRowCount(0); // Xóa dữ liệu cũ
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    if (reviews != null) {
                        for (BookReview review : reviews) {
                            Vector<Object> row = new Vector<>();
                            row.add(review.getReviewerUsername()); // Hiển thị username
                            row.add(review.getRating() + " ★"); // Thêm ký hiệu sao
                            row.add(review.getReviewedAt() != null ? timestampFormat.format(review.getReviewedAt())
                                    : "");
                            row.add(review.getReviewText());
                            tableModel.addRow(row);
                        }
                    }
                    System.out.println("[ViewReviews] UI Updated.");
                } catch (Exception e) {
                    System.err.println("[ViewReviews] Error loading reviews/rating: " + e.getMessage());
                    e.printStackTrace();
                    avgRatingLabel.setText("Điểm trung bình: Lỗi");
                    tableModel.setRowCount(0);
                    // Có thể thêm 1 dòng báo lỗi vào bảng
                }
            }
        };
        worker.execute();
    }

    private void openAddEditReviewDialog() {
        AddEditReviewDialog addEditDialog = new AddEditReviewDialog(this, true, currentUser, currentBook, reviewDao);
        addEditDialog.setVisible(true);

        // Sau khi dialog thêm/sửa đóng lại, load lại dữ liệu đánh giá
        loadReviewsAndRating();
    }
}