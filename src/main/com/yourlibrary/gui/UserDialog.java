package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays; // For password clearing

public class UserDialog extends JDialog {

    private JTextField usernameField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JComboBox<String> roleComboBox; // Để chọn vai trò
    private JPasswordField passwordField; // Chỉ dùng khi thêm mới
    private JPasswordField confirmPasswordField; // Chỉ dùng khi thêm mới
    private JLabel passwordLabel;
    private JLabel confirmPasswordLabel;
    private JButton saveButton;
    private JButton cancelButton;

    private UserDao userDao;
    private User userToEdit; // User đang được sửa, null nếu là thêm mới
    private boolean isEditMode; // Cờ để biết là đang sửa hay thêm

    public UserDialog(Frame owner, boolean modal, User userToEdit, UserDao userDao) {
        super(owner, modal);
        this.userToEdit = userToEdit;
        this.userDao = userDao;
        this.isEditMode = (userToEdit != null); // True nếu userToEdit không null

        setTitle(isEditMode ? "Sửa thông tin User" : "Thêm User mới");
        setSize(400, 300); // Điều chỉnh kích thước
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Form Panel ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);
        gbc.weightx = 0; // Reset

        // Email
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Email*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        emailField = new JTextField();
        formPanel.add(emailField, gbc);

        // Full Name
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        fullNameField = new JTextField();
        formPanel.add(fullNameField, gbc);

        // Role
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Vai trò*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        // Các vai trò có thể có trong hệ thống
        String[] roles = { "READER", "LIBRARIAN", "ADMIN" };
        roleComboBox = new JComboBox<>(roles);
        formPanel.add(roleComboBox, gbc);

        // Password (chỉ hiển thị khi thêm mới)
        passwordLabel = new JLabel("Mật khẩu*: ");
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Confirm Password (chỉ hiển thị khi thêm mới)
        confirmPasswordLabel = new JLabel("Xác nhận MK*: ");
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField, gbc);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Lưu");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        // --- Add panels to dialog ---
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Populate data if editing ---
        if (isEditMode) {
            populateFields();
            // Ẩn các trường mật khẩu khi sửa
            passwordLabel.setVisible(false);
            passwordField.setVisible(false);
            confirmPasswordLabel.setVisible(false);
            confirmPasswordField.setVisible(false);
            // Có thể thêm nút "Reset mật khẩu" riêng nếu muốn
        } else {
            // Đảm bảo các trường mật khẩu hiển thị khi thêm mới
            passwordLabel.setVisible(true);
            passwordField.setVisible(true);
            confirmPasswordLabel.setVisible(true);
            confirmPasswordField.setVisible(true);
        }

        // --- Action Listeners ---
        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> dispose());
    }

    private void populateFields() {
        usernameField.setText(userToEdit.getUsername());
        emailField.setText(userToEdit.getEmail());
        fullNameField.setText(userToEdit.getFullName() != null ? userToEdit.getFullName() : "");
        roleComboBox.setSelectedItem(userToEdit.getRole()); // Đặt giá trị cho ComboBox
    }

    private void saveUser() {
        // --- Get data from fields ---
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String selectedRole = (String) roleComboBox.getSelectedItem(); // Lấy vai trò được chọn

        // --- Basic Validation ---
        if (username.isEmpty() || email.isEmpty() || selectedRole == null) {
            JOptionPane.showMessageDialog(this, "Username, Email và Vai trò là bắt buộc.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Thêm kiểm tra định dạng email nếu muốn

        // --- Password Validation (only for add mode) ---
        String password = null;
        if (!isEditMode) { // Chỉ kiểm tra và lấy mật khẩu khi thêm mới
            char[] passChars = passwordField.getPassword();
            char[] confirmPassChars = confirmPasswordField.getPassword();
            password = new String(passChars);
            String confirmPassword = new String(confirmPassChars);

            Arrays.fill(passChars, ' '); // Clear arrays
            Arrays.fill(confirmPassChars, ' ');

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mật khẩu và Xác nhận mật khẩu là bắt buộc khi thêm mới.",
                        "Thiếu mật khẩu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu và Xác nhận mật khẩu không khớp.", "Lỗi mật khẩu",
                        JOptionPane.WARNING_MESSAGE);
                passwordField.setText("");
                confirmPasswordField.setText("");
                passwordField.requestFocus();
                return;
            }
        }

        // --- Create or Update User Object ---
        User user = isEditMode ? userToEdit : new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(selectedRole);
        // Không đặt mật khẩu cho user object nếu đang sửa (trừ khi có chức năng reset)

        // --- Call DAO ---
        boolean success = false;
        try {
            if (isEditMode) {
                success = userDao.updateUserByAdmin(user); // Gọi hàm cập nhật của Admin
            } else {
                success = userDao.addUser(user, password); // Gọi hàm thêm mới (truyền cả mật khẩu)
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Đã xảy ra lỗi khi lưu người dùng:\n" + ex.getMessage(), "Lỗi hệ thống",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }

        if (success) {
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật người dùng thành công!" : "Thêm người dùng thành công!",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog on success
        } else {
            // Thông báo lỗi cụ thể hơn đã được in ra từ DAO
            JOptionPane.showMessageDialog(this,
                    isEditMode ? "Cập nhật người dùng thất bại. Username hoặc Email có thể đã tồn tại."
                            : "Thêm người dùng thất bại. Username hoặc Email có thể đã tồn tại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Don't close dialog on failure
        }
    }
}