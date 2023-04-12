package com.boot.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableFeignClients
@ConditionalOnProperty(name = "spring.enable.scheduling")
@EntityScan(basePackages = {"com.boot.cart.model"})
public class SpringCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCartServiceApplication.class, args);
    }

}
