package com.boot.cart.model;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.CartEntryDTO;
import com.boot.cart.dto.ProductDTO;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.*;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@AllArgsConstructor
public class CartEntryMapper implements Serializable {

     @Autowired
     ProductServiceClient productServiceClient;

    public static List<CartEntryDTO> cartEntityToDtoList(Cart cart, List<ProductDTO> productsInCart) {

        List<CartEntryDTO> cartEntryDTOList = new ArrayList<>();



        for (CartEntry cartEntry:cart.getEntries()) {
            for (ProductDTO product:productsInCart) {
                 if (cartEntry.getProductName().equals(product.getName())){
                     cartEntryDTOList.add(new CartEntryDTO()
                             .setId(cartEntry.getId())
                             .setProductName(product.getName())
                             .setDescription(product.getDescription())
                             .setPrice(product.getPrice())
                             .setPhotoLink(product.getPhotoLink())
                             .setQuantity(cartEntry.getQuantity()));
                 }
            }
        }
        return cartEntryDTOList;
    }
}
