package main.com.yourlibrary.gui;

import main.com.yourlibrary.dao.BookDao;
import main.com.yourlibrary.dao.BorrowDao;
import main.com.yourlibrary.dao.UserDao;
import main.com.yourlibrary.model.BorrowRecord;
import main.com.yourlibrary.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;
import java.sql.Date; // Import java.sql.Date

public class CirculationPanel extends JPanel {

    private UserDao userDao;
    private BorrowDao borrowDao;
    private User currentUser;
    private MainWindow mainWindow;

    private JTextField userSearchField;
    private JButton findUserButton;
    private JLabel selectedUserInfoLabel;
    private User selectedUser = null;

    private JTable activeLoansTable;
    private DefaultTableModel activeLoansTableModel;

    private JButton returnButton;
    private JButton renewButton;
    private JButton issueButton;
    private JButton viewHistoryButton;

    public CirculationPanel(User currentUser, UserDao userDao, BorrowDao borrowDao, MainWindow mainWindow) {
        this.currentUser = currentUser;
        this.userDao = userDao;
        this.borrowDao = borrowDao;
        this.mainWindow = mainWindow;
        setLayout(new BorderLayout(10, 10));
        System.out.println("[CirculationPanel] Initializing..."); // DEBUG

        // --- Panel User Search (Top) ---
        JPanel userPanel = new JPanel(new BorderLayout(5, 5));
        userPanel.setBorder(BorderFactory.createTitledBorder("Chọn Người dùng (Thành viên)"));
        JPanel userSearchSubPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userSearchSubPanel.add(new JLabel("Tìm User (ID/Username):"));
        userSearchField = new JTextField(15);
        userSearchSubPanel.add(userSearchField);
        findUserButton = new JButton("Tìm User");
        userSearchSubPanel.add(findUserButton);
        selectedUserInfoLabel = new JLabel("Chưa chọn người dùng.");
        selectedUserInfoLabel.setFont(selectedUserInfoLabel.getFont().deriveFont(Font.ITALIC));
        selectedUserInfoLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        userPanel.add(userSearchSubPanel, BorderLayout.NORTH);
        userPanel.add(selectedUserInfoLabel, BorderLayout.CENTER);

        // --- Panel Active Loans (Center) ---
        JPanel loansPanel = new JPanel(new BorderLayout(5, 5));
        loansPanel.setBorder(BorderFactory.createTitledBorder("Sách Đang Mượn"));
        String[] columnNames = { "Loan ID", "Book ID", "ISBN", "Tiêu đề sách", "Ngày mượn", "Ngày hẹn trả",
                "Trạng thái" };
        activeLoansTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        activeLoansTable = new JTable(activeLoansTableModel);
        activeLoansTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        activeLoansTable.getColumnModel().getColumn(1).setMinWidth(0);
        activeLoansTable.getColumnModel().getColumn(1).setMaxWidth(0);
        activeLoansTable.getColumnModel().getColumn(1).setPreferredWidth(0);
        JScrollPane scrollPane = new JScrollPane(activeLoansTable);
        loansPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Panel Actions (Bottom/Right of Loans Panel) ---
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        returnButton = new JButton("Trả sách đã chọn");
        renewButton = new JButton("Gia hạn sách đã chọn");
        issueButton = new JButton("Cho mượn sách mới...");
        viewHistoryButton = new JButton("Xem lịch sử User");
        actionButtonPanel.add(returnButton);
        actionButtonPanel.add(renewButton);
        actionButtonPanel.add(issueButton);
        actionButtonPanel.add(viewHistoryButton);
        loansPanel.add(actionButtonPanel, BorderLayout.SOUTH);

        // --- Add Panels to Main Panel ---
        add(userPanel, BorderLayout.NORTH);
        add(loansPanel, BorderLayout.CENTER);

        // --- Initial State ---
        setLoanActionsEnabled(false);
        issueButton.setEnabled(false);

        // --- Action Listeners ---
        findUserButton.addActionListener(e -> findUser());
        userSearchField.addActionListener(e -> findUser());
        returnButton.addActionListener(e -> returnSelectedBook());
        renewButton.addActionListener(e -> renewSelectedLoan());
        issueButton.addActionListener(e -> openIssueBookDialog());
        viewHistoryButton.addActionListener(e -> viewSelectedUserHistory());

        activeLoansTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = activeLoansTable.getSelectedRow() != -1;
                System.out.println("[CirculationPanel] Table selection changed. Row selected: " + rowSelected); // DEBUG
                setLoanActionsEnabled(rowSelected);
            }
        });
        System.out.println("[CirculationPanel] Initialization complete."); // DEBUG
    } // End Constructor

    // --- Action Methods ---
    private void findUser() {
        String keyword = userSearchField.getText().trim();
        System.out.println("[CirculationPanel] Finding user with keyword: " + keyword); // DEBUG
        if (keyword.isEmpty()) {
            clearSelectedUser();
            return;
        }
        User foundUser = null;
        try {
            int userId = Integer.parseInt(keyword);
            foundUser = userDao.findUserById(userId);
        } catch (NumberFormatException e) {
            List<User> users = userDao.searchUsers(keyword);
            if (users.size() == 1)
                foundUser = users.get(0);
            else if (users.size() > 1) {
                JOptionPane.showMessageDialog(this, "Tìm thấy nhiều người dùng...", "Nhiều kết quả",
                        JOptionPane.INFORMATION_MESSAGE);
                clearSelectedUser();
                return;
            }
        }
        if (foundUser != null) {
            System.out.println("[CirculationPanel] User found: " + foundUser.getUsername()); // DEBUG
            selectUser(foundUser);
        } else {
            System.out.println("[CirculationPanel] User not found."); // DEBUG
            JOptionPane.showMessageDialog(this, "Không tìm thấy user: " + keyword, "Không tìm thấy",
                    JOptionPane.WARNING_MESSAGE);
            clearSelectedUser();
        }
    }

    private void selectUser(User user) {
        System.out.println(
                "[CirculationPanel] Selecting user: " + user.getUsername() + " (ID: " + user.getUserId() + ")"); // DEBUG
        selectedUser = user;
        selectedUserInfoLabel.setText("Đã chọn: " + user.getFullName() + " (ID: " + user.getUserId() + ", Username: "
                + user.getUsername() + ")");
        selectedUserInfoLabel.setFont(selectedUserInfoLabel.getFont().deriveFont(Font.PLAIN));
        issueButton.setEnabled(true); // Enable issue button when user is selected
        loadActiveLoansForSelectedUser();
    }

    private void clearSelectedUser() {
        System.out.println("[CirculationPanel] Clearing selected user."); // DEBUG
        selectedUser = null;
        selectedUserInfoLabel.setText("Chưa chọn người dùng.");
        selectedUserInfoLabel.setFont(selectedUserInfoLabel.getFont().deriveFont(Font.ITALIC));
        if (activeLoansTableModel != null)
            activeLoansTableModel.setRowCount(0);
        setLoanActionsEnabled(false);
        issueButton.setEnabled(false); // Disable issue button
        if (userSearchField != null)
            userSearchField.requestFocus();
    }

    private void loadActiveLoansForSelectedUser() {
        System.out.println("[CirculationPanel] Loading active loans for user: " +
                (selectedUser != null ? selectedUser.getUsername() : "null"));

        // Clear existing data
        activeLoansTableModel.setRowCount(0);
        setLoanActionsEnabled(false);

        if (selectedUser == null) {
            System.out.println("[CirculationPanel] No user selected, skipping load");
            return;
        }

        // Get data from DAO
        List<BorrowRecord> loans = borrowDao.findActiveLoansByUser(selectedUser.getUserId());
        System.out
                .println("[CirculationPanel] Received " + (loans != null ? loans.size() : "null") + " loans from DAO");

        // Setup date formatters
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat timestampFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Process and display data
        if (loans != null && !loans.isEmpty()) {
            for (BorrowRecord loan : loans) {
                if (loan == null) {
                    System.err.println("[CirculationPanel] Null loan record found, skipping");
                    continue;
                }

                System.out.println("[CirculationPanel] Processing loan ID: " + loan.getLoanId() +
                        ", Status: " + loan.getStatus());

                try {
                    Vector<Object> row = new Vector<>();
                    row.add(loan.getLoanId());
                    row.add(loan.getBookId());
                    row.add(loan.getBookIsbn() != null ? loan.getBookIsbn() : "");
                    row.add(loan.getBookTitle() != null ? loan.getBookTitle() : "");

                    // Format dates safely
                    row.add(loan.getBorrowDate() != null ? timestampFormat.format(loan.getBorrowDate()) : "N/A");
                    row.add(loan.getDueDate() != null ? dateFormat.format(loan.getDueDate()) : "N/A");

                    row.add(loan.getStatus() != null ? loan.getStatus() : "UNKNOWN");

                    activeLoansTableModel.addRow(row);
                    System.out.println("[CirculationPanel] Added row for loan ID: " + loan.getLoanId());
                } catch (Exception e) {
                    System.err.println("[CirculationPanel] Error processing loan ID " + loan.getLoanId() +
                            ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            System.out.println("[CirculationPanel] No active loans found for user");
        }

        System.out.println("[CirculationPanel] Table now contains " +
                activeLoansTableModel.getRowCount() + " rows");
    }

    private void setLoanActionsEnabled(boolean enabled) {
        System.out.println("[CirculationPanel] Setting loan action buttons enabled: " + enabled); // DEBUG
        returnButton.setEnabled(enabled);
        renewButton.setEnabled(enabled);
    }

    private void returnSelectedBook() {
        System.out.println("[CirculationPanel] returnSelectedBook action initiated."); // DEBUG
        int selectedRow = activeLoansTable.getSelectedRow();
        if (selectedRow == -1 || selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn user và lượt mượn.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int loanId = (Integer) activeLoansTableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) activeLoansTableModel.getValueAt(selectedRow, 3);
        System.out.println("[CirculationPanel] Attempting to return loanId: " + loanId + " for book: " + bookTitle); // DEBUG
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Xác nhận trả sách:\n'" + bookTitle + "'\ncho user: " + selectedUser.getUsername() + "?",
                "Xác nhận trả sách", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = borrowDao.returnBook(loanId);
            System.out.println("[CirculationPanel] returnBook DAO result: " + success); // DEBUG
            if (success) {
                JOptionPane.showMessageDialog(this, "Trả sách thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadActiveLoansForSelectedUser();
                mainWindow.refreshBookList();
            } else
                JOptionPane.showMessageDialog(this, "Trả sách thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renewSelectedLoan() {
        System.out.println("[CirculationPanel] renewSelectedLoan action initiated."); // DEBUG
        int selectedRow = activeLoansTable.getSelectedRow();
        if (selectedRow == -1 || selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn user và lượt mượn.", "Thiếu thông tin",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        int loanId = (Integer) activeLoansTableModel.getValueAt(selectedRow, 0);
        String bookTitle = (String) activeLoansTableModel.getValueAt(selectedRow, 3);
        System.out.println("[CirculationPanel] Attempting to renew loanId: " + loanId + " for book: " + bookTitle); // DEBUG
        String newDateStr = JOptionPane.showInputDialog(this,
                "Nhập ngày hẹn trả mới cho sách:\n'" + bookTitle + "'\n(Định dạng yyyy-MM-dd):", "Gia hạn sách",
                JOptionPane.PLAIN_MESSAGE);
        if (newDateStr == null || newDateStr.trim().isEmpty()) {
            System.out.println("[CirculationPanel] Renew cancelled by user.");
            return;
        } // DEBUG
        newDateStr = newDateStr.trim();
        java.sql.Date newDueDate = null;
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            inputFormat.setLenient(false);
            java.util.Date utilDate = inputFormat.parse(newDateStr);
            java.sql.Date today = new java.sql.Date(System.currentTimeMillis());
            if (utilDate.before(today)) {
                int confirmPast = JOptionPane.showConfirmDialog(this, "Ngày gia hạn là ngày quá khứ?", "Cảnh báo",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirmPast == JOptionPane.NO_OPTION) {
                    System.out.println("[CirculationPanel] Renew cancelled due to past date confirmation.");
                    return;
                } // DEBUG
            }
            newDueDate = new java.sql.Date(utilDate.getTime());
            System.out.println("[CirculationPanel] Parsed new due date: " + newDueDate); // DEBUG
        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Định dạng ngày không hợp lệ (yyyy-MM-dd).", "Lỗi định dạng",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        int confirmation = JOptionPane.showConfirmDialog(this,
                "Xác nhận gia hạn sách:\n'" + bookTitle + "'\nđến ngày: "
                        + new SimpleDateFormat("dd/MM/yyyy").format(newDueDate) + "?",
                "Xác nhận gia hạn", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            boolean success = borrowDao.renewLoan(loanId, newDueDate);
            System.out.println("[CirculationPanel] renewLoan DAO result: " + success); // DEBUG
            if (success) {
                JOptionPane.showMessageDialog(this, "Gia hạn thành công!", "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                loadActiveLoansForSelectedUser();
            } else
                JOptionPane.showMessageDialog(this, "Gia hạn thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
        } else {
            System.out.println("[CirculationPanel] Renew confirmation cancelled by user."); // DEBUG
        }
    }

    private void openIssueBookDialog() {
        System.out.println("[CirculationPanel] openIssueBookDialog action initiated."); // DEBUG
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng.", "Chưa chọn User",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        BookDao tempBookDao = new BookDao();
        IssueBookDialog issueDialog = new IssueBookDialog(mainWindow, true, selectedUser, tempBookDao, borrowDao);
        issueDialog.setVisible(true);
        System.out.println("[CirculationPanel] IssueBookDialog closed. Refreshing active loans."); // DEBUG
        loadActiveLoansForSelectedUser();
        if (mainWindow != null)
            mainWindow.refreshBookList();
    }

    private void viewSelectedUserHistory() {
        System.out.println("[CirculationPanel] viewSelectedUserHistory action initiated."); // DEBUG
        if (selectedUser == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn người dùng.", "Chưa chọn User",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<BorrowRecord> history = borrowDao.getLoanHistoryByUser(selectedUser.getUserId());
        System.out.println("[CirculationPanel] History list size: " + (history != null ? history.size() : "null")); // DEBUG
        String dialogTitle = "Lịch sử mượn/trả của: " + selectedUser.getUsername() + " (ID: " + selectedUser.getUserId()
                + ")";
        LoanHistoryDialog historyDialog = new LoanHistoryDialog(mainWindow, dialogTitle, history);
        historyDialog.setVisible(true);
    }

} // End class CirculationPanel