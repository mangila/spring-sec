package com.github.mangila.sec.deliveryservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class OrderServiceListener {

    private final MemoryDatabase database;
    private final ObjectMapper objectMapper;

    public OrderServiceListener(MemoryDatabase database,
                                ObjectMapper objectMapper) {
        this.database = database;
        this.objectMapper = objectMapper;
    }

    @RabbitListener(
            queues = "new-order-to-delivery-queue",
            containerFactory = "rabbitListenerContainerFactory",
            errorHandler = "rabbitListenerErrorHandler",
            executor = "rabbitVirtualThreadTaskExecutor")
    public void listen(Message message, Channel channel) throws IOException {
        log.info("Received message: {}", message);
        log.info("channel: {}", channel);
        ObjectNode jsonObj = objectMapper.readValue(message.getBody(), ObjectNode.class);
        String orderId = jsonObj.get("id").asText();
        String address = jsonObj.get("address").asText();
        log.trace("Order Message: {} - {}", orderId, address);
        Delivery delivery = new Delivery(orderId, address, "PENDING");
        log.trace("Order saved to database: {}", delivery);
        database.save(delivery);
    }
}