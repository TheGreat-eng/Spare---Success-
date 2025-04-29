package main.com.yourlibrary.gui; // Hoặc package bạn đang dùng

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.dao.BorrowDao;
import main.com.yourlibrary.dao.FavoriteDao; // <<< Import FavoriteDao
import main.com.yourlibrary.dao.ReportDao;
import main.com.yourlibrary.dao.ReviewDao;
import main.com.yourlibrary.dao.SupportRequestDao;
import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.BorrowRecord;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent; // <<< Import ListSelectionEvent
import javax.swing.event.ListSelectionListener; // <<< Import ListSelectionListener
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class MainWindow extends JFrame {

    // --- Biến thành viên ---
    private User currentUser;
    // DAOs
    private BookDao bookDao;
    private UserDao userDao;
    private SupportRequestDao supportRequestDao;
    private BorrowDao borrowDao;
    private FavoriteDao favoriteDao; // <<< Thêm FavoriteDao
    private ReviewDao reviewDao;
    private ReportDao reportDao;

    // Components dùng chung hoặc cần truy cập từ nhiều nơi
    private JTabbedPane tabbedPane;
    private JButton supportButton; // Nút gửi hỗ trợ

    // Components của Tab Quản lý Sách
    private JTextField searchField;
    private JButton searchButton;
    private JButton showAllButton;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton favoriteButton; // <<< Thêm nút yêu thích

    // --- Constructor ---
    public MainWindow(User user) {
        this.currentUser = user;
        // Khởi tạo các DAO
        this.bookDao = new BookDao();
        this.userDao = new UserDao();
        this.supportRequestDao = new SupportRequestDao();
        this.borrowDao = new BorrowDao();
        this.favoriteDao = new FavoriteDao(); // <<< Khởi tạo FavoriteDao
        this.reviewDao = new ReviewDao();
        this.reportDao = new ReportDao();

        // --- Cài đặt cửa sổ chính ---
        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Tạo Menu Bar ---
        JMenuBar menuBar = new JMenuBar();
        JMenu accountMenu = new JMenu("Tài khoản");
        menuBar.add(accountMenu);
        JMenuItem profileMenuItem = new JMenuItem("Hồ sơ của tôi");
        profileMenuItem.addActionListener(e -> openProfileDialog());
        accountMenu.add(profileMenuItem);
        JMenuItem historyMenuItem = new JMenuItem("Lịch sử mượn/trả của tôi");
        historyMenuItem.addActionListener(e -> viewMyLoanHistory());
        accountMenu.add(historyMenuItem);
        JMenuItem favMenuItem = new JMenuItem("Sách yêu thích của tôi"); // <<< Thêm mục Sách yêu thích
        favMenuItem.addActionListener(e -> viewMyFavorites()); // <<< Gọi hàm xem yêu thích
        accountMenu.add(favMenuItem); // <<< Thêm vào menu
        accountMenu.addSeparator();
        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.addActionListener(e -> handleLogout());
        accountMenu.add(logoutMenuItem);
        setJMenuBar(menuBar);

        // --- Tạo JTabbedPane ---
        tabbedPane = new JTabbedPane();

        // --- Tab 1: Quản lý Sách ---
        JPanel bookManagementPanel = createBookManagementPanel();
        tabbedPane.addTab("Quản lý Sách", null, bookManagementPanel, "Quản lý và tìm kiếm sách");

        // --- Thêm các Tab chỉ dành cho Librarian/Admin ---
        if ("LIBRARIAN".equalsIgnoreCase(currentUser.getRole()) || "ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            // Tab 2: Quản lý Thành viên
            UserManagementPanel userManagementPanel = new UserManagementPanel(currentUser, userDao, this);
            tabbedPane.addTab("Quản lý Thành viên", null, userManagementPanel, "Quản lý tài khoản người dùng");
            // Tab 3: Quản lý Mượn/Trả
            CirculationPanel circulationPanel = new CirculationPanel(currentUser, userDao, borrowDao, this);
            tabbedPane.addTab("Quản lý Mượn/Trả", null, circulationPanel, "Quản lý việc mượn và trả sách");
            // Tab 4: Yêu cầu Hỗ trợ (Placeholder)
            // JPanel supportPanelPlaceholder = new JPanel(new BorderLayout()); // Use
            // BorderLayout to center
            // JLabel supportLabel = new JLabel("Chức năng quản lý yêu cầu hỗ trợ sẽ được
            // thêm ở đây.",
            // SwingConstants.CENTER);
            // supportPanelPlaceholder.add(supportLabel, BorderLayout.CENTER);
            // tabbedPane.addTab("Yêu cầu Hỗ trợ", null, supportPanelPlaceholder, "Xem và xử
            // lý yêu cầu hỗ trợ");
            SupportManagementPanel supportPanel = new SupportManagementPanel(currentUser, supportRequestDao, this);
            tabbedPane.addTab("Yêu cầu Hỗ trợ", null, supportPanel, "Xem và xử lý yêu cầu hỗ trợ");
            // Tab 5: Báo cáo Thống kê
            ReportDao reportDao = new ReportDao(); // Khởi tạo ReportDao
            ReportingPanel reportingPanel = new ReportingPanel(reportDao);
            tabbedPane.addTab("Báo cáo Thống kê", null, reportingPanel, "Xem các số liệu thống kê thư viện");

        }

        // --- Thêm TabbedPane vào vùng CENTER của Frame ---
        add(tabbedPane, BorderLayout.CENTER);

        // --- Panel chứa các nút chức năng chung (trên cùng) ---
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        supportButton = new JButton("Gửi Hỗ trợ");
        topButtonPanel.add(supportButton);
        add(topButtonPanel, BorderLayout.NORTH);

        // --- Gán sự kiện cho các nút chung còn lại ---
        supportButton.addActionListener(e -> openSupportRequestDialog());

    } // --- Kết thúc Constructor ---

    // --- Phương thức tạo Panel Quản lý Sách (ĐÃ CẬP NHẬT) ---
    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Panel Tìm kiếm Sách
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm Sách (Tiêu đề, Tác giả, ISBN):"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);
        searchButton = new JButton("Tìm");
        searchPanel.add(searchButton);
        showAllButton = new JButton("Hiển thị tất cả");
        searchPanel.add(showAllButton);
        panel.add(searchPanel, BorderLayout.NORTH);

        // Bảng hiển thị sách
        String[] columnNames = { "ID", "ISBN", "Tiêu đề", "Tác giả", "Thể loại", "Năm XB", "SL Tổng", "SL Có sẵn" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.getTableHeader().setReorderingAllowed(false);
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Panel Nút chức năng Sách
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        favoriteButton = new JButton("Yêu thích"); // <<< Khởi tạo nút yêu thích
        favoriteButton.setEnabled(false); // <<< Ban đầu vô hiệu hóa
        buttonPanel.add(favoriteButton, 0); // <<< Thêm vào đầu panel nút

        // Trong createBookManagementPanel(), trong buttonPanel
        JButton reviewButton = new JButton("Xem/Đánh giá");
        reviewButton.setEnabled(false); // Ban đầu vô hiệu hóa
        buttonPanel.add(reviewButton, 1); // Thêm sau nút Yêu thích

        addButton = new JButton("Thêm sách");
        editButton = new JButton("Sửa sách");
        deleteButton = new JButton("Xóa sách");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        // Phân quyền cho nút Thêm/Sửa/Xóa
        if (!"LIBRARIAN".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Gán sự kiện cho các nút sách ---
        searchButton.addActionListener(e -> searchBooks());
        showAllButton.addActionListener(e -> loadAllBooks());
        addButton.addActionListener(e -> openBookDialog(null));
        editButton.addActionListener(e -> openEditBookDialog());
        deleteButton.addActionListener(e -> deleteSelectedBook());
        searchField.addActionListener(e -> searchBooks());
        favoriteButton.addActionListener(e -> toggleFavorite(favoriteButton)); // <<< Gán sự kiện cho nút yêu thích
        reviewButton.addActionListener(e -> openViewReviewsDialog());

        // --- Listener cho việc chọn sách trong bảng (ĐỂ CẬP NHẬT NÚT YÊU THÍCH) ---
        // <<< THÊM MỚI
        bookTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                // Chỉ xử lý khi việc chọn kết thúc và có hàng được chọn
                if (!e.getValueIsAdjusting()) {
                    if (bookTable.getSelectedRow() != -1) {
                        System.out.println("[MainWindow] Book selection changed, updating favorite button state..."); // DEBUG
                        updateFavoriteButtonState(favoriteButton);
                    } else {
                        System.out.println("[MainWindow] Book selection cleared, disabling favorite button."); // DEBUG
                        favoriteButton.setEnabled(false);
                        favoriteButton.setText("Yêu thích"); // Reset text
                    }
                }

                if (!e.getValueIsAdjusting()) {
                    boolean rowSelected = bookTable.getSelectedRow() != -1;
                    if (rowSelected) {
                        updateFavoriteButtonState(favoriteButton);
                        reviewButton.setEnabled(true); // <<< Bật nút review khi chọn sách
                    } else {
                        favoriteButton.setEnabled(false);
                        favoriteButton.setText("Yêu thích");
                        reviewButton.setEnabled(false); // <<< Tắt nút review khi không chọn
                    }
                }
            }

        });

        // Nạp dữ liệu sách ban đầu
        loadAllBooks();

        return panel; // Trả về panel đã hoàn thiện
    }

    // --- Các phương thức xử lý nghiệp vụ (Giữ nguyên hoặc chỉ thay đổi nhỏ) ---
    // loadAllBooks, searchBooks, displayBooks, openBookDialog, openEditBookDialog,
    // deleteSelectedBook giữ nguyên

    private void loadAllBooks() {
        if (searchField != null)
            searchField.setText("");
        List<Book> books = bookDao.getAllBooks();
        displayBooks(books);
        // Khi load lại danh sách, vô hiệu hóa nút yêu thích vì chưa có sách nào được
        // chọn
        if (favoriteButton != null) {
            favoriteButton.setEnabled(false);
            favoriteButton.setText("Yêu thích");
        }
    }

    private void searchBooks() {
        if (searchField == null)
            return;
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllBooks();
            return;
        }
        List<Book> books = bookDao.searchBooks(keyword);
        displayBooks(books);
        if (books.isEmpty())
            JOptionPane.showMessageDialog(this, "Không tìm thấy sách nào phù hợp.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        // Khi tìm kiếm, cũng vô hiệu hóa nút yêu thích
        if (favoriteButton != null) {
            favoriteButton.setEnabled(false);
            favoriteButton.setText("Yêu thích");
        }
    }

    private void displayBooks(List<Book> books) {
        if (tableModel == null)
            return;
        tableModel.setRowCount(0);
        if (books != null) {
            for (Book book : books) {
                Vector<Object> row = new Vector<>();
                row.add(book.getBookId());
                row.add(book.getIsbn());
                row.add(book.getTitle());
                row.add(book.getAuthor());
                row.add(book.getGenre());
                row.add(book.getPublicationYear() > 0 ? book.getPublicationYear() : "");
                row.add(book.getQuantity());
                row.add(book.getAvailableQuantity());
                tableModel.addRow(row);
            }
        }
    }

    private void openBookDialog(Book bookToEdit) {
        BookDialog bookDialog = new BookDialog(this, true, bookToEdit, bookDao);
        bookDialog.setVisible(true);
        refreshBookList();
    }

    private void openEditBookDialog() {
        if (bookTable == null || tableModel == null)
            return;
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để sửa.", "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Book bookToEdit = bookDao.findBookById(bookId);
        if (bookToEdit != null)
            openBookDialog(bookToEdit);
        else {
            JOptionPane.showMessageDialog(this, "Không thể lấy thông tin sách.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            loadAllBooks();
        }
    }

    private void deleteSelectedBook() {
        if (bookTable == null || tableModel == null)
            return;
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sách để xóa.", "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);
        int confirmation = JOptionPane.showConfirmDialog(this, "Xóa sách:\n'" + bookTitle + "' (ID: " + bookId + ")?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = bookDao.deleteBook(bookId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa sách!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                refreshBookList();
            } else
                JOptionPane.showMessageDialog(this, "Xóa sách thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Thêm phương thức này vào lớp MainWindow
    private void openViewReviewsDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            // Nút không nên được bật nếu không có hàng nào được chọn, nhưng kiểm tra lại
            // cho chắc
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        // Cần lấy đối tượng Book đầy đủ (hoặc ít nhất là title) để truyền vào dialog
        Book selectedBook = bookDao.findBookById(bookId); // Dùng lại hàm tìm sách

        if (selectedBook != null) {
            ViewReviewsDialog viewDialog = new ViewReviewsDialog(this, true, currentUser, selectedBook, reviewDao);
            viewDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lấy thông tin sách để xem đánh giá.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Phương thức mở Dialog Hồ sơ (Giữ nguyên) ---
    public void openProfileDialog() {
        // Nếu UserProfileDialog cần BorrowDao để xem lịch sử từ đó, hãy truyền vào đây
        UserProfileDialog profileDialog = new UserProfileDialog(this, true, currentUser, userDao /* , borrowDao */);
        profileDialog.setVisible(true);
    }

    // --- Phương thức cập nhật thông tin User từ Dialog (Giữ nguyên) ---
    public void updateUserInfo(User updatedUser) {
        this.currentUser = updatedUser;
        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        System.out.println("[MainWindow] User info updated.");
    }

    // --- Phương thức mở Dialog Hỗ trợ (Giữ nguyên) ---
    private void openSupportRequestDialog() {
        SupportRequestDialog supportDialog = new SupportRequestDialog(this, true, currentUser, supportRequestDao);
        supportDialog.setVisible(true);
    }

    // --- Phương thức xem lịch sử mượn trả cá nhân (Giữ nguyên) ---
    private void viewMyLoanHistory() {
        List<BorrowRecord> history = borrowDao.getLoanHistoryByUser(currentUser.getUserId());
        String dialogTitle = "Lịch sử mượn/trả của bạn (" + currentUser.getUsername() + ")";
        LoanHistoryDialog historyDialog = new LoanHistoryDialog(this, dialogTitle, history);
        historyDialog.setVisible(true);
    }

    // --- Phương thức xử lý Đăng xuất (Giữ nguyên) ---
    private void handleLogout() {
        int confirmation = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn đăng xuất?", "Xác nhận",
                JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
            });
        }
    }

    // --- Phương thức làm mới danh sách sách (Giữ nguyên) ---
    public void refreshBookList() {
        System.out.println("[MainWindow] Refreshing book list...");
        loadAllBooks();
    }

    // --- Phương thức cập nhật trạng thái nút Yêu thích (MỚI) ---
    private void updateFavoriteButtonState(JButton favButton) {
        int selectedRow = bookTable.getSelectedRow();
        // Kiểm tra lại selectedRow để tránh lỗi nếu người dùng bỏ chọn nhanh
        if (selectedRow == -1 || selectedRow >= tableModel.getRowCount()) {
            System.out.println("[MainWindow] updateFavoriteButtonState called with invalid row: " + selectedRow); // DEBUG
            favButton.setEnabled(false);
            favButton.setText("Yêu thích");
            return;
        }
        try {
            int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
            System.out.println("[MainWindow] Checking favorite status for bookId: " + bookId + ", userId: "
                    + currentUser.getUserId()); // DEBUG
            boolean isFav = favoriteDao.isFavorite(currentUser.getUserId(), bookId);
            System.out.println("[MainWindow] Is favorite: " + isFav); // DEBUG
            if (isFav) {
                favButton.setText("Bỏ thích");
            } else {
                favButton.setText("Thêm thích");
            }
            favButton.setEnabled(true);
        } catch (ArrayIndexOutOfBoundsException e) {
            // Xử lý trường hợp bảng bị thay đổi dữ liệu đột ngột
            System.err.println("[MainWindow] Error getting bookId for favorite check: " + e.getMessage());
            favButton.setEnabled(false);
            favButton.setText("Yêu thích");
        }
    }

    // --- Phương thức xử lý nhấn nút Yêu thích (MỚI) ---
    private void toggleFavorite(JButton favButton) {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            System.out.println("[MainWindow] Toggle favorite called with no selection."); // DEBUG
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);

        boolean isCurrentlyFavorite = favoriteDao.isFavorite(currentUser.getUserId(), bookId);
        boolean success = false;
        System.out.println(
                "[MainWindow] Toggling favorite for bookId: " + bookId + ", current status: " + isCurrentlyFavorite); // DEBUG

        if (isCurrentlyFavorite) {
            success = favoriteDao.removeFavorite(currentUser.getUserId(), bookId);
            if (success)
                System.out.println("[MainWindow] Removed favorite successfully.");
            else
                System.err.println("[MainWindow] Failed to remove favorite.");
        } else {
            success = favoriteDao.addFavorite(currentUser.getUserId(), bookId);
            if (success)
                System.out.println("[MainWindow] Added favorite successfully.");
            else
                System.err.println("[MainWindow] Failed to add favorite.");
        }

        if (success) {
            updateFavoriteButtonState(favButton); // Cập nhật lại nút
        } else {
            JOptionPane.showMessageDialog(this, "Thao tác Yêu thích/Bỏ thích thất bại!", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Phương thức xem Sách yêu thích (MỚI) ---
    private void viewMyFavorites() {
        System.out.println("[MainWindow] Viewing my favorites..."); // DEBUG
        List<Book> favBooks = favoriteDao.getFavoriteBooksByUser(currentUser.getUserId());
        System.out.println("[MainWindow] Found " + (favBooks != null ? favBooks.size() : 0) + " favorite books."); // DEBUG
        String dialogTitle = "Sách yêu thích của bạn (" + currentUser.getUsername() + ")";
        FavoriteBooksDialog favDialog = new FavoriteBooksDialog(this, dialogTitle, favBooks);
        favDialog.setVisible(true);
    }

} // --- Kết thúc lớp MainWindow ---