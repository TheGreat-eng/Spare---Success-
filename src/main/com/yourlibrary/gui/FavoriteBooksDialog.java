package main.com.yourlibrary.gui;

import main.com.yourlibrary.model.Book; // Dùng Book model
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class FavoriteBooksDialog extends JDialog {

    private JTable favoriteTable;
    private DefaultTableModel tableModel;

    public FavoriteBooksDialog(Frame owner, String title, List<Book> favoriteBooks) {
        super(owner, true); // Modal dialog
        setTitle(title);
        setSize(700, 350);
        setLocationRelativeTo(owner);

        // --- Table ---
        // Các cột giống bảng sách chính
        String[] columnNames = { "ID", "ISBN", "Tiêu đề", "Tác giả", "Thể loại", "Năm XB" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        favoriteTable = new JTable(tableModel);
        favoriteTable.setAutoCreateRowSorter(true);

        // Đổ dữ liệu
        if (favoriteBooks != null) {
            for (Book book : favoriteBooks) {
                Vector<Object> row = new Vector<>();
                row.add(book.getBookId());
                row.add(book.getIsbn());
                row.add(book.getTitle());
                row.add(book.getAuthor());
                row.add(book.getGenre());
                row.add(book.getPublicationYear() > 0 ? book.getPublicationYear() : "");
                tableModel.addRow(row);
            }
        }

        JScrollPane scrollPane = new JScrollPane(favoriteTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Nút đóng ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}