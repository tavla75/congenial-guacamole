package org.example.resturang_kassa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "product_categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    protected ProductCategory() {
    }

    public ProductCategory(String name) {
        this.name = name.trim();
    }

    public String getName() {
        return name;
    }
}
