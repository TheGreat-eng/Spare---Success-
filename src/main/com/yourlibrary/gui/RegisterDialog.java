package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays; // Để xóa password

public class RegisterDialog extends JDialog {

    private JTextField usernameField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerButton;
    private JButton cancelButton;

    private UserDao userDao;

    public RegisterDialog(Frame owner, boolean modal, UserDao userDao) {
        super(owner, modal);
        this.userDao = userDao;

        setTitle("Đăng ký tài khoản mới");
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tên đăng nhập*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        formPanel.add(usernameField, gbc);
        gbc.weightx = 0;

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

        // Password
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Mật khẩu*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField();
        formPanel.add(passwordField, gbc);

        // Confirm Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(new JLabel("Xác nhận MK*:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        confirmPasswordField = new JPasswordField();
        formPanel.add(confirmPasswordField, gbc);

        // --- Panel Nút ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        registerButton = new JButton("Đăng ký");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);

        // --- Add Panels ---
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        registerButton.addActionListener(e -> handleRegister());
        cancelButton.addActionListener(e -> dispose());
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        char[] passChars = passwordField.getPassword();
        char[] confirmPassChars = confirmPasswordField.getPassword();
        String password = new String(passChars);
        String confirmPassword = new String(confirmPassChars);

        // Xóa pass từ bộ nhớ
        Arrays.fill(passChars, ' ');
        Arrays.fill(confirmPassChars, ' ');

        // --- Validation ---
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Các trường có dấu * là bắt buộc.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Có thể thêm validate định dạng email
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu và Xác nhận mật khẩu không khớp.", "Lỗi mật khẩu",
                    JOptionPane.WARNING_MESSAGE);
            passwordField.setText("");
            confirmPasswordField.setText("");
            passwordField.requestFocus();
            return;
        }
        // Có thể thêm validate độ dài/độ mạnh mật khẩu

        // --- Tạo User Object ---
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setFullName(fullName); // fullName có thể trống
        newUser.setRole("READER"); // Mặc định là Reader

        // --- Gọi DAO để thêm user (phiên bản không băm) ---
        boolean success = userDao.addUser(newUser, password); // Truyền plain password

        if (success) {
            JOptionPane.showMessageDialog(this, "Đăng ký thành công! Vui lòng đăng nhập.", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng dialog đăng ký
        } else {
            // Lỗi đã được in ra từ DAO (ví dụ: trùng username/email)
            JOptionPane.showMessageDialog(this, "Đăng ký thất bại. Tên đăng nhập hoặc Email có thể đã được sử dụng.",
                    "Lỗi đăng ký", JOptionPane.ERROR_MESSAGE);
        }
    }
}