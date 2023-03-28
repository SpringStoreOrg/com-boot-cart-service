package com.boot.cart.model;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.CartEntryDTO;
import com.boot.cart.dto.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.Serializable;
import java.util.*;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class CartEntryMapper implements Serializable {

     @Autowired
     ProductServiceClient productServiceClient;

    public static List<CartEntryDTO> cartEntityToDtoList(Cart cart, List<ProductDTO> productsInCart) {

        List<CartEntryDTO> cartEntryDTOList = new ArrayList<>();

        Map<String, CartEntry> cartEntries = new HashMap<>();

        for (CartEntry cartEntry : cart.getEntries()) {

            cartEntries.put(cartEntry.getProductName(), cartEntry);
        }

        for (ProductDTO product : productsInCart) {
            if (cartEntries.containsKey(product.getName())) {
                cartEntryDTOList.add(new CartEntryDTO()
                        .setId(cartEntries.get(product.getName()).getId())
                        .setProductName(product.getName())
                        .setDescription(product.getDescription())
                        .setPrice(product.getPrice())
                        .setPhotoLinks(product.getPhotoLinks())
                        .setStock(product.getStock())
                        .setQuantity(cartEntries.get(product.getName()).getQuantity()));
            }
        }
        return cartEntryDTOList;
    }
}
