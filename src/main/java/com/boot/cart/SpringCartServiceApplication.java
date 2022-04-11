package com.boot.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
@EntityScan(basePackages = {"com.boot.services.model"})
public class SpringCartServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCartServiceApplication.class, args);
    }

}
