package com.boot.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${product.service.url}")
    private String productServiceUrl;

    @Bean(name = "userServiceRestTemplate")
    public RestTemplate userServiceRestTemplateUrl() {
        return new RestTemplateBuilder().rootUri(userServiceUrl).build();
    }

    @Bean(name = "productServiceRestTemplate")
    public RestTemplate productServiceRestTemplateUrl() {
        return new RestTemplateBuilder().rootUri(productServiceUrl).build();
    }

}
