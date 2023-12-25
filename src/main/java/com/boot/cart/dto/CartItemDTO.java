package com.boot.cart.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CartItemDTO {
    @Size(min = 2, message = "Min Product Name size is 2!")
    private String slug;
    @Positive(message = "Quantity should be positive number")
    private Integer quantity;
}
