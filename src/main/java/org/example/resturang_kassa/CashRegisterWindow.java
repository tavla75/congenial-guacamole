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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        JTabbedPane categoryTabs = new JTabbedPane();
        Runnable refreshProducts = () -> {
            refreshCategoryTabs(categoryTabs, productRepository.findAll(), order);
        };
        refreshProducts.run();

        JPanel orderPage = new JPanel(new BorderLayout(16, 16));
        JLabel title = new JLabel("Beställ produkter");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        orderPage.add(title, BorderLayout.NORTH);
        orderPage.add(categoryTabs, BorderLayout.CENTER);
        orderPage.add(order, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Beställning", orderPage);
        tabs.addTab("Admin", createAdminPage(productRepository, refreshProducts));
        gridPage.add(tabs, BorderLayout.CENTER);

        return gridPage;
    }

    private static void refreshCategoryTabs(
            JTabbedPane tabs, List<Product> products, OrderPanel order) {
        tabs.removeAll();
        Map<String, List<Product>> categories = new LinkedHashMap<>();
        categories.put("Mat", new ArrayList<>());
        categories.put("Dryck", new ArrayList<>());
        for (Product product : products) {
            categories.computeIfAbsent(product.getCategory(), key -> new ArrayList<>())
                    .add(product);
        }

        categories.forEach((category, categoryProducts) -> {
            JPanel productGrid = new JPanel(new GridLayout(0, 4, 12, 12));
            for (Product product : categoryProducts) {
                addProduct(productGrid, product, order);
            }
            tabs.addTab(category, productGrid);
        });
    }

    private static JPanel createAdminPage(
            ProductRepository productRepository, Runnable refreshProducts) {
        JPanel adminPage = new JPanel(new BorderLayout(12, 12));
        adminPage.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel heading = new JLabel("Ändra produktpriser");
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
        adminPage.add(heading, BorderLayout.NORTH);

        List<ProductEditorRow> editorRows = new ArrayList<>();
        JPanel productEditor = new JPanel(new GridLayout(0, 1, 8, 8));
        for (Product product : productRepository.findAll()) {
            ProductEditorRow row = new ProductEditorRow(
                    product, productRepository, refreshProducts, deletedRow -> {
                        editorRows.remove(deletedRow);
                        productEditor.remove(deletedRow);
                        productEditor.revalidate();
                        productEditor.repaint();
                    });
            editorRows.add(row);
            productEditor.add(row);
        }
        adminPage.add(productEditor, BorderLayout.CENTER);

        JButton saveAllButton = new JButton("Spara alla");
        saveAllButton.addActionListener(event -> {
            int savedCount = 0;
            List<String> failedProducts = new ArrayList<>();
            for (ProductEditorRow row : List.copyOf(editorRows)) {
                if (row.save(false)) {
                    savedCount++;
                } else {
                    failedProducts.add(row.productName());
                }
            }
            refreshProducts.run();
            if (failedProducts.isEmpty()) {
                JOptionPane.showMessageDialog(
                        adminPage, "Alla " + savedCount + " produkter sparades.");
            } else {
                JOptionPane.showMessageDialog(
                        adminPage,
                        "Sparade " + savedCount + " produkter. Kunde inte spara: "
                                + String.join(", ", failedProducts),
                        "Vissa produkter kunde inte sparas",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        JButton addProductButton = new JButton("Lägg till produkt");
        addProductButton.addActionListener(event -> {
            try {
                Product newProduct = productRepository.save(
                        new Product("Ny produkt", BigDecimal.ZERO, "Mat"));
                ProductEditorRow row = new ProductEditorRow(
                        newProduct, productRepository, refreshProducts, deletedRow -> {
                            editorRows.remove(deletedRow);
                            productEditor.remove(deletedRow);
                            productEditor.revalidate();
                            productEditor.repaint();
                        });
                editorRows.add(row);
                productEditor.add(row);
                refreshProducts.run();
                productEditor.revalidate();
                productEditor.repaint();
            } catch (RuntimeException exception) {
                showError(adminPage, exception);
            }
        });
        JPanel actions = new JPanel(new BorderLayout(8, 8));
        actions.add(saveAllButton, BorderLayout.WEST);
        actions.add(addProductButton, BorderLayout.EAST);
        adminPage.add(actions, BorderLayout.SOUTH);
        return adminPage;
    }

    private static final class ProductEditorRow extends JPanel {
        private final Product product;
        private final ProductRepository repository;
        private final Runnable refreshProducts;
        private final java.util.function.Consumer<ProductEditorRow> removeFromEditor;
        private final JTextField categoryField;
        private final JTextField nameField;
        private final JTextField priceField;

        private ProductEditorRow(
                Product product,
                ProductRepository repository,
                Runnable refreshProducts,
                java.util.function.Consumer<ProductEditorRow> removeFromEditor) {
            super(new GridLayout(1, 5, 8, 8));
            this.product = product;
            this.repository = repository;
            this.refreshProducts = refreshProducts;
            this.removeFromEditor = removeFromEditor;
            categoryField = new JTextField(product.getCategory());
            nameField = new JTextField(product.getName());
            priceField = new JTextField(product.getPrice().toPlainString());

            JButton saveButton = new JButton("Spara");
            saveButton.addActionListener(event -> save(true));
            JButton deleteButton = new JButton("Ta bort");
            deleteButton.addActionListener(event -> delete());

            add(categoryField);
            add(nameField);
            add(priceField);
            add(saveButton);
            add(deleteButton);
        }

        private boolean save(boolean showSuccess) {
            try {
                BigDecimal price = new BigDecimal(priceField.getText().trim().replace(',', '.'));
                if (price.signum() < 0 || nameField.getText().isBlank()
                        || categoryField.getText().isBlank()) {
                    throw new IllegalArgumentException(
                            "Ange kategori, produktnamn och ett pris som inte är negativt.");
                }
                product.setCategory(categoryField.getText());
                product.setName(nameField.getText().trim());
                product.setPrice(price);
                repository.save(product);
                refreshProducts.run();
                if (showSuccess) {
                    JOptionPane.showMessageDialog(this, "Produkten sparades.");
                }
                return true;
            } catch (RuntimeException exception) {
                showError(this, exception);
                return false;
            }
        }

        private String productName() {
            return nameField.getText().isBlank() ? "Produkt " + product.getId()
                    : nameField.getText();
        }

        private void delete() {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Ta bort produkten \"" + product.getName() + "\"?",
                    "Bekräfta borttagning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                try {
                    repository.delete(product);
                    removeFromEditor.accept(this);
                    refreshProducts.run();
                } catch (RuntimeException exception) {
                    showError(this, exception);
                }
            }
        }

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

            payButton.addActionListener(event -> choosePaymentMethod());

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

        private void choosePaymentMethod() {
            String[] options = {"Kort", "Kontant", "Avbryt"};
            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Välj betalningssätt för " + calculateTotal().toPlainString() + " kr",
                    "Betala",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]
            );

            if (choice == 0) {
                completePayment("Kort", BigDecimal.ZERO);
            } else if (choice == 1) {
                takeCashPayment();
            }
        }

        private void takeCashPayment() {
            String enteredAmount = JOptionPane.showInputDialog(
                    this,
                    "Mottaget belopp (kr):"
            );
            if (enteredAmount == null) {
                return;
            }

            try {
                BigDecimal received = new BigDecimal(
                        enteredAmount.trim().replace(',', '.'));
                BigDecimal total = calculateTotal();
                if (received.signum() < 0 || received.compareTo(total) < 0) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Det mottagna beloppet måste vara minst "
                                    + total.toPlainString() + " kr.",
                            "För lågt belopp",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                completePayment("Kontant", received.subtract(total));
            } catch (NumberFormatException exception) {
                JOptionPane.showMessageDialog(
                        this,
                        "Skriv in ett giltigt belopp.",
                        "Ogiltigt belopp",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        }

        private void completePayment(String paymentMethod, BigDecimal change) {
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

                String message = "Betalning klar med " + paymentMethod + ": "
                        + completedPayment.amount().toPlainString() + " kr";
                if ("Kontant".equals(paymentMethod)) {
                    message += "\nVäxel: " + change.toPlainString() + " kr";
                }
                JOptionPane.showMessageDialog(
                        this,
                        message,
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
