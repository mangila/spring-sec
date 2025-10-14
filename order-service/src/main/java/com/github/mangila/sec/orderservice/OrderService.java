package com.github.mangila.sec.orderservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class OrderService {
    private final DeliveryServiceClient deliveryServiceClient;
    private final MemoryDatabase memoryDatabase;
    private final ObjectMapper objectMapper;

    public OrderService(DeliveryServiceClient deliveryServiceClient,
                        MemoryDatabase memoryDatabase,
                        ObjectMapper objectMapper) {
        this.deliveryServiceClient = deliveryServiceClient;
        this.memoryDatabase = memoryDatabase;
        this.objectMapper = objectMapper;
    }

    public String createNewOrder(CreateNewOrderRequest request) {
        String id = UUID.randomUUID().toString();
        Order order = new Order(id,
                request.customerId(),
                request.items(),
                2.0);
        deliveryServiceClient.enqueueNewOrder(objectMapper.createObjectNode()
                .put("id", order.id())
                .put("address", request.address()));
        memoryDatabase.save(order);
        return id;
    }

    public OrderDto findById(String orderId) {
        return Stream.of(memoryDatabase.findById(orderId))
                .filter(Objects::nonNull)
                .map(order -> {
                    ObjectNode json = deliveryServiceClient.getDelivery(order.id());
                    return new OrderDto(
                            order.customerId(),
                            order.items(),
                            json.optional("address")
                                    .map(JsonNode::asText)
                                    .orElse("UNKNOWN"),
                            json.optional("status")
                                    .map(JsonNode::asText)
                                    .orElse("UNKNOWN"),
                            order.price()
                    );
                })
                .findFirst()
                .orElse(OrderDto.EMPTY);
    }
}