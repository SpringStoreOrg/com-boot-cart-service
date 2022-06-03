
package com.boot.cart.controller;

import java.util.Set;

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


@Controller
@AllArgsConstructor
public class CartController {

    private CartService cartService;
    
    @PostMapping("/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                    @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                    @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity)
            throws InvalidInputDataException, EntityNotFoundException {

        CartDTO newCart = cartService.addProductToCart(email, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @PutMapping("/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> updateProductToCart(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email,
                                                       @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                       @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.updateProductFromCart(email, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @DeleteMapping("/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> removeProductfromCart(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email")  String email,
                                                         @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                         @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.removeProductFromCart(email, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<CartDTO> deleteCartByEmail(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @RequestParam("email") String email)
            throws EntityNotFoundException {
        cartService.deleteCartByEmail(email);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{email}")
    @ResponseBody
    public ResponseEntity<CartDTO> getCartByEmail(@Email(message = "Invalid email!", regexp = Constants.EMAIL_REGEXP) @PathVariable("email") String email) throws EntityNotFoundException {
        CartDTO newCart = cartService.getCartByEmail(email);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }
}
