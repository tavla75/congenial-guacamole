package org.example.resturang_kassa;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

final class CashRegisterWindow {

    private CashRegisterWindow() {
    }

    static void showWindow(
            ProductRepository productRepository,
            RestaurantOrderRepository orderRepository,
            PaymentService paymentService) {
        JFrame window = new JFrame("Resturang kassa");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(true);
        window.setMinimumSize(new Dimension(640, 400));
        window.setSize(900, 600);
        window.setLocationRelativeTo(null);

        CardLayout pages = new CardLayout();
        JPanel content = new JPanel(pages);
        content.add(createWelcomePage(pages, content), "welcome");
        content.add(createGridPage(productRepository, orderRepository, paymentService), "grid");

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

    private static JPanel createGridPage(
            ProductRepository productRepository,
            RestaurantOrderRepository orderRepository,
            PaymentService paymentService) {
        JPanel gridPage = new JPanel(new BorderLayout(16, 16));
        gridPage.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        OrderPanel order = new OrderPanel(orderRepository, paymentService);
        JPanel products = new JPanel(new GridLayout(0, 4, 12, 12));
        Runnable refreshProducts = () -> {
            products.removeAll();
            for (Product product : productRepository.findAll()) {
                addProduct(products, product, order);
            }
            products.revalidate();
            products.repaint();
        };
        refreshProducts.run();

        JPanel orderPage = new JPanel(new BorderLayout(16, 16));
        JLabel title = new JLabel("Beställ produkter");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        orderPage.add(title, BorderLayout.NORTH);
        orderPage.add(products, BorderLayout.CENTER);
        orderPage.add(order, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Beställning", orderPage);
        tabs.addTab("Admin", createAdminPage(productRepository, refreshProducts));
        gridPage.add(tabs, BorderLayout.CENTER);

        return gridPage;
    }

    private static JPanel createAdminPage(
            ProductRepository productRepository, Runnable refreshProducts) {
        JPanel adminPage = new JPanel(new BorderLayout(12, 12));
        adminPage.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Ändra produktpriser");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        adminPage.add(heading, BorderLayout.NORTH);

        JPanel productEditor = new JPanel(new GridLayout(0, 1, 8, 8));
        for (Product product : productRepository.findAll()) {
            productEditor.add(createProductEditorRow(product, productRepository, refreshProducts));
        }
        adminPage.add(productEditor, BorderLayout.CENTER);

        JButton addProductButton = new JButton("Lägg till produkt");
        addProductButton.addActionListener(event -> {
            try {
                productRepository.save(new Product("Ny produkt", BigDecimal.ZERO));
                refreshProducts.run();
                productEditor.removeAll();
                for (Product product : productRepository.findAll()) {
                    productEditor.add(createProductEditorRow(
                            product, productRepository, refreshProducts));
                }
                productEditor.revalidate();
                productEditor.repaint();
            } catch (RuntimeException exception) {
                showError(adminPage, exception);
            }
        });
        adminPage.add(addProductButton, BorderLayout.SOUTH);
        return adminPage;
    }

    private static JPanel createProductEditorRow(
            Product product, ProductRepository repository, Runnable refreshProducts) {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 8));
        JTextField nameField = new JTextField(product.getName());
        JTextField priceField = new JTextField(product.getPrice().toPlainString());
        JButton saveButton = new JButton("Spara");
        saveButton.addActionListener(event -> {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                if (price.signum() < 0 || nameField.getText().isBlank()) {
                    throw new IllegalArgumentException("Ange ett produktnamn och ett pris som inte är negativt.");
                }
                product.setName(nameField.getText().trim());
                product.setPrice(price);
                repository.save(product);
                refreshProducts.run();
                JOptionPane.showMessageDialog(row, "Produkten sparades.");
            } catch (RuntimeException exception) {
                showError(row, exception);
            }
        });
        row.add(nameField);
        row.add(priceField);
        row.add(saveButton);
        return row;
    }

    private static void addProduct(JPanel products, Product product, OrderPanel order) {
        JPanel tile = new JPanel(new BorderLayout(4, 4));
        tile.setBorder(BorderFactory.createLineBorder(new java.awt.Color(23, 107, 135)));

        JLabel productLabel = new JLabel(
                String.format("%s - %s kr", product.getName(), product.getPrice()), JLabel.CENTER);
        tile.add(productLabel, BorderLayout.CENTER);

        JButton orderButton = new JButton("Beställ");
        orderButton.addActionListener(event -> order.addItem(product));
        tile.add(orderButton, BorderLayout.SOUTH);

        products.add(tile);
    }

    private static void showError(JPanel parent, RuntimeException exception) {
        JOptionPane.showMessageDialog(
                parent,
                exception.getMessage() == null ? "Ett fel inträffade." : exception.getMessage(),
                "Fel",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static final class OrderPanel extends JPanel {
        private final javax.swing.DefaultListModel<OrderLine> orderItems =
                new javax.swing.DefaultListModel<>();
        private final JLabel totalLabel = new JLabel("Totalt: 0,00 kr");
        private final RestaurantOrderRepository orders;
        private final PaymentService payments;

        private OrderPanel(RestaurantOrderRepository orders, PaymentService payments) {
            super(new BorderLayout(8, 8));
            this.orders = orders;
            this.payments = payments;
            setBorder(BorderFactory.createTitledBorder("Aktuell beställning"));
            setPreferredSize(new Dimension(240, 0));

            JList<OrderLine> itemList = new JList<>(orderItems);
            itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            add(itemList, BorderLayout.CENTER);

            payButton = new JButton("Betala");
            payButton.setEnabled(false);

            JButton removeButton = new JButton("Ta bort vald");
            removeButton.addActionListener(event -> {
                int selectedIndex = itemList.getSelectedIndex();
                if (selectedIndex >= 0) {
                    orderItems.remove(selectedIndex);
                    updateTotal();
                    payButton.setEnabled(!orderItems.isEmpty());
                }
            });

            JPanel footer = new JPanel(new BorderLayout(4, 4));
            footer.add(totalLabel, BorderLayout.NORTH);
            footer.add(removeButton, BorderLayout.SOUTH);

            payButton.addActionListener(event -> {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        String.format(Locale.forLanguageTag("sv-SE"),
                                "Betala %s kr?", calculateTotal()),
                        "Bekräfta betalning",
                        JOptionPane.YES_NO_OPTION
                );
                if (result == JOptionPane.YES_OPTION) {
                    completePayment();
                }
            });

            footer.add(payButton, BorderLayout.CENTER);
            add(footer, BorderLayout.SOUTH);

            removeButton.addActionListener(event -> payButton.setEnabled(!orderItems.isEmpty()));
        }

        private final JButton payButton;

        private void addItem(Product product) {
            orderItems.addElement(new OrderLine(
                    product.getName(), product.getPrice()));
            updateTotal();
            payButton.setEnabled(true);
        }

        private BigDecimal calculateTotal() {
            BigDecimal total = BigDecimal.ZERO;
            for (int index = 0; index < orderItems.size(); index++) {
                total = total.add(orderItems.get(index).price());
            }
            return total;
        }

        private void updateTotal() {
            totalLabel.setText("Totalt: " + calculateTotal().toPlainString() + " kr");
        }

        private void completePayment() {
            try {
                RestaurantOrder order = new RestaurantOrder();
                BigDecimal total = calculateTotal();
                order.setTotal(total);
                for (int index = 0; index < orderItems.size(); index++) {
                    OrderLine item = orderItems.get(index);
                    order.getItems().add(new OrderItem(item.name(), 1, item.price()));
                }
                RestaurantOrder savedOrder = orders.save(order);
                PaymentResponse pendingPayment = payments.createPayment(savedOrder.getId());
                PaymentResponse completedPayment =
                        payments.completeTestPayment(pendingPayment.paymentReference());

                orderItems.clear();
                updateTotal();
                payButton.setEnabled(false);
                JOptionPane.showMessageDialog(
                        this,
                        "Betalning klar: " + completedPayment.amount().toPlainString() + " kr",
                        "Betalning",
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (RuntimeException exception) {
                showError(this, exception);
            }
        }
    }

    private record OrderLine(String name, BigDecimal price) {
        @Override
        public String toString() {
            return name + " - " + price.toPlainString() + " kr";
        }
    }
}
