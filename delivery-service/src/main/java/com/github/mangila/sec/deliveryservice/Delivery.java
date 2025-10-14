package com.github.mangila.sec.deliveryservice;

public record Delivery(String orderId,
                       String address,
                       String status) {
}