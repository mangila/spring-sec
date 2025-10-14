package com.github.mangila.sec.orderservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class DeliveryServiceClient {

    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;
    private final RestClient restClient;
    private final String basePath;

    public DeliveryServiceClient(ObjectMapper objectMapper,
                                 @Qualifier("deliveryRestClient") RestClient restClient,
                                 @Value("${application.integration.delivery-service.basepath}") String basePath,
                                 RabbitTemplate rabbitTemplate) {
        Assert.hasText(basePath, "`application.integration.delivery-service.basepath` must be set");
        this.objectMapper = objectMapper;
        this.restClient = restClient;
        this.basePath = basePath;
        this.rabbitTemplate = rabbitTemplate;
    }

    public void enqueueNewOrder(ObjectNode objectNode) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(objectNode);
            Message message = MessageBuilder.withBody(bytes)
                    .setContentType(MediaType.APPLICATION_JSON_VALUE)
                    .setTimestamp(Date.from(Instant.now()))
                    .build();
            rabbitTemplate.send(Config.NEW_ORDER_TO_DELIVERY_QUEUE, message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectNode getDelivery(String orderId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(basePath.concat("/{orderId}"))
                        .build(orderId))
                .retrieve()
                .body(ObjectNode.class);
    }
}