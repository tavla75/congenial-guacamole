package org.example.resturang_kassa;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal total,
        OrderStatus status,
        List<OrderItemResponse> items
) {
}
