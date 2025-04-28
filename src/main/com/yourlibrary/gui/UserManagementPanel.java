package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

public class UserManagementPanel extends JPanel {

    private UserDao userDao;
    private User currentUser; // User đang đăng nhập (để biết quyền)

    // Components
    private JTextField searchField;
    private JButton searchButton;
    private JButton showAllButton;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addUserButton;
    private JButton editUserButton;
    private JButton deleteUserButton;

    // Tham chiếu đến MainWindow để có thể mở Dialog
    private MainWindow mainWindow;

    public UserManagementPanel(User currentUser, UserDao userDao, MainWindow mainWindow) {
        this.currentUser = currentUser; // Lưu user đăng nhập
        this.userDao = userDao;
        this.mainWindow = mainWindow; // Lưu tham chiếu cửa sổ chính
        setLayout(new BorderLayout(5, 5));

        // --- Search Panel (Top) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Tìm User (Username, Email, Tên):"));
        searchField = new JTextField(20);
        searchPanel.add(searchField);
        searchButton = new JButton("Tìm");
        searchPanel.add(searchButton);
        showAllButton = new JButton("Hiển thị tất cả");
        searchPanel.add(showAllButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- User Table (Center) ---
        String[] columnNames = { "ID", "Username", "Email", "Họ và tên", "Vai trò" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Không cho sửa trực tiếp
            }
        };
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addUserButton = new JButton("Thêm User");
        editUserButton = new JButton("Sửa User");
        deleteUserButton = new JButton("Xóa User");
        buttonPanel.add(addUserButton);
        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Load initial data ---
        loadAllUsers();

        // --- Action Listeners ---
        searchButton.addActionListener(e -> searchUsers());
        showAllButton.addActionListener(e -> loadAllUsers());
        searchField.addActionListener(e -> searchUsers()); // Tìm khi nhấn Enter
        addUserButton.addActionListener(e -> openUserDialog(null)); // null for adding new user
        editUserButton.addActionListener(e -> openEditUserDialog());
        deleteUserButton.addActionListener(e -> deleteSelectedUser());

        // Phân quyền: Chỉ Admin mới được quản lý user? Hay cả Librarian?
        // Hiện tại để cả Librarian và Admin (bạn có thể đổi thành chỉ ADMIN nếu muốn)
        if (!"LIBRARIAN".equalsIgnoreCase(currentUser.getRole()) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            searchPanel.setVisible(false); // Ẩn cả khu vực tìm kiếm nếu không có quyền
            buttonPanel.setVisible(false); // Ẩn các nút chức năng
            // Có thể hiển thị thông báo không có quyền thay vì ẩn hoàn toàn
        }
    }

    // --- Data Loading and Display ---
    public void loadAllUsers() { // Để public để MainWindow có thể gọi refresh nếu cần
        searchField.setText("");
        List<User> users = userDao.getAllUsers();
        displayUsers(users);
    }

    private void searchUsers() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadAllUsers();
            return;
        }
        List<User> users = userDao.searchUsers(keyword);
        displayUsers(users);
        if (users.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy người dùng nào phù hợp.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void displayUsers(List<User> users) {
        tableModel.setRowCount(0); // Clear existing data
        if (users != null) {
            for (User user : users) {
                Vector<Object> row = new Vector<>();
                row.add(user.getUserId());
                row.add(user.getUsername());
                row.add(user.getEmail());
                row.add(user.getFullName());
                row.add(user.getRole());
                tableModel.addRow(row);
            }
        }
    }

    // --- Dialog Handling ---
    private void openUserDialog(User userToEdit) {
        // Tạo và hiển thị UserDialog
        UserDialog userDialog = new UserDialog(mainWindow, true, userToEdit, userDao);
        userDialog.setVisible(true);

        // Refresh a`fter dialog closes
        loadAllUsers(); // Load lại tất cả để thấy thay đổi
    }

    private void openEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để sửa.", "Chưa chọn User",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);

        // Lấy thông tin đầy đủ của user từ DB (bao gồm cả password hash nếu cần cho
        // việc khác)
        // Nhưng UserDialog chỉ cần các thông tin cơ bản
        User userToEdit = userDao.findUserById(userId); // Dùng lại findUserById đã có

        if (userToEdit != null) {
            if (currentUser.getUserId() == userToEdit.getUserId()) {
                // Nếu user đang sửa chính mình, chuyển sang dialog hồ sơ cá nhân
                JOptionPane.showMessageDialog(this, "Để sửa thông tin cá nhân, vui lòng sử dụng mục 'Hồ sơ'.",
                        "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                mainWindow.openProfileDialog(); // Gọi phương thức mở profile dialog của MainWindow
            } else if ("ADMIN".equalsIgnoreCase(userToEdit.getRole())
                    && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                // Chỉ Admin mới được sửa Admin khác (ví dụ)
                JOptionPane.showMessageDialog(this, "Bạn không có quyền sửa thông tin của quản trị viên khác.",
                        "Không có quyền", JOptionPane.WARNING_MESSAGE);
            } else {
                openUserDialog(userToEdit); // Mở dialog sửa thông thường
            }
        } else {
            JOptionPane.showMessageDialog(this, "Không thể lấy thông tin người dùng để sửa.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            loadAllUsers();
        }
    }

    // --- Action Handling ---
    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một người dùng để xóa.", "Chưa chọn User",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String username = (String) tableModel.getValueAt(selectedRow, 1);
        String userRole = (String) tableModel.getValueAt(selectedRow, 4);

        // Ngăn chặn xóa chính mình
        if (userId == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this, "Bạn không thể xóa chính tài khoản của mình.", "Không thể xóa",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ngăn chặn Librarian xóa Admin (ví dụ)
        if ("ADMIN".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            JOptionPane.showMessageDialog(this, "Bạn không có quyền xóa tài khoản quản trị viên.", "Không có quyền",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa người dùng:\n'" + username + "' (ID: " + userId + ")?",
                "Xác nhận xóa User",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            // TODO: Thêm kiểm tra ràng buộc (sách đang mượn) trước khi xóa
            boolean success = userDao.deleteUser(userId);
            if (success) {
                JOptionPane.showMessageDialog(this, "Đã xóa người dùng thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadAllUsers(); // Refresh list
            } else {
                JOptionPane.showMessageDialog(this,
                        "Xóa người dùng thất bại. Người dùng có thể đang mượn sách hoặc có lỗi xảy ra.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}