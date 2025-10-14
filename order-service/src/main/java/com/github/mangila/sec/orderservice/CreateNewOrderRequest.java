package com.github.mangila.sec.orderservice;

import java.util.List;
import java.util.Objects;

public record CreateNewOrderRequest(
        String customerId,
        List<String> items,
        String address
) {
    public CreateNewOrderRequest {
        Objects.requireNonNull(customerId, "customerId must not be null");
        if (customerId.isBlank()) throw new IllegalArgumentException("customerId must not be blank");
        Objects.requireNonNull(items, "items must not be null");
        if (items.isEmpty()) throw new IllegalArgumentException("items must not be empty");
        Objects.requireNonNull(address, "address must not be null");
        if (address.isBlank()) throw new IllegalArgumentException("address must not be blank");
    }
}