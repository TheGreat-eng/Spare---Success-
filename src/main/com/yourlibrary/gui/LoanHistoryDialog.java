package main.com.yourlibrary.gui;

import main.com.yourlibrary.model.BorrowRecord;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

public class LoanHistoryDialog extends JDialog {

    private JTable historyTable;
    private DefaultTableModel tableModel;

    public LoanHistoryDialog(Window owner, String title, List<BorrowRecord> history) {
        super(owner, ModalityType.MODELESS); // Modeless để không chặn cửa sổ chính
        setTitle(title);
        setSize(800, 400); // Kích thước lớn hơn để xem lịch sử
        setLocationRelativeTo(owner);

        // --- Table ---
        String[] columnNames = { "Loan ID", "ISBN", "Tiêu đề sách", "Ngày mượn", "Ngày hẹn trả", "Ngày trả",
                "Trạng thái" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        historyTable = new JTable(tableModel);
        historyTable.setAutoCreateRowSorter(true); // Cho phép sắp xếp cột

        // Định dạng ngày tháng
        SimpleDateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        // Đổ dữ liệu vào bảng
        if (history != null) {
            for (BorrowRecord record : history) {
                Vector<Object> row = new Vector<>();
                row.add(record.getLoanId());
                row.add(record.getBookIsbn());
                row.add(record.getBookTitle());
                row.add(record.getBorrowDate() != null ? timestampFormat.format(record.getBorrowDate()) : "");
                row.add(record.getDueDate() != null ? dateFormat.format(record.getDueDate()) : "");
                row.add(record.getReturnDate() != null ? dateFormat.format(record.getReturnDate()) : ""); // Hiển thị
                                                                                                          // ngày trả
                                                                                                          // nếu có
                row.add(record.getStatus());
                tableModel.addRow(row);
            }
        }

        JScrollPane scrollPane = new JScrollPane(historyTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Nút đóng ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}