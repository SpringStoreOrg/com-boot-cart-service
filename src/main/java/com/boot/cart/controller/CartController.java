
package com.boot.cart.controller;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.boot.cart.dto.CartDTO;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.util.Constants;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import com.boot.cart.service.CartService;

import java.io.IOException;


@Controller
@AllArgsConstructor
public class CartController {

    private CartService cartService;

    private static final String USER_ID_HEADER = "User-Id";

    @PostMapping("/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                    @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                    @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity,
                                                    @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.addProductToCart(userId, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @PutMapping("/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> updateProductToCart(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                       @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                       @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity,
                                                       @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.updateProductFromCart(userId, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @PutMapping("/{email}")
    public ResponseEntity<CartDTO> updateProductToCartOnLogin(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                              @Size(min = 2, message = "Min Product Name size is 2!") @RequestBody String products,
                                                              @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException, IOException {
        CartDTO newCart = cartService.updateProductToCartOnLogin(userId, products);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @DeleteMapping("/{email}/{productName}")
    public ResponseEntity<CartDTO> removeProductFromCart(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                         @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                         @RequestHeader(value = USER_ID_HEADER) long userId)
            throws EntityNotFoundException {
        CartDTO newCart = cartService.removeProductFromCart(userId, productName);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<CartDTO> deleteCartByUserId(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @RequestParam("email") String email,
                                                     @RequestHeader(value = USER_ID_HEADER) long userId)
            throws EntityNotFoundException {
        cartService.deleteCartByUserId(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{email}")
    @ResponseBody
    public ResponseEntity<CartDTO> getCartByUserId(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                  @RequestHeader(value = USER_ID_HEADER) long userId){
        CartDTO newCart = cartService.getCartByUserId(userId);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }
}
