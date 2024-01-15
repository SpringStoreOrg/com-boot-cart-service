package com.boot.cart.client;

import com.boot.cart.dto.PagedProductResponseDTO;
import com.boot.cart.dto.ProductInfoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@FeignClient(name="product-service")
public interface ProductServiceClient {

    @GetMapping
    @ResponseBody
    ResponseEntity<PagedProductResponseDTO> callGetAllProductsFromUser(@RequestParam("productNames") String productNames,
                                                                       @RequestParam(value = "includeInactive") Boolean includeInactive,
                                                                       @RequestParam(value = "size") int size);

    @GetMapping("/{productName}/info")
    @ResponseBody
    ProductInfoDTO getProductInfoByProductName(@PathVariable("productName") String productName);

    @GetMapping("/info")
    @ResponseBody
    List<ProductInfoDTO> getProductInfo(@RequestParam String productNames);
}