package main.com.yourlibrary;

import main.com.yourlibrary.gui.LoginWindow;
import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Chạy GUI trên Event Dispatch Thread (EDT) để đảm bảo an toàn luồng
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                LoginWindow loginWindow = new LoginWindow();
                loginWindow.setVisible(true);
            }
        });
    }
}