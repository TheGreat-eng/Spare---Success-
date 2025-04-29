package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.SupportRequestDao;
import main.com.yourlibrary.model.SupportRequest;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp; // Import Timestamp
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

public class SupportManagementPanel extends JPanel {

    private SupportRequestDao supportRequestDao;
    private User currentUser; // Librarian/Admin logged in
    private MainWindow mainWindow;

    // Components
    private JTable requestTable;
    private DefaultTableModel tableModel;
    private JButton viewDetailsButton; // Xem chi tiết nội dung
    private JButton markProcessingButton;
    private JButton markResolvedButton;
    private JButton refreshButton;

    public SupportManagementPanel(User currentUser, SupportRequestDao supportRequestDao, MainWindow mainWindow) {
        this.currentUser = currentUser;
        this.supportRequestDao = supportRequestDao;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- Table (Center) ---
        String[] columnNames = { "ID", "Người gửi", "Chủ đề", "Trạng thái", "Thời gian gửi" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        requestTable = new JTable(tableModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Điều chỉnh độ rộng cột
        requestTable.getColumnModel().getColumn(0).setPreferredWidth(40); // ID
        requestTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Người gửi
        requestTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Chủ đề
        requestTable.getColumnModel().getColumn(3).setPreferredWidth(80); // Trạng thái
        requestTable.getColumnModel().getColumn(4).setPreferredWidth(140); // Thời gian

        JScrollPane scrollPane = new JScrollPane(requestTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Panel (South) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewDetailsButton = new JButton("Xem Nội dung");
        markProcessingButton = new JButton("Đánh dấu 'Đang xử lý'");
        markResolvedButton = new JButton("Đánh dấu 'Đã giải quyết'");
        refreshButton = new JButton("Làm mới danh sách");

        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(markProcessingButton);
        buttonPanel.add(markResolvedButton);
        buttonPanel.add(refreshButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Initial State ---
        setActionsEnabled(false); // Vô hiệu hóa nút khi chưa chọn

        // --- Load initial data ---
        loadSupportRequests();

        // --- Listeners ---
        refreshButton.addActionListener(e -> loadSupportRequests());
        viewDetailsButton.addActionListener(e -> viewSelectedRequestDetails());
        markProcessingButton.addActionListener(e -> updateSelectedRequestStatus("PROCESSING"));
        markResolvedButton.addActionListener(e -> updateSelectedRequestStatus("RESOLVED"));

        // Listener khi chọn hàng trong bảng
        requestTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                setActionsEnabled(requestTable.getSelectedRow() != -1);
            }
        });
    }

    private void setActionsEnabled(boolean enabled) {
        viewDetailsButton.setEnabled(enabled);
        markProcessingButton.setEnabled(enabled);
        markResolvedButton.setEnabled(enabled);
    }

    private void loadSupportRequests() {
        System.out.println("[SupportPanel] Loading support requests...");
        // Chạy trên luồng riêng để tránh block UI
        SwingWorker<List<SupportRequest>, Void> worker = new SwingWorker<List<SupportRequest>, Void>() {
            @Override
            protected List<SupportRequest> doInBackground() throws Exception {
                return supportRequestDao.getAllRequests();
            }

            @Override
            protected void done() {
                try {
                    List<SupportRequest> requests = get();
                    System.out
                            .println("[SupportPanel] Found " + (requests != null ? requests.size() : 0) + " requests.");
                    tableModel.setRowCount(0); // Clear table
                    setActionsEnabled(false); // Disable buttons
                    SimpleDateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                    if (requests != null) {
                        for (SupportRequest req : requests) {
                            Vector<Object> row = new Vector<>();
                            row.add(req.getRequestId());
                            row.add(req.getRequesterUsername()); // Lấy từ JOIN
                            row.add(req.getSubject());
                            row.add(req.getStatus());
                            row.add(req.getRequestedAt() != null ? timestampFormat.format(req.getRequestedAt()) : "");
                            tableModel.addRow(row);
                        }
                    }
                    System.out.println("[SupportPanel] Request table updated.");
                } catch (Exception e) {
                    System.err.println("[SupportPanel] Error loading support requests: " + e.getMessage());
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(SupportManagementPanel.this,
                            "Không thể tải danh sách yêu cầu hỗ trợ.",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Xem chi tiết nội dung yêu cầu
    // Xem chi tiết nội dung yêu cầu
    private void viewSelectedRequestDetails() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) {
            System.out.println("[SupportPanel] View details called with no selection."); // DEBUG
            return; // Không có hàng nào được chọn
        }

        int requestId = (Integer) tableModel.getValueAt(selectedRow, 0);
        System.out.println("[SupportPanel] Viewing details for requestId: " + requestId); // DEBUG

        // Gọi DAO để lấy thông tin chi tiết (bao gồm cả message)
        SupportRequest fullRequest = supportRequestDao.getRequestById(requestId);

        if (fullRequest != null) {
            System.out.println("[SupportPanel] Full request details obtained."); // DEBUG
            // Tạo JTextArea để hiển thị nội dung message (cho phép xuống dòng)
            JTextArea textArea = new JTextArea(10, 40); // Kích thước gợi ý
            textArea.setText(fullRequest.getMessage()); // Đặt nội dung message
            textArea.setWrapStyleWord(true);
            textArea.setLineWrap(true);
            textArea.setEditable(false); // Không cho sửa
            textArea.setCaretPosition(0); // Cuộn lên đầu

            // Đặt JTextArea vào JScrollPane để có thể cuộn nếu nội dung dài
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(450, 250)); // Đặt kích thước ưu tiên cho scrollpane

            // Lấy thêm thông tin để hiển thị trong tiêu đề hoặc thông báo
            String title = "Chi tiết Yêu cầu #" + requestId;
            String details = String.format("Người gửi: %s\nChủ đề: %s\nTrạng thái: %s\nThời gian gửi: %s",
                    fullRequest.getRequesterUsername(),
                    fullRequest.getSubject() != null ? fullRequest.getSubject() : "(Không có)",
                    fullRequest.getStatus(),
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fullRequest.getRequestedAt()));

            // Tạo một panel để chứa cả thông tin chi tiết và nội dung
            JPanel messagePanel = new JPanel(new BorderLayout(5, 5));
            messagePanel.add(new JLabel("<html>" + details.replace("\n", "<br>") + "</html>"), BorderLayout.NORTH); // Hiển
                                                                                                                    // thị
                                                                                                                    // HTML
                                                                                                                    // để
                                                                                                                    // xuống
                                                                                                                    // dòng
            messagePanel.add(scrollPane, BorderLayout.CENTER);

            // Hiển thị JOptionPane với panel tùy chỉnh
            JOptionPane.showMessageDialog(this, // parentComponent
                    messagePanel, // message là panel chứa mọi thứ
                    title, // Tiêu đề dialog
                    JOptionPane.INFORMATION_MESSAGE); // Kiểu icon
        } else {
            System.err.println("[SupportPanel] Failed to get full request details for ID: " + requestId); // DEBUG
            JOptionPane.showMessageDialog(this, "Không thể lấy chi tiết yêu cầu #" + requestId + ". Vui lòng thử lại.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Cập nhật trạng thái
    private void updateSelectedRequestStatus(String newStatus) {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1)
            return;

        int requestId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 3);

        // Không cho cập nhật nếu trạng thái đã là cái muốn cập nhật hoặc đã Resolved
        if (newStatus.equalsIgnoreCase(currentStatus)) {
            JOptionPane.showMessageDialog(this, "Yêu cầu đã ở trạng thái '" + newStatus + "'.", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if ("RESOLVED".equalsIgnoreCase(currentStatus) && !"RESOLVED".equalsIgnoreCase(newStatus)) {
            JOptionPane.showMessageDialog(this, "Không thể đổi trạng thái của yêu cầu đã giải quyết.", "Không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn đổi trạng thái yêu cầu #" + requestId + " thành '" + newStatus + "'?",
                "Xác nhận cập nhật", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = supportRequestDao.updateRequestStatus(requestId, newStatus, currentUser.getUserId());
            if (success) {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadSupportRequests(); // Tải lại danh sách để cập nhật bảng
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

} // End class SupportManagementPanel