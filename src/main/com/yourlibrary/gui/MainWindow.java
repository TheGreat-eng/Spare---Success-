package main.com.yourlibrary.gui; // Hoặc package bạn đang dùng

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.dao.UserDao; // Import UserDao
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class MainWindow extends JFrame {

    private User currentUser;
    private BookDao bookDao;
    private UserDao userDao; // <<< Thêm biến thành viên UserDao

    // Thành phần Giao diện
    private JTextField searchField;
    private JButton searchButton;
    private JButton showAllButton;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton profileButton; // Nút quản lý thông tin cá nhân

    public MainWindow(User user) {
        this.currentUser = user;
        this.bookDao = new BookDao();
        this.userDao = new UserDao(); // <<< Khởi tạo UserDao ở đây

        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        // --- Panel Tìm kiếm (NORTH) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm kiếm (Tiêu đề, Tác giả, ISBN):"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);
        searchButton = new JButton("Tìm");
        searchPanel.add(searchButton);
        showAllButton = new JButton("Hiển thị tất cả");
        searchPanel.add(showAllButton);
        profileButton = new JButton("Hồ sơ");
        searchPanel.add(profileButton);

        add(searchPanel, BorderLayout.NORTH);

        // --- Bảng hiển thị sách (CENTER) ---
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

        // Thiết lập độ rộng cột (tùy chỉnh nếu cần)
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(bookTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel Nút chức năng (SOUTH) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addButton = new JButton("Thêm sách");
        editButton = new JButton("Sửa sách");
        deleteButton = new JButton("Xóa sách");
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);

        // Chỉ hiển thị nút Thêm/Sửa/Xóa cho Librarian/Admin
        if (!"LIBRARIAN".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            addButton.setVisible(false);
            editButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        add(buttonPanel, BorderLayout.SOUTH);

        // --- Nạp dữ liệu sách ban đầu ---
        loadAllBooks();

        // --- Xử lý sự kiện cho các nút ---
        searchButton.addActionListener(e -> searchBooks());
        showAllButton.addActionListener(e -> loadAllBooks());
        addButton.addActionListener(e -> openBookDialog(null));
        editButton.addActionListener(e -> openEditBookDialog());
        deleteButton.addActionListener(e -> deleteSelectedBook());
        profileButton.addActionListener(e -> openProfileDialog()); // <<< Kết nối nút Hồ sơ với phương thức

        // Xử lý nhấn Enter trong ô tìm kiếm
        searchField.addActionListener(e -> searchBooks());

    }

    // --- Các phương thức xử lý ---

    private void loadAllBooks() {
        searchField.setText("");
        List<Book> books = bookDao.getAllBooks();
        displayBooks(books);
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllBooks();
            return;
        }
        List<Book> books = bookDao.searchBooks(keyword);
        displayBooks(books);
        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sách nào phù hợp.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void displayBooks(List<Book> books) {
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
        if (searchField.getText().trim().isEmpty()) {
            loadAllBooks();
        } else {
            searchBooks();
        }
    }

    private void openEditBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách để sửa.", "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Book bookToEdit = bookDao.findBookById(bookId);
        if (bookToEdit != null) {
            openBookDialog(bookToEdit);
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lấy thông tin sách để sửa.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            loadAllBooks();
        }
    }

    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách để xóa.", "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2);
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa cuốn sách:\n'" + bookTitle + "' (ID: " + bookId + ")?",
                "Xác nhận xóa sách",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = bookDao.deleteBook(bookId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa sách thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                if (searchField.getText().trim().isEmpty()) {
                    loadAllBooks();
                } else {
                    searchBooks();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Xóa sách thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Phương thức mở Dialog Hồ sơ (ĐÃ SỬA) ---
    private void openProfileDialog() {
        // Tạo và hiển thị UserProfileDialog, truyền vào this (MainWindow), modal=true,
        // currentUser và đối tượng userDao đã khởi tạo.
        UserProfileDialog profileDialog = new UserProfileDialog(this, true, currentUser, userDao);
        profileDialog.setVisible(true);

        // Không cần làm gì thêm ở đây, việc cập nhật MainWindow (nếu cần)
        // sẽ được thực hiện thông qua phương thức updateUserInfo
    }

    // --- Phương thức để UserProfileDialog gọi lại (ĐÃ THÊM) ---
    public void updateUserInfo(User updatedUser) {
        this.currentUser = updatedUser; // Cập nhật đối tượng user hiện tại trong MainWindow
        // Cập nhật lại tiêu đề cửa sổ để phản ánh thay đổi (ví dụ: nếu tên thay đổi)
        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        // Bạn có thể cập nhật thêm các thành phần khác trên MainWindow nếu cần
        // Ví dụ: một JLabel chào mừng hiển thị tên đầy đủ.
        System.out.println("Thông tin người dùng trên MainWindow đã được cập nhật."); // Log để kiểm tra
    }

} // Kết thúc lớp MainWindow