package org.example.resturang_kassa;

import java.math.BigDecimal;

public record OrderItemRequest(
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
