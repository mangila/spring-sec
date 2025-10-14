package com.github.mangila.sec.orderservice;

import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Configuration
@EnableMethodSecurity(
        prePostEnabled = false,
        jsr250Enabled = true
)
// @EnableWebSecurity - Optional, if you want to use the default Spring Security configuration
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
                .requestInterceptor(new BasicAuthenticationInterceptor("delivery-username", "delivery-password"))
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, applicationName)
                .build();
    }


    /**
     * Configures the security filter chain for the application.
     * The configuration disables CSRF protection, enforces authentication for all requests,
     * sets session management to stateless, and configures HTTP Basic authentication with a specified realm name.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during the configuration of the security filter
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(matcher -> matcher
                        .anyRequest()
                        .authenticated())
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpSecurityHttpBasicConfigurer -> {
                    httpSecurityHttpBasicConfigurer.realmName("Order Service");
                });
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        var roles = Map.of(
                "READER", "READER",
                "WRITER", "WRITER"
        );

        var reader = User.builder()
                .username("order-reader-username")
                .password(encoder.encode("order-reader-password"))
                .roles(roles.get("READER"))
                .build();

        var writer = User.builder()
                .username("order-writer-username")
                .password(encoder.encode("order-writer-password"))
                .roles(roles.get("READER"), roles.get("WRITER"))
                .build();

        return new InMemoryUserDetailsManager(reader, writer);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}