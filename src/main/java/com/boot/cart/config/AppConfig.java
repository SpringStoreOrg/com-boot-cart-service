package com.boot.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;

@Configuration
public class AppConfig {
	
	@Bean
	public RestTemplate template() {
	    return new RestTemplate();
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
