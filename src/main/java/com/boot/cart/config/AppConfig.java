package com.boot.cart.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;

@Configuration
public class AppConfig {


    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${product.service.url}")
    private String productServiceUrl;

    //TODO here I would create 2 restTemplates using something like new RestTemplateBuilder().rootUri("http://user.service.url").build() and use just relative paths and reference them in the client classes by name.
    @Bean(name = "userServiceRestTemplate")
    public RestTemplate userServiceRestTemplateUrl() {
        return new RestTemplateBuilder().rootUri(userServiceUrl).build();
    }

    @Bean(name = "productServiceRestTemplate")
    public RestTemplate productServiceRestTemplateUrl() {
        return new RestTemplateBuilder().rootUri(productServiceUrl).build();
    }


    @Bean
    public ProductServiceClient cartServiceClient() {
        return new ProductServiceClient();
    }

    @Bean
    public UserServiceClient userServiceClient() {
        return new UserServiceClient();
    }


}
