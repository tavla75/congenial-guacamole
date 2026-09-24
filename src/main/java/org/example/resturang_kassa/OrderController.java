package org.example.resturang_kassa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final RestaurantOrderRepository orders;

    public OrderController(RestaurantOrderRepository orders) {
        this.orders = orders;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestBody CreateOrderRequest request) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Ordern måste innehålla minst en produkt");
        }

        RestaurantOrder order = new RestaurantOrder();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest item : request.items()) {
            if (item.productName() == null || item.productName().isBlank()
                    || item.quantity() <= 0
                    || item.unitPrice() == null
                    || item.unitPrice().signum() < 0) {
                throw new IllegalArgumentException("Ogiltig orderrad");
            }

            order.getItems().add(new OrderItem(
                    item.productName().trim(),
                    item.quantity(),
                    item.unitPrice()
            ));
            total = total.add(item.unitPrice()
                    .multiply(BigDecimal.valueOf(item.quantity())));
        }

        order.setTotal(total);
        return toResponse(orders.save(order));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return orders.findById(id)
                .map(this::toResponse)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order hittades inte: " + id));
    }

    private OrderResponse toResponse(RestaurantOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getTotal(),
                order.getStatus(),
                order.getItems().stream()
                        .map(item -> new OrderItemResponse(
                                item.getProductName(),
                                item.getQuantity(),
                                item.getUnitPrice()
                        ))
                        .toList()
        );
    }
}
