package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.model.Book;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector; // Dùng Vector cho DefaultTableModel hoặc Object[]

public class MainWindow extends JFrame {

    private User currentUser;
    private BookDao bookDao;

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
        this.bookDao = new BookDao(); // Khởi tạo Book DAO

        setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
        setSize(900, 600); // Tăng kích thước để chứa bảng
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5)); // Layout chính là BorderLayout

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
        searchPanel.add(profileButton); // Thêm nút Profile vào khu vực tìm kiếm

        add(searchPanel, BorderLayout.NORTH);

        // --- Bảng hiển thị sách (CENTER) ---
        String[] columnNames = { "ID", "ISBN", "Tiêu đề", "Tác giả", "Thể loại", "Năm XB", "SL Tổng", "SL Có sẵn" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Ngăn không cho sửa trực tiếp trên bảng
                return false;
            }
        };
        bookTable = new JTable(tableModel);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Chỉ cho chọn 1 hàng
        bookTable.getTableHeader().setReorderingAllowed(false); // Không cho kéo thả cột

        // Thiết lập độ rộng cột (tùy chỉnh nếu cần)
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(100); // ISBN
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Title
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Author
        // ... chỉnh các cột khác nếu muốn

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
        addButton.addActionListener(e -> openBookDialog(null)); // null nghĩa là thêm mới
        editButton.addActionListener(e -> openEditBookDialog());
        deleteButton.addActionListener(e -> deleteSelectedBook());
        profileButton.addActionListener(e -> openProfileDialog()); // Xử lý nút profile

        // Xử lý nhấn Enter trong ô tìm kiếm
        searchField.addActionListener(e -> searchBooks());

    }

    // --- Các phương thức xử lý ---

    private void loadAllBooks() {
        searchField.setText(""); // Xóa ô tìm kiếm
        List<Book> books = bookDao.getAllBooks();
        displayBooks(books);
    }

    private void searchBooks() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllBooks(); // Nếu ô tìm kiếm trống thì hiển thị tất cả
            return;
        }
        List<Book> books = bookDao.searchBooks(keyword);
        displayBooks(books);
        if (books.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sách nào phù hợp.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Hiển thị danh sách sách lên JTable.
     * 
     * @param books Danh sách sách cần hiển thị.
     */
    private void displayBooks(List<Book> books) {
        // Xóa dữ liệu cũ trong bảng
        tableModel.setRowCount(0);

        if (books != null) {
            for (Book book : books) {
                Vector<Object> row = new Vector<>(); // Hoặc dùng Object[]
                row.add(book.getBookId());
                row.add(book.getIsbn());
                row.add(book.getTitle());
                row.add(book.getAuthor());
                row.add(book.getGenre());
                row.add(book.getPublicationYear() > 0 ? book.getPublicationYear() : ""); // Hiển thị rỗng nếu năm là 0
                row.add(book.getQuantity());
                row.add(book.getAvailableQuantity());
                tableModel.addRow(row);
            }
        }
    }

    /**
     * Mở dialog để thêm hoặc sửa sách.
     * 
     * @param bookToEdit Sách cần sửa (null nếu là thêm mới).
     */
    private void openBookDialog(Book bookToEdit) {
        // Tạo và hiển thị BookDialog
        // Tham số: owner=this, modal=true, book=bookToEdit
        BookDialog bookDialog = new BookDialog(this, true, bookToEdit, bookDao);
        bookDialog.setVisible(true); // Hiển thị dialog và chờ nó đóng lại

        // Sau khi dialog đóng, làm mới danh sách sách
        // Có thể tối ưu chỉ tìm kiếm lại nếu đang có từ khóa, hoặc load lại tất cả
        if (searchField.getText().trim().isEmpty()) {
            loadAllBooks();
        } else {
            searchBooks();
        }
    }

    /**
     * Lấy sách đang được chọn và mở dialog sửa.
     */
    private void openEditBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) { // Kiểm tra xem có hàng nào được chọn không
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách để sửa.", "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Lấy bookId từ cột đầu tiên (cột 0) của hàng được chọn
        // Cần đảm bảo cột 0 đúng là book_id
        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);

        // Lấy thông tin đầy đủ của sách từ DB (an toàn hơn lấy trực tiếp từ bảng)
        Book bookToEdit = bookDao.findBookById(bookId);

        if (bookToEdit != null) {
            openBookDialog(bookToEdit); // Mở dialog với thông tin sách đã lấy
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lấy thông tin sách để sửa. Sách có thể đã bị xóa.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            loadAllBooks(); // Tải lại danh sách phòng trường hợp sách vừa bị xóa
        }
    }

    /**
     * Xóa cuốn sách đang được chọn.
     */
    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một cuốn sách để xóa.", "Chưa chọn sách",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int bookId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) tableModel.getValueAt(selectedRow, 2); // Lấy tiêu đề để hiển thị xác nhận

        // Hiển thị hộp thoại xác nhận
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
                // Làm mới danh sách
                if (searchField.getText().trim().isEmpty()) {
                    loadAllBooks();
                } else {
                    searchBooks();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Xóa sách thất bại. Có thể sách đang được tham chiếu hoặc có lỗi xảy ra.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Mở dialog quản lý thông tin cá nhân.
     */
    private void openProfileDialog() {
        // Tạo và hiển thị UserProfileDialog (bạn sẽ tạo lớp này sau)
        // UserProfileDialog profileDialog = new UserProfileDialog(this, true,
        // currentUser);
        // profileDialog.setVisible(true);

        // Sau khi dialog đóng, có thể cần cập nhật lại tiêu đề cửa sổ nếu tên thay đổi
        // setTitle("Quản lý Thư viện - User: " + currentUser.getUsername() + " (" +
        // currentUser.getRole() + ")");

        JOptionPane.showMessageDialog(this, "Chức năng quản lý hồ sơ sẽ được thực hiện ở bước sau.");
    }

}