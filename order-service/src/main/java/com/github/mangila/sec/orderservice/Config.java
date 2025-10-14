package com.github.mangila.sec.orderservice;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

@Configuration
public class Config {

    public static final String NEW_ORDER_TO_DELIVERY_QUEUE = "new-order-to-delivery-queue";

    @Bean
    public Queue createNewOrderToDeliveryQueue() {
        return new Queue(NEW_ORDER_TO_DELIVERY_QUEUE, Boolean.FALSE);
    }

    @Bean
    RestClient deliveryRestClient(
            @Value("${spring.application.name}") String applicationName,
            @Value("${application.integration.delivery-service.url}") String url) {
        Assert.hasText(applicationName, "`spring.application.name` must be set");
        Assert.hasText(url, "`application.integration.delivery-service.url` must be set");
        return RestClient.builder()
                .baseUrl(url)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, applicationName)
                .build();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(matcher -> matcher
                        .anyRequest()
                        .authenticated())
                .httpBasic(httpSecurityHttpBasicConfigurer -> {
                    httpSecurityHttpBasicConfigurer.realmName("Order Service");
                });
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.builder()
                        .username("order-username")
                        .password(encoder.encode("order-password"))
                        .build()
        );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}