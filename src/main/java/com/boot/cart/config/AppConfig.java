package com.boot.cart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;

@Configuration
public class AppConfig {
	
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
}
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
