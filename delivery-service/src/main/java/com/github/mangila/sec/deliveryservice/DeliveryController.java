package com.github.mangila.sec.deliveryservice;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/delivery")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping(
            value = "{orderId}",
            produces = "application/json")
    @RolesAllowed("READER")
    public ResponseEntity<String> getDelivery(@PathVariable String orderId) {
        String deliveryJson = deliveryService.getDelivery(orderId);
        if (deliveryJson.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deliveryJson);
    }

    @PostMapping(value = "{orderId}")
    @RolesAllowed("WRITER")
    public ResponseEntity<?> fulfillDelivery(@PathVariable String orderId) {
        boolean result = deliveryService.fulfill(orderId);
        if (!result) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

}