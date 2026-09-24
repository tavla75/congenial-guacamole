package org.example.resturang_kassa;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentReference =
            UUID.randomUUID().toString();

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;

    private Instant createdAt = Instant.now();

    @OneToOne
    private RestaurantOrder order;

    protected Payment(){
    }

    public Payment(RestaurantOrder order, BigDecimal amount){
        this.order = order;
        this.amount = amount;
    }
    public Long getId(){
        return id;
    }
    public String getPaymentReference(){
        return paymentReference;
    }
    public BigDecimal getAmount(){
        return amount;
    }
    public PaymentStatus getStatus(){
        return  status;
    }
    public void setStatus(PaymentStatus status){
        this.status = status;
    }
    public RestaurantOrder getOrder(){
        return order;
    }


}
