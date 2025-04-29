package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.ReportDao;
import main.com.yourlibrary.model.Book; // Nếu hiển thị chi tiết sách mượn nhiều

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class ReportingPanel extends JPanel {

    private ReportDao reportDao;

    // Labels để hiển thị thống kê
    private JLabel totalBooksLabel;
    private JLabel totalCopiesLabel;
    private JLabel totalMembersLabel;
    private JLabel totalBorrowsLabel;
    private JLabel currentlyBorrowedLabel;
    private JList<String> mostBorrowedList; // Hiển thị sách mượn nhiều
    private DefaultListModel<String> listModel;

    public ReportingPanel(ReportDao reportDao) {
        this.reportDao = reportDao;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10)); // Thêm padding

        // Panel chứa các số liệu thống kê chính
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS)); // Xếp dọc
        statsPanel.setBorder(BorderFactory.createTitledBorder("Thống kê chung"));

        totalBooksLabel = createStatLabel("Tổng số đầu sách:");
        totalCopiesLabel = createStatLabel("Tổng số bản sao sách:");
        totalMembersLabel = createStatLabel("Tổng số thành viên:");
        totalBorrowsLabel = createStatLabel("Tổng số lượt mượn:");
        currentlyBorrowedLabel = createStatLabel("Số sách đang được mượn:");

        statsPanel.add(totalBooksLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Khoảng cách
        statsPanel.add(totalCopiesLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(totalMembersLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(totalBorrowsLabel);
        statsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        statsPanel.add(currentlyBorrowedLabel);

        // Panel chứa danh sách sách mượn nhiều nhất
        JPanel mostBorrowedPanel = new JPanel(new BorderLayout());
        mostBorrowedPanel.setBorder(BorderFactory.createTitledBorder("Sách được mượn nhiều nhất (Top 5)"));
        listModel = new DefaultListModel<>();
        mostBorrowedList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(mostBorrowedList);
        mostBorrowedPanel.add(listScrollPane, BorderLayout.CENTER);

        // Nút làm mới (optional)
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton refreshButton = new JButton("Làm mới thống kê");
        refreshButton.addActionListener(e -> loadStatistics());
        refreshPanel.add(refreshButton);

        // Thêm các panel vào panel chính
        add(statsPanel, BorderLayout.NORTH);
        add(mostBorrowedPanel, BorderLayout.CENTER);
        add(refreshPanel, BorderLayout.SOUTH);

        // Load dữ liệu lần đầu
        loadStatistics();
    }

    // Hàm trợ giúp tạo JLabel
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text + " Đang tải...");
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        return label;
    }

    // Hàm load dữ liệu thống kê
    private void loadStatistics() {
        // Chạy trên luồng riêng để không block giao diện nếu query lâu
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            // Biến tạm để lưu kết quả từ luồng nền
            int books, copies, members, borrows, currentBorrows;
            List<Book> topBooks;

            @Override
            protected Void doInBackground() throws Exception {
                System.out.println("[ReportingPanel] Loading statistics...");
                books = reportDao.getTotalBookCount();
                copies = reportDao.getTotalBookCopies();
                members = reportDao.getTotalMemberCount();
                borrows = reportDao.getTotalBorrowCount();
                currentBorrows = reportDao.getCurrentlyBorrowedCount();
                topBooks = reportDao.getMostBorrowedBooks(5); // Lấy top 5
                System.out.println("[ReportingPanel] Statistics loaded.");
                return null;
            }

            @Override
            protected void done() {
                try {
                    // Lấy kết quả (không cần get() vì đã lưu vào biến thành viên tạm)
                    get(); // Gọi get() để bắt exception nếu có từ doInBackground

                    // Cập nhật giao diện trên Event Dispatch Thread
                    totalBooksLabel.setText("Tổng số đầu sách: " + books);
                    totalCopiesLabel.setText("Tổng số bản sao sách: " + copies);
                    totalMembersLabel.setText("Tổng số thành viên: " + members);
                    totalBorrowsLabel.setText("Tổng số lượt mượn: " + borrows);
                    currentlyBorrowedLabel.setText("Số sách đang được mượn: " + currentBorrows);

                    listModel.clear(); // Xóa danh sách cũ
                    if (topBooks != null && !topBooks.isEmpty()) {
                        for (Book book : topBooks) {
                            // Có thể hiển thị cả số lượt mượn nếu DAO trả về
                            listModel.addElement(book.getTitle() + " (ID: " + book.getBookId() + ")");
                        }
                    } else {
                        listModel.addElement("Chưa có dữ liệu mượn sách.");
                    }
                    System.out.println("[ReportingPanel] UI Updated.");

                } catch (Exception e) {
                    System.err.println("[ReportingPanel] Error updating UI with statistics: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ReportingPanel.this,
                            "Không thể tải dữ liệu thống kê.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    // Reset labels về trạng thái lỗi
                    totalBooksLabel.setText("Tổng số đầu sách: Lỗi");
                    // ... reset các label khác ...
                    listModel.clear();
                    listModel.addElement("Lỗi khi tải dữ liệu.");
                }
            }
        };
        worker.execute(); // Chạy SwingWorker
    }
}