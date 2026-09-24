package org.example.resturang_kassa;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

final class CashRegisterWindow {

    private CashRegisterWindow() {
    }

    static void showWindow() {
        JFrame window = new JFrame("Resturang kassa");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setMinimumSize(new Dimension(640, 400));
        window.setSize(900, 600);
        window.setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel welcomeMessage = new JLabel("Välkommen till restaurangkassan", JLabel.CENTER);
        welcomeMessage.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        content.add(welcomeMessage, BorderLayout.CENTER);

        window.setContentPane(content);
        window.setVisible(true);
    }
}
