package com.github.mangila.sec.orderservice;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemoryDatabase {

    private final ConcurrentHashMap<String, Order> orders = new ConcurrentHashMap<>();

    public void save(Order order) {
        orders.put(order.id(), order);
    }

    public Order findById(String id) {
        return orders.get(id);
    }

}