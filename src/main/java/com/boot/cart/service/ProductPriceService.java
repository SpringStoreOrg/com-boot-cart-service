package com.boot.cart.service;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.ProductPriceDTO;
import com.boot.cart.exception.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductPriceService {
    private Map<String, Long> productPriceMap = new HashMap<>();
    private ProductServiceClient productServiceClient;

    @Scheduled(fixedDelay = 10*60*1000)
    public void loadProductPrices(){
        productPriceMap = productServiceClient.getProductPrices().stream()
                .collect(Collectors.toMap(ProductPriceDTO::getName, ProductPriceDTO::getPrice));
        log.info("{} product prices were loaded", productPriceMap.keySet().size());
    }

    public Long getPrice(String productName) {
        if (productPriceMap.containsKey(productName)) {
            return productPriceMap.get(productName);
        }
        throw new EntityNotFoundException("Could not find product name:" + productName);
    }
}
