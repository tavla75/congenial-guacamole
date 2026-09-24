package org.example.resturang_kassa;

import java.math.BigDecimal;

public record OrderItemResponse(
        String productName,
        int quantity,
        BigDecimal unitPrice
) {
}
