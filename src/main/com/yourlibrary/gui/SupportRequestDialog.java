package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.DatabaseUtil;
import main.com.yourlibrary.dao.SupportRequestDao;
import main.com.yourlibrary.model.SupportRequest;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class SupportRequestDialog extends JDialog {

    private JTextField subjectField;
    private JTextArea messageArea;
    private JButton sendButton;
    private JButton cancelButton;

    private User currentUser; // Người dùng đang đăng nhập gửi yêu cầu
    private SupportRequestDao supportRequestDao;

    public SupportRequestDialog(Frame owner, boolean modal, User currentUser, SupportRequestDao supportRequestDao) {
        super(owner, modal);
        this.currentUser = currentUser;
        this.supportRequestDao = supportRequestDao;

        setTitle("Gửi yêu cầu hỗ trợ");
        setSize(400, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // --- Panel Form ---
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Subject
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Chủ đề (Nếu có):"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        subjectField = new JTextField(25);
        formPanel.add(subjectField, gbc);
        gbc.weightx = 0; // Reset

        // Message
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Nội dung*:"), gbc); // Căn lề trên trái
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0; // Cho phép mở rộng
        messageArea = new JTextArea(10, 25);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        formPanel.add(scrollPane, gbc);

        // --- Panel Nút bấm ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sendButton = new JButton("Gửi yêu cầu");
        cancelButton = new JButton("Hủy");
        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);

        // Thêm vào Dialog
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Xử lý sự kiện ---
        sendButton.addActionListener(e -> sendRequest());
        cancelButton.addActionListener(e -> dispose());
    }

    private void sendRequest() {
        String subject = subjectField.getText().trim();
        String message = messageArea.getText().trim();

        if (message.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nội dung yêu cầu không được để trống.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            messageArea.requestFocus(); // Đặt focus vào ô nội dung
            return;
        }

        // Tạo đối tượng SupportRequest
        SupportRequest newRequest = new SupportRequest();
        newRequest.setUserId(currentUser.getUserId()); // Lấy ID từ user đang đăng nhập
        newRequest.setSubject(subject); // Subject có thể trống
        newRequest.setMessage(message);

        // Gọi DAO để lưu
        boolean success = supportRequestDao.addRequest(newRequest);

        if (success) {
            JOptionPane.showMessageDialog(this, "Đã gửi yêu cầu hỗ trợ thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Đóng dialog nếu thành công
        } else {
            JOptionPane.showMessageDialog(this, "Gửi yêu cầu thất bại. Vui lòng thử lại.", "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

}