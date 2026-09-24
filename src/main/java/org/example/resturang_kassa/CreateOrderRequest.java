package org.example.resturang_kassa;

import java.util.List;

public record CreateOrderRequest(List<OrderItemRequest> items) {
}
