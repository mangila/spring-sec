package com.github.mangila.sec.orderservice;

import java.util.List;

public record CreateNewOrderRequest(
        String customerId,
        List<String> items,
        String address
) {
}