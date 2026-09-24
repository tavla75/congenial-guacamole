package org.example.resturang_kassa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

@SpringBootApplication
public class ResturangKassaApplication {

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(ResturangKassaApplication.class, args);

        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("Cannot open the restaurant cash register window in headless mode.");
            return;
        }

        SwingUtilities.invokeLater(CashRegisterWindow::showWindow);
    }

}
