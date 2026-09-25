package org.example.resturang_kassa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private BigDecimal price;

    private String category = "Mat";

    protected Product() {
    }

    public Product(String name, BigDecimal price) {
        this(name, price, "Mat");
    }

    public Product(String name, BigDecimal price, String category) {
        this.name = name;
        this.price = price;
        setCategory(category);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getCategory() {
        if (category != null && !category.isBlank()) {
            return category;
        }
        return "Dryck".equalsIgnoreCase(name) ? "Dryck" : "Mat";
    }

    public void setCategory(String category) {
        this.category = category == null || category.isBlank() ? "Mat" : category.trim();
    }
}
