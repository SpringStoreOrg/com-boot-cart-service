package com.boot.cart.client;

import java.util.*;
import java.util.stream.Collectors;

import com.boot.cart.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;


@Component
public class ProductServiceClient {

    @Autowired
    private RestTemplate productServiceRestTemplate;

    public List<ProductDTO> callGetAllProductsFromUser(String productNames, Boolean includeInactive ) {

        return Arrays.asList(Objects.requireNonNull(productServiceRestTemplate.getForEntity(Constants.GET_ALL_PRODUCTS_FOR_USER, ProductDTO[].class, productNames, includeInactive).getBody()));
    }

    public ProductInfoDTO getProductInfo(String productName) {
        ResponseEntity<ProductInfoDTO> responseEntity = productServiceRestTemplate.getForEntity("/{productName}/info", ProductInfoDTO.class, productName);
        return responseEntity.getBody();
    }

    public ProductInfoDTO[] getProductsInfo(List<String> productNames) {
        ResponseEntity<ProductInfoDTO[]> responseEntity = productServiceRestTemplate.getForEntity("/info?productNames={productNames}", ProductInfoDTO[].class, String.join(",", productNames));
        return responseEntity.getBody();
    }
}