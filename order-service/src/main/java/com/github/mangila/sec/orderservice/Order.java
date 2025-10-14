package com.github.mangila.sec.orderservice;

import java.util.List;

public record Order(String id,
                    String customerId,
                    List<String> items,
                    double price) {
}