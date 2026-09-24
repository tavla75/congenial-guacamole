package org.example.resturang_kassa;

import java.math.BigDecimal;
public record PaymentResponse(
        String paymentReference,
        Long orderId,
        BigDecimal amount,
        PaymentStatus status
) {
}
