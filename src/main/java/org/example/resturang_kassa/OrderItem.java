package org.example.resturang_kassa;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private int quantity;
    private BigDecimal unitPrice;

    protected  OrderItem(){

    }

    public OrderItem(String productName, int quantity, BigDecimal unitPrice){
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal(){
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getProductName(){
        return productName;
    }
    public int getQuantity(){
        return quantity;
    }
    public BigDecimal getUnitPrice(){
        return unitPrice;
    }
}
