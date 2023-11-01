
package com.boot.cart.controller;

import com.boot.cart.dto.CartItemResponse;
import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.CartItemDTO;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Size;


@Controller
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController {

    private CartService cartService;

    private static final String USER_ID_HEADER = "User-Id";

    @PostMapping
    public ResponseEntity<CartItemResponse> addProductToCart(@Validated @RequestBody CartItemDTO cartItem,
                                                             @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException {
        CartItemResponse response = cartService.addProductToCart(userId, cartItem.getName(), cartItem.getQuantity());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<CartItemResponse> updateCartItem(@RequestBody CartItemDTO cartItem,
                                                           @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException{
        CartItemResponse response = cartService.updateCartItem(userId, cartItem.getName(), cartItem.getQuantity());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{productSlug}")
    public ResponseEntity<CartItemResponse> removeProductFromCart(@Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productSlug") String productSlug,
                                                         @RequestHeader(value = USER_ID_HEADER) long userId)
            throws EntityNotFoundException {
        CartItemResponse itemResponse = cartService.removeProductFromCart(userId, productSlug);
        return new ResponseEntity<>(itemResponse, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity deleteCartByUserId(@RequestHeader(value = USER_ID_HEADER) long userId)
            throws EntityNotFoundException {
        cartService.deleteCartByUserId(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping
    @ResponseBody
    public ResponseEntity<CartDTO> getCartByUserId(@RequestHeader(value = USER_ID_HEADER) long userId){
        CartDTO newCart = cartService.getCartByUserId(userId);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }
}
