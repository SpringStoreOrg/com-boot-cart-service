
package com.boot.cart.controller;

import java.util.Set;

import javax.validation.constraints.Email;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.boot.cart.service.CartService;
import com.boot.services.dto.CartDTO;

@Controller
@RequestMapping("/")
public class CartController {

    @Autowired
    private CartService cartService;

    @PutMapping("/addProductToCart/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@Email(message = "Invalid email!", regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$") @PathVariable("email") String email,
                                                    @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                    @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity)
            throws InvalidInputDataException, EntityNotFoundException {

        CartDTO newCart = cartService.addProductToCart(email, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @PutMapping("/updateProductToCart/{email}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> updateProductToCart(@Email(message = "Invalid email!", regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$") @PathVariable("email") String email,
                                                       @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                       @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.updateProductFromCart(email, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.CREATED);
    }

    @PutMapping("/removeProductFromCart/{userName}/{productName}/{quantity}")
    public ResponseEntity<CartDTO> removeProductfromCart(@Size(min = 2, message = "Min User Name size is 2!") @PathVariable("userName") String userName,
                                                         @Size(min = 2, message = "Min Product Name size is 2!") @PathVariable("productName") String productName,
                                                         @Positive(message = "Quantity should be positive number") @PathVariable("quantity") int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        CartDTO newCart = cartService.removeProductFromCart(userName, productName, quantity);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @DeleteMapping("/deleteCartByUserName/{userName}")
    public ResponseEntity<CartDTO> deleteUserByUserName(@Size(min = 2, message = "Min User Name size is 2!") @PathVariable("userName") String userName)
            throws EntityNotFoundException, InvalidInputDataException {
        cartService.deleteCartByUserName(userName);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/getCartByEmail")
    @ResponseBody
    public ResponseEntity<CartDTO> getCartByEmail(@Email(message = "Invalid email!", regexp = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$") @RequestParam String email) throws EntityNotFoundException {
        CartDTO newCart = cartService.getCartByEmail(email);
        return new ResponseEntity<>(newCart, HttpStatus.OK);
    }

    @GetMapping("/getAllCarts")
    @ResponseBody
    public ResponseEntity<Set<CartDTO>> getAllCarts() throws EntityNotFoundException {
        Set<CartDTO> cartList = cartService.getAllCarts();
        return new ResponseEntity<>(cartList, HttpStatus.OK);
    }

    @GetMapping("/getNumberOfActiveCarts")
    @ResponseBody
    public ResponseEntity<Long> getNumberOfActiveCarts(){
        Long activeCarts = cartService.getNumberOfActiveCarts();
        return new ResponseEntity<>(activeCarts, HttpStatus.OK);
    }

}
