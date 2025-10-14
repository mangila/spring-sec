package com.github.mangila.sec.deliveryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DeliveryService {

    private final MemoryDatabase database;
    private final ObjectMapper objectMapper;

    public DeliveryService(MemoryDatabase database,
                           ObjectMapper objectMapper) {
        this.database = database;
        this.objectMapper = objectMapper;
    }

    public String getDelivery(String orderId) {
        Delivery delivery = database.findById(orderId);
        log.info("Delivery found: {}", delivery);
        if (delivery == null) {
            return "";
        }
        ObjectNode json = objectMapper.valueToTree(delivery);
        log.info("Delivery JSON: {}", json);
        return json.toString();
    }

    public boolean fulfill(String orderId) {
        Delivery delivery = database.findById(orderId);
        if (delivery == null) {
            return false;
        }
        if (!"PENDING".equals(delivery.status())) {
            throw new IllegalStateException("Order is not in PENDING state");
        }
        database.save(new Delivery(orderId, delivery.address(), "DELIVERED"));
        return true;
    }
}