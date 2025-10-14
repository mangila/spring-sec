package com.github.mangila.sec.deliveryservice;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.RabbitListenerErrorHandler;
import org.springframework.amqp.rabbit.support.ListenerExecutionFailedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
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

import java.util.Map;

@Configuration
@Slf4j
@EnableMethodSecurity(
        prePostEnabled = false,
        jsr250Enabled = true
)
// @EnableWebSecurity - Optional, if you want to use the default Spring Security configuration
public class Config {

    @Bean
    TaskExecutor rabbitVirtualThreadTaskExecutor() {
        return new VirtualThreadTaskExecutor("rabbitmq-listener-");
    }

    @Bean
    RabbitListenerErrorHandler rabbitListenerErrorHandler() {
        return new RabbitListenerErrorHandler() {
            @Override
            public Object handleError(Message amqpMessage,
                                      Channel channel,
                                      org.springframework.messaging.Message<?> message,
                                      ListenerExecutionFailedException exception) throws Exception {
                log.error("RabbitMQ listener error: {}", exception.getMessage(), exception);
                return null;
            }
        };
    }

    /**
     * Configures the security filter chain for the application.
     * This method sets up HTTP security settings including disabled CSRF,
     * requiring authentication for all requests, stateless session management,
     * and HTTP Basic authentication with a custom realm.
     *
     * @param http the {@link HttpSecurity} object used to configure security settings
     * @return the configured {@link SecurityFilterChain} instance
     * @throws Exception in case a configuration error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            @Value("${spring.application.name}") String applicationName
    ) throws Exception {
        Assert.hasText(applicationName, "`spring.application.name` must be set");
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(matcher -> matcher
                        .anyRequest()
                        .authenticated())
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(httpSecurityHttpBasicConfigurer -> {
                    httpSecurityHttpBasicConfigurer.realmName(applicationName);
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
                .username("delivery-reader-username")
                .password(encoder.encode("delivery-reader-password"))
                .roles(roles.get("READER"))
                .build();

        var writer = User.builder()
                .username("delivery-writer-username")
                .password(encoder.encode("delivery-writer-password"))
                .roles(roles.get("READER"), roles.get("WRITER"))
                .build();

        return new InMemoryUserDetailsManager(reader, writer);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}