package com.github.mangila.sec.orderservice;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CredentialsStore {

    /**
     * This is a sample token to show how to initialize the store.
     * But should be loaded from env variables or any trusted way to inject values into the application.
     */
    public static final String DELIVERY_READER_TOKEN = "delivery-reader-token";
    private final ConcurrentHashMap<String, List<String>> credentials = new ConcurrentHashMap<>();

    public void save(String key, List<String> credentials) {
        this.credentials.put(key, credentials);
    }

    public List<String> getCredentials(String id) {
        return credentials.get(id);
    }

    /**
     * This is a sample method to show how to initialize the store.
     * But should be replaced with a remote call to a secure store.
     * That is already loaded with the credentials
     */
    @PostConstruct
    void init() {
        String username = "delivery-reader-username";
        String password = "delivery-reader-password";
        credentials.put(DELIVERY_READER_TOKEN, List.of(username, password));
    }

}
