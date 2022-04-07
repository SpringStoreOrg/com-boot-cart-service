package com.boot.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;

@Configuration
public class AppConfig {
	
	//TODO here I would create 2 restTemplates using something like new RestTemplateBuilder().rootUri("http://user.service.url").build() and use just relative paths and reference them in the client classes by name.
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
