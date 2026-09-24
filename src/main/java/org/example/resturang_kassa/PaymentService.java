package org.example.resturang_kassa;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final RestaurantOrderRepository orders;
    private final PaymentRepository payments;

    public PaymentService(
            RestaurantOrderRepository orders,
            PaymentRepository payments
    ){
        this.orders = orders;
        this.payments = payments;
    }

    @Transactional
    public PaymentResponse createPayment(Long orderId){
        RestaurantOrder order = orders.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order hittades inte: " + orderId
                        ));
        if (order.getStatus() == OrderStatus.PAID) {
            throw new IllegalStateException(
                    "Ordern är redan betald"
            );
        }

        order.setStatus(OrderStatus.PAYMENT_PENDING);

        Payment payment = new Payment(
                order,
                order.getTotal()
        );

        Payment savedPayment = payments.save(payment);

        return toResponse(savedPayment);
    }

    @Transactional
    public PaymentResponse completeTestPayment(
            String paymentReference
    ){
        Payment payment = payments
                .findByPaymentReference(paymentReference)
                .orElseThrow(()->
                        new IllegalArgumentException(
                                "Betalning hittade inte"
                        ));
        payment.setStatus(PaymentStatus.PAID);
        payment.getOrder().setStatus(OrderStatus.PAID);

        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(
            String paymentReference
    ){
        Payment payment = payments
                .findByPaymentReference(paymentReference)
                .orElseThrow(()->
                        new IllegalArgumentException(
                                "Betalning hittades inte"
                        ));
        return toResponse(payment);
    }

    private PaymentResponse toResponse(Payment payment){
        return new PaymentResponse(
                payment.getPaymentReference(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getStatus()
        );
    }
}
