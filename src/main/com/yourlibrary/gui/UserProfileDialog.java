package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays; // Để xóa mảng char password

public class UserProfileDialog extends JDialog {

    private JTextField usernameField; // Chỉ hiển thị, không cho sửa
    private JTextField emailField;
    private JTextField fullNameField;
    private JPasswordField oldPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton saveProfileButton;
    private JButton changePasswordButton;
    private JButton cancelButton;

    private User currentUser;
    private UserDao userDao;
    private MainWindow ownerWindow; // Để cập nhật lại thông tin trên cửa sổ chính

    public UserProfileDialog(MainWindow owner, boolean modal, User currentUser, UserDao userDao) {
        super(owner, modal);
        this.ownerWindow = owner; // Lưu lại tham chiếu đến cửa sổ cha
        this.currentUser = currentUser;
        this.userDao = userDao;

        setTitle("Quản lý Hồ sơ - " + currentUser.getUsername());
        setSize(450, 350); // Điều chỉnh kích thước
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Thông tin cá nhân ---
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBorder(BorderFactory.createTitledBorder("Thông tin cá nhân"));
        GridBagConstraints gbcProfile = new GridBagConstraints();
        gbcProfile.insets = new Insets(5, 5, 5, 5);
        gbcProfile.anchor = GridBagConstraints.WEST;

        gbcProfile.gridx = 0;
        gbcProfile.gridy = 0;
        profilePanel.add(new JLabel("Tên đăng nhập:"), gbcProfile);
        gbcProfile.gridx = 1;
        gbcProfile.gridy = 0;
        gbcProfile.fill = GridBagConstraints.HORIZONTAL;
        gbcProfile.weightx = 1.0;
        usernameField = new JTextField(currentUser.getUsername());
        usernameField.setEditable(false); // Không cho sửa username
        usernameField.setBackground(Color.LIGHT_GRAY); // Đánh dấu là không sửa được
        profilePanel.add(usernameField, gbcProfile);
        gbcProfile.weightx = 0;

        gbcProfile.gridx = 0;
        gbcProfile.gridy = 1;
        gbcProfile.fill = GridBagConstraints.NONE;
        profilePanel.add(new JLabel("Email*:"), gbcProfile);
        gbcProfile.gridx = 1;
        gbcProfile.gridy = 1;
        gbcProfile.fill = GridBagConstraints.HORIZONTAL;
        emailField = new JTextField(currentUser.getEmail());
        profilePanel.add(emailField, gbcProfile);

        gbcProfile.gridx = 0;
        gbcProfile.gridy = 2;
        gbcProfile.fill = GridBagConstraints.NONE;
        profilePanel.add(new JLabel("Họ và tên:"), gbcProfile);
        gbcProfile.gridx = 1;
        gbcProfile.gridy = 2;
        gbcProfile.fill = GridBagConstraints.HORIZONTAL;
        fullNameField = new JTextField(currentUser.getFullName());
        profilePanel.add(fullNameField, gbcProfile);

        gbcProfile.gridx = 1;
        gbcProfile.gridy = 3;
        gbcProfile.fill = GridBagConstraints.NONE;
        gbcProfile.anchor = GridBagConstraints.EAST;
        saveProfileButton = new JButton("Lưu thông tin");
        profilePanel.add(saveProfileButton, gbcProfile);

        // --- Panel Đổi mật khẩu ---
        JPanel passwordPanel = new JPanel(new GridBagLayout());
        passwordPanel.setBorder(BorderFactory.createTitledBorder("Đổi mật khẩu"));
        GridBagConstraints gbcPass = new GridBagConstraints();
        gbcPass.insets = new Insets(5, 5, 5, 5);
        gbcPass.anchor = GridBagConstraints.WEST;

        gbcPass.gridx = 0;
        gbcPass.gridy = 0;
        passwordPanel.add(new JLabel("Mật khẩu cũ*:"), gbcPass);
        gbcPass.gridx = 1;
        gbcPass.gridy = 0;
        gbcPass.fill = GridBagConstraints.HORIZONTAL;
        gbcPass.weightx = 1.0;
        oldPasswordField = new JPasswordField(15);
        passwordPanel.add(oldPasswordField, gbcPass);
        gbcPass.weightx = 0;

        gbcPass.gridx = 0;
        gbcPass.gridy = 1;
        gbcPass.fill = GridBagConstraints.NONE;
        passwordPanel.add(new JLabel("Mật khẩu mới*:"), gbcPass);
        gbcPass.gridx = 1;
        gbcPass.gridy = 1;
        gbcPass.fill = GridBagConstraints.HORIZONTAL;
        newPasswordField = new JPasswordField();
        passwordPanel.add(newPasswordField, gbcPass);

        gbcPass.gridx = 0;
        gbcPass.gridy = 2;
        gbcPass.fill = GridBagConstraints.NONE;
        passwordPanel.add(new JLabel("Xác nhận MK mới*:"), gbcPass);
        gbcPass.gridx = 1;
        gbcPass.gridy = 2;
        gbcPass.fill = GridBagConstraints.HORIZONTAL;
        confirmPasswordField = new JPasswordField();
        passwordPanel.add(confirmPasswordField, gbcPass);

        gbcPass.gridx = 1;
        gbcPass.gridy = 3;
        gbcPass.fill = GridBagConstraints.NONE;
        gbcPass.anchor = GridBagConstraints.EAST;
        changePasswordButton = new JButton("Đổi mật khẩu");
        passwordPanel.add(changePasswordButton, gbcPass);

        // --- Panel Nút bấm dưới cùng ---
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        cancelButton = new JButton("Đóng");
        bottomButtonPanel.add(cancelButton);

        // --- Thêm các panel vào Dialog ---
        JPanel centerPanel = new JPanel(); // Panel trung tâm để chứa 2 panel trên
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(profilePanel);
        centerPanel.add(passwordPanel);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);

        // --- Xử lý sự kiện ---
        saveProfileButton.addActionListener(e -> saveProfileChanges());
        changePasswordButton.addActionListener(e -> changePassword());
        cancelButton.addActionListener(e -> dispose());
    }

    private void saveProfileChanges() {
        String newEmail = emailField.getText().trim();
        String newFullName = fullNameField.getText().trim();

        // Validation cơ bản
        if (newEmail.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email không được để trống.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        // Thêm kiểm tra định dạng email nếu muốn

        // Cập nhật đối tượng User hiện tại (chỉ email và tên)
        currentUser.setEmail(newEmail);
        currentUser.setFullName(newFullName);

        boolean success = userDao.updateUserProfile(currentUser);

        if (success) {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            // Cập nhật lại thông tin trên cửa sổ chính nếu cần (ví dụ: tiêu đề)
            if (ownerWindow != null) {
                ownerWindow.updateUserInfo(currentUser); // Gọi phương thức cập nhật trên MainWindow
            }
        } else {
            JOptionPane.showMessageDialog(this, "Cập nhật thông tin thất bại. Email có thể đã tồn tại.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            // Có thể cần lấy lại thông tin user cũ từ DB nếu lỗi
            User refreshedUser = userDao.findUserById(currentUser.getUserId());
            if (refreshedUser != null) {
                currentUser = refreshedUser; // Cập nhật lại currentUser nếu lỗi
                emailField.setText(currentUser.getEmail());
                fullNameField.setText(currentUser.getFullName());
            }
        }
    }

    private void changePassword() {
        char[] oldPassChars = oldPasswordField.getPassword();
        char[] newPassChars = newPasswordField.getPassword();
        char[] confirmPassChars = confirmPasswordField.getPassword();

        String oldPassword = new String(oldPassChars);
        String newPassword = new String(newPassChars);
        String confirmPassword = new String(confirmPassChars);

        // Xóa mảng char ngay lập tức
        Arrays.fill(oldPassChars, ' ');
        Arrays.fill(newPassChars, ' ');
        Arrays.fill(confirmPassChars, ' ');

        // Validation
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ mật khẩu cũ, mới và xác nhận.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Mật khẩu mới và xác nhận không khớp.", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            // Xóa ô mật khẩu mới và xác nhận
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            newPasswordField.requestFocus(); // Đặt focus lại ô mật khẩu mới
            return;
        }

        // Gọi DAO để đổi mật khẩu
        boolean success = userDao.changePassword(currentUser.getUserId(), oldPassword, newPassword);

        if (success) {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            // Xóa các ô mật khẩu
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại. Mật khẩu cũ không đúng hoặc có lỗi xảy ra.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            // Xóa các ô mật khẩu
            oldPasswordField.setText("");
            newPasswordField.setText("");
            confirmPasswordField.setText("");
            oldPasswordField.requestFocus();
        }
    }
}