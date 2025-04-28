package main.com.yourlibrary.gui; // Hoặc package bạn đang dùng

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.dao.BorrowDao;
import main.com.yourlibrary.dao.SupportRequestDao;
import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.BorrowRecord; // Import BorrowRecord
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class MainWindow extends JFrame {

    // --- Biến thành viên ---
    private User currentUser;
    private BookDao bookDao;
    private UserDao userDao;
    private SupportRequestDao supportRequestDao;
    private BorrowDao borrowDao;

    // Components dùng chung hoặc cần truy cập từ nhiều nơi
    private JTabbedPane tabbedPane;
    // private JButton profileButton; // <<< Nút này sẽ bị loại bỏ, thay bằng menu
    private JButton supportButton; // Nút gửi hỗ trợ (giữ lại hoặc chuyển vào menu khác?)

    // Components của Tab Quản lý Sách
    private JTextField searchField;
    private JButton searchButton;
    private JButton showAllButton;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;

    // --- Constructor ---
    public MainWindow(User user) {
        this.currentUser = user;
        // Khởi tạo các DAO
        this.bookDao = new BookDao();
        this.userDao = new UserDao();
        this.supportRequestDao = new SupportRequestDao();
        this.borrowDao = new BorrowDao();

        // --- Cài đặt cửa sổ chính ---
        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setSize(950, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Tạo Menu Bar --- <<< THÊM MENU BAR Ở ĐÂY
        JMenuBar menuBar = new JMenuBar();

        // --- Menu "Tài khoản" ---
        JMenu accountMenu = new JMenu("Tài khoản");
        menuBar.add(accountMenu); // Thêm Menu vào Menu Bar

        JMenuItem profileMenuItem = new JMenuItem("Hồ sơ của tôi");
        profileMenuItem.setAccelerator(KeyStroke.getKeyStroke("control P")); // Phím tắt ví dụ
        profileMenuItem.addActionListener(e -> openProfileDialog()); // Gọi hàm mở dialog hồ sơ
        accountMenu.add(profileMenuItem); // Thêm mục vào Menu

        JMenuItem historyMenuItem = new JMenuItem("Lịch sử mượn/trả của tôi");
        historyMenuItem.setAccelerator(KeyStroke.getKeyStroke("control H")); // Phím tắt ví dụ
        historyMenuItem.addActionListener(e -> viewMyLoanHistory()); // Gọi hàm xem lịch sử cá nhân
        accountMenu.add(historyMenuItem); // Thêm mục vào Menu

        accountMenu.addSeparator(); // Thêm đường kẻ ngang

        JMenuItem logoutMenuItem = new JMenuItem("Đăng xuất");
        logoutMenuItem.addActionListener(e -> handleLogout()); // Gọi hàm xử lý đăng xuất
        accountMenu.add(logoutMenuItem); // Thêm mục vào Menu

        // --- Menu "Trợ giúp" (Ví dụ) ---
        // JMenu helpMenu = new JMenu("Trợ giúp");
        // JMenuItem aboutMenuItem = new JMenuItem("Thông tin ứng dụng");
        // helpMenu.add(aboutMenuItem);
        // menuBar.add(helpMenu);

        // --- Đặt Menu Bar cho Frame ---
        setJMenuBar(menuBar);

        // --- Tạo JTabbedPane để chứa các chức năng ---
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

            // Tab 4: Quản lý Yêu cầu Hỗ trợ (Placeholder)
            JPanel supportPanelPlaceholder = new JPanel();
            supportPanelPlaceholder.add(new JLabel("Chức năng quản lý yêu cầu hỗ trợ sẽ được thêm ở đây."));
            tabbedPane.addTab("Yêu cầu Hỗ trợ", null, supportPanelPlaceholder, "Xem và xử lý yêu cầu hỗ trợ");
        }

        // --- Thêm TabbedPane vào vùng CENTER của Frame ---
        add(tabbedPane, BorderLayout.CENTER);

        // --- Panel chứa các nút chức năng chung (trên cùng) ---
        // Có thể giữ lại nút Gửi Hỗ trợ ở đây hoặc chuyển vào menu Trợ giúp
        JPanel topButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // profileButton = new JButton("Hồ sơ"); // <<< LOẠI BỎ NÚT HỒ SƠ
        supportButton = new JButton("Gửi Hỗ trợ");
        // topButtonPanel.add(profileButton); // <<< LOẠI BỎ
        topButtonPanel.add(supportButton);
        add(topButtonPanel, BorderLayout.NORTH);

        // --- Gán sự kiện cho các nút chung còn lại ---
        // profileButton.addActionListener(e -> openProfileDialog()); // <<< LOẠI BỎ
        supportButton.addActionListener(e -> openSupportRequestDialog());

    } // --- Kết thúc Constructor ---

    // --- Phương thức tạo Panel Quản lý Sách (Giữ nguyên) ---
    private JPanel createBookManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        // ... (Code tạo searchPanel, bookTable, buttonPanel như cũ) ...

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
        addButton = new JButton("Thêm sách");
        editButton = new JButton("Sửa sách");
        deleteButton = new JButton("Xóa sách");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        if (!"LIBRARIAN".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }
        panel.add(buttonPanel, BorderLayout.SOUTH);

        // Gán sự kiện cho các nút sách
        searchButton.addActionListener(e -> searchBooks());
        showAllButton.addActionListener(e -> loadAllBooks());
        addButton.addActionListener(e -> openBookDialog(null));
        editButton.addActionListener(e -> openEditBookDialog());
        deleteButton.addActionListener(e -> deleteSelectedBook());
        searchField.addActionListener(e -> searchBooks());

        // Nạp dữ liệu sách ban đầu
        loadAllBooks();

        return panel;
    }

    // --- Các phương thức xử lý nghiệp vụ (Giữ nguyên hoặc chỉ thay đổi nhỏ) ---

    private void loadAllBooks() {
        if (searchField != null)
            searchField.setText("");
        List<Book> books = bookDao.getAllBooks();
        displayBooks(books);
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
        refreshBookList(); // Làm mới sau khi thêm/sửa sách
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

    // --- Phương thức mở Dialog Hồ sơ (Giữ nguyên) ---
    public void openProfileDialog() {
        // Truyền BorrowDao vào đây nếu UserProfileDialog cần nó để xem lịch sử
        UserProfileDialog profileDialog = new UserProfileDialog(this, true, currentUser, userDao /* , borrowDao */);
        profileDialog.setVisible(true);
    }

    // --- Phương thức cập nhật thông tin User từ Dialog (Giữ nguyên) ---
    public void updateUserInfo(User updatedUser) {
        this.currentUser = updatedUser;
        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        System.out.println("Thông tin người dùng trên MainWindow đã được cập nhật.");
    }

    // --- Phương thức mở Dialog Hỗ trợ (Giữ nguyên) ---
    private void openSupportRequestDialog() {
        SupportRequestDialog supportDialog = new SupportRequestDialog(this, true, currentUser, supportRequestDao);
        supportDialog.setVisible(true);
    }

    // --- Phương thức xem lịch sử mượn trả cá nhân (MỚI) ---
    private void viewMyLoanHistory() {
        List<BorrowRecord> history = borrowDao.getLoanHistoryByUser(currentUser.getUserId());
        String dialogTitle = "Lịch sử mượn/trả của bạn (" + currentUser.getUsername() + ")";
        // LoanHistoryDialog cần owner là Window
        LoanHistoryDialog historyDialog = new LoanHistoryDialog(this, dialogTitle, history);
        historyDialog.setVisible(true);
    }

    // --- Phương thức xử lý Đăng xuất (MỚI) ---
    private void handleLogout() {
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn đăng xuất?",
                "Xác nhận đăng xuất",
                JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            this.dispose(); // Đóng cửa sổ MainWindow hiện tại
            // Mở lại cửa sổ Login
            // Đảm bảo LoginWindow được import đúng cách
            SwingUtilities.invokeLater(() -> {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
            });
        }
    }

    // --- Phương thức làm mới danh sách sách (MỚI - nếu cần gọi từ panel con) ---
    public void refreshBookList() {
        System.out.println("Refreshing book list in MainWindow...");
        // Kiểm tra xem tab sách có đang hiển thị không để tối ưu? (Tùy chọn)
        // Component selectedComponent = tabbedPane.getSelectedComponent();
        // if (selectedComponent instanceof JPanel && selectedComponent ==
        // bookManagementPanel) { // Cần lưu tham chiếu bookManagementPanel
        loadAllBooks();
        // }
    }

} // --- Kết thúc lớp MainWindow ---