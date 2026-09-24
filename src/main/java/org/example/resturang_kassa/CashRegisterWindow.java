package org.example.resturang_kassa;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.Locale;

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

        CardLayout pages = new CardLayout();
        JPanel content = new JPanel(pages);
        content.add(createWelcomePage(pages, content), "welcome");
        content.add(createGridPage(), "grid");

        window.setContentPane(content);
        window.setVisible(true);
    }

    private static JPanel createWelcomePage(CardLayout pages, JPanel content) {
        JPanel welcomePage = new JPanel(new BorderLayout(16, 16));
        welcomePage.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel welcomeMessage = new JLabel("Välkommen till restaurangkassan", JLabel.CENTER);
        welcomeMessage.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        welcomePage.add(welcomeMessage, BorderLayout.CENTER);

        JButton continueButton = new JButton("Fortsätt");
        continueButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        continueButton.addActionListener(event -> pages.show(content, "grid"));
        welcomePage.add(continueButton, BorderLayout.SOUTH);

        return welcomePage;
    }

    private static JPanel createGridPage() {
        JPanel gridPage = new JPanel(new BorderLayout(16, 16));
        gridPage.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        OrderPanel order = new OrderPanel();
        JPanel products = new JPanel(new GridLayout(0, 4, 12, 12));
        double[] prices = {99, 119, 79, 25};
        addProduct(products, "Hamburgare", prices, 0, order);
        addProduct(products, "Pizza", prices, 1, order);
        addProduct(products, "Sallad", prices, 2, order);
        addProduct(products, "Dryck", prices, 3, order);

        JButton addProductButton = new JButton("Lägg till produkt");
        addProductButton.addActionListener(event ->
                addProduct(products, "Ny produkt", prices, 0, order));

        JPanel orderPage = new JPanel(new BorderLayout(16, 16));
        JLabel title = new JLabel("Beställ produkter");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        orderPage.add(title, BorderLayout.NORTH);
        orderPage.add(products, BorderLayout.CENTER);
        orderPage.add(order, BorderLayout.EAST);
        orderPage.add(addProductButton, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Beställning", orderPage);
        tabs.addTab("Admin", createAdminPage(prices));
        gridPage.add(tabs, BorderLayout.CENTER);

        return gridPage;
    }

    private static JPanel createAdminPage(double[] prices) {
        JPanel adminPage = new JPanel(new BorderLayout(12, 12));
        adminPage.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Ändra produktpriser");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        adminPage.add(heading, BorderLayout.NORTH);

        JPanel priceEditor = new JPanel(new GridLayout(0, 2, 12, 12));
        addPriceEditor(priceEditor, "Hamburgare", prices, 0);
        addPriceEditor(priceEditor, "Pizza", prices, 1);
        addPriceEditor(priceEditor, "Sallad", prices, 2);
        addPriceEditor(priceEditor, "Dryck", prices, 3);
        adminPage.add(priceEditor, BorderLayout.CENTER);

        JLabel savedMessage = new JLabel(" ");
        JButton saveButton = new JButton("Spara priser");
        saveButton.addActionListener(event -> savedMessage.setText("Priser sparade."));

        JPanel footer = new JPanel(new BorderLayout(8, 8));
        footer.add(saveButton, BorderLayout.WEST);
        footer.add(savedMessage, BorderLayout.CENTER);
        adminPage.add(footer, BorderLayout.SOUTH);
        return adminPage;
    }

    private static void addPriceEditor(JPanel editor, String name, double[] prices, int index) {
        editor.add(new JLabel(name));
        JTextField price = new JTextField(String.format(Locale.US, "%.2f", prices[index]));
        editor.add(price);
        price.addActionListener(event -> updatePrice(price, prices, index));
        price.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent event) {
                updatePrice(price, prices, index);
            }
        });
    }

    private static void updatePrice(JTextField field, double[] prices, int index) {
        try {
            double value = Double.parseDouble(field.getText().replace(',', '.'));
            if (value >= 0) {
                prices[index] = value;
            }
        } catch (NumberFormatException ignored) {
            field.setText(String.format(Locale.US, "%.2f", prices[index]));
        }
    }

    private static void addProduct(
            JPanel products, String name, double[] prices, int priceIndex, OrderPanel order) {
        JPanel product = new JPanel(new BorderLayout(4, 4));
        product.setBorder(BorderFactory.createLineBorder(new java.awt.Color(23, 107, 135)));

        JTextField productName = new JTextField(name);
        productName.setHorizontalAlignment(JTextField.CENTER);
        product.add(productName, BorderLayout.CENTER);

        JButton orderButton = new JButton("Beställ");
        orderButton.addActionListener(event ->
                order.addItem(productName.getText().trim(), prices[priceIndex]));
        product.add(orderButton, BorderLayout.SOUTH);

        products.add(product);
        products.revalidate();
        products.repaint();
    }

    private static final class OrderPanel extends JPanel {
        private final javax.swing.DefaultListModel<String> orderItems = new javax.swing.DefaultListModel<>();
        private final JLabel totalLabel = new JLabel("Totalt: 0,00 kr");
        private double total;

        private OrderPanel() {
            super(new BorderLayout(8, 8));
            setBorder(BorderFactory.createTitledBorder("Aktuell beställning"));
            setPreferredSize(new Dimension(240, 0));

            JList<String> itemList = new JList<>(orderItems);
            itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(itemList, BorderLayout.CENTER);

            JButton removeButton = new JButton("Ta bort vald");
            removeButton.addActionListener(event -> {
                int selectedIndex = itemList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    String selectedItem = orderItems.remove(selectedIndex);
                    total -= extractPrice(selectedItem);
                    updateTotal();
                }
            });

            JPanel footer = new JPanel(new BorderLayout(4, 4));
            footer.add(totalLabel, BorderLayout.NORTH);
            footer.add(removeButton, BorderLayout.SOUTH);
            add(footer, BorderLayout.SOUTH);
        }

        private void addItem(String name, double price) {
            String itemName = name.isBlank() ? "Namnlös produkt" : name;
            orderItems.addElement(String.format(Locale.forLanguageTag("sv-SE"),
                    "%s - %.2f kr", itemName, price));
            total += price;
            updateTotal();
        }

        private void updateTotal() {
            totalLabel.setText(String.format(Locale.forLanguageTag("sv-SE"),
                    "Totalt: %.2f kr", total));
        }

        private double extractPrice(String item) {
            String price = item.substring(item.lastIndexOf(" - ") + 3)
                    .replace(" kr", "")
                    .replace(',', '.');
            return Double.parseDouble(price);
        }
    }
}
