package com.boot.cart.client;

import java.util.*;

import com.boot.cart.dto.BatchUpdateDTO;
import com.boot.cart.dto.ProductDTO;
import com.boot.cart.dto.ProductPriceDTO;
import com.boot.cart.dto.ReserveDTO;
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

    public void reserve(String productName, int quantity) {
        productServiceRestTemplate.put("/{productName}/reserve", getRequest(new ReserveDTO(quantity)), productName);
    }

    public void reserveRelease(String productName, int quantity){
        productServiceRestTemplate.put("/{productName}/reserve/release", getRequest(new ReserveDTO(quantity)), productName);
    }

    public void batchReserve(List<BatchUpdateDTO> batchUpdate){
        productServiceRestTemplate.put("/batch/reserve", getRequest(batchUpdate));
    }

    public void batchReserveRelease(List<BatchUpdateDTO> batchUpdate){
        productServiceRestTemplate.put("/batch/reserve/release", getRequest(batchUpdate));
    }

    public List<ProductPriceDTO> getProductPrices() {
        ResponseEntity<ProductPriceDTO[]> response = productServiceRestTemplate.getForEntity("/prices", ProductPriceDTO[].class);
        if (response.getBody() != null) {
            return Arrays.asList(response.getBody());
        }
        return new ArrayList<>();
    }

    private HttpEntity getRequest(Object body){
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity(body, headers);
    }
}