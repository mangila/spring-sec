package com.github.mangila.resilience.deliveryservice;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class MemoryDatabase {

    private final ConcurrentHashMap<String, Delivery> deliveries = new ConcurrentHashMap<>();

    public void save(Delivery delivery) {
        deliveries.put(delivery.orderId(), delivery);
    }

    public Delivery findById(String id) {
        return deliveries.get(id);
    }

}