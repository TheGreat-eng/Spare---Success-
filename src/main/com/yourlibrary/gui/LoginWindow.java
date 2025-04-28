// File: gui/LoginWindow.java
package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private UserDao userDao; // Đối tượng để tương tác với DB User

    public LoginWindow() {
        userDao = new UserDao(); // Khởi tạo DAO

        setTitle("Đăng nhập Thư viện");
        setSize(350, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Căn giữa màn hình

        // Tạo các thành phần Giao diện
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Khoảng cách giữa các thành phần

        // Nhãn và ô nhập Username
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel("Tên đăng nhập:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        usernameField = new JTextField(15); // Độ rộng 15 ký tự
        panel.add(usernameField, gbc);

        // Nhãn và ô nhập Password
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(15);
        panel.add(passwordField, gbc);

        // Nút Đăng nhập
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2; // Nút chiếm 2 cột
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        loginButton = new JButton("Đăng nhập");
        panel.add(loginButton, gbc);

        // Thêm panel vào Frame
        add(panel);

        // --- Xử lý sự kiện nhấn nút Đăng nhập ---
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });
    }

    private void handleLogin() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword(); // Lấy mật khẩu dạng mảng char
        String password = new String(passwordChars); // Chuyển sang String (cẩn thận trong môi trường thực tế)

        // Xóa mật khẩu khỏi bộ nhớ càng sớm càng tốt
        java.util.Arrays.fill(passwordChars, ' ');

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập tên đăng nhập và mật khẩu.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Gọi DAO để xác thực
        User loggedInUser = userDao.authenticateUser(username, password);

        if (loggedInUser != null) {
            // Đăng nhập thành công!
            JOptionPane.showMessageDialog(this,
                    "Đăng nhập thành công! Xin chào " + (loggedInUser.getFullName() != null ? loggedInUser.getFullName()
                            : loggedInUser.getUsername()),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);

            // Đóng cửa sổ Login
            this.dispose();

            // Mở cửa sổ chính (MainWindow) - Bạn sẽ tạo lớp này ở bước sau
            MainWindow mainWindow = new MainWindow(loggedInUser); // Truyền thông tin user đã đăng nhập
            mainWindow.setVisible(true);

        } else {
            // Đăng nhập thất bại
            JOptionPane.showMessageDialog(this,
                    "Tên đăng nhập hoặc mật khẩu không đúng.",
                    "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

}