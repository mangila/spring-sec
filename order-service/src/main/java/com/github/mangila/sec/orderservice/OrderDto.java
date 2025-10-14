package com.github.mangila.sec.orderservice;

import java.util.List;

public record OrderDto(
        String customerId,
        List<String> items,
        String address,
        String status,
        double price
) {
    public static final OrderDto EMPTY = new OrderDto(null, null, null, null, 0.0);

    public boolean isEmpty() {
        return EMPTY.equals(this);
    }

}