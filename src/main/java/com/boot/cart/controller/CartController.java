
package com.boot.cart.controller;

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
import java.util.List;


@Controller
@RequestMapping("/")
@AllArgsConstructor
public class CartController {

    private CartService cartService;

    private static final String USER_ID_HEADER = "User-Id";

    @PostMapping
    public ResponseEntity<CartDTO> addProductToCart(@Validated @RequestBody CartItemDTO cartItem,
                                                    @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.addProductToCart(userId, cartItem.getName(), cartItem.getQuantity());
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<CartDTO> updateCart(@RequestBody List<CartItemDTO> products,
                                              @RequestHeader(value = USER_ID_HEADER) long userId)
            throws InvalidInputDataException, EntityNotFoundException{
        CartDTO newCart = cartService.updateCart(userId, products);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @DeleteMapping("/{productName}")
    public ResponseEntity<CartDTO> removeProductFromCart(@Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                         @RequestHeader(value = USER_ID_HEADER) long userId)
            throws EntityNotFoundException {
        CartDTO newCart = cartService.removeProductFromCart(userId, productName);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<CartDTO> deleteCartByUserId(@RequestHeader(value = USER_ID_HEADER) long userId)
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
