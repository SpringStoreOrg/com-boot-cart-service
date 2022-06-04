package com.boot.cart.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.ProductDTO;
import com.boot.cart.dto.UserDTO;
import com.boot.cart.model.Cart;
import com.boot.cart.model.CartEntry;
import com.boot.cart.repository.CartEntryRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.repository.CartRepository;


import lombok.extern.slf4j.Slf4j;

import static com.boot.cart.model.Cart.cartEntityToDto;
import static com.boot.cart.model.Cart.cartEntityToDtoList;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CartService {

    CartEntryRepository cartEntryRepository;

    CartRepository cartRepository;

    ProductServiceClient productServiceClient;

    UserServiceClient userServiceClient;


    public CartDTO addProductToCart(String email, String productName, int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        log.info("addProductToCart - process started");
        ProductDTO productDTO;
        try {
            productDTO = productServiceClient.callGetProductByProductName(productName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        if (productDTO.getStock() == 0) {
            throw new InvalidInputDataException("We are sorry, but currently: " + productName + " is out of order!");
        }

        if (productDTO.getStock() < quantity) {
            throw new InvalidInputDataException("You can not add more than: " + productDTO.getStock() + " "
                    + productName + " Products to your shopping cart!");
        }
        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        Cart cart = cartRepository.findByUserId(user.getId());

        List<CartEntry> cartEntries;
        double productTotal = 0;

        if (cart == null) {
            cart = new Cart();
            cartEntries = new ArrayList<>();
            CartEntry cartEntry = new CartEntry();
            cartEntry.setProductName(productName);
            cartEntry.setQuantity(quantity);
            cartEntry.setCart(cart);

            cartEntries.add(cartEntry);
        } else {
            cartEntries = cart.getEntries();
            CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

            if (cartEntry != null) {
                cartEntry.setQuantity(cartEntry.getQuantity() + quantity);
                cartEntry.setCart(cart);
            } else {
                CartEntry newCartEntry = new CartEntry();
                newCartEntry.setProductName(productName);
                newCartEntry.setQuantity(quantity);
                newCartEntry.setCart(cart);
                cartEntryRepository.save(newCartEntry);
            }
        }

        for (int i = 0; i < quantity; i++) {
            productTotal += productDTO.getPrice();
        }

        cart.setEntries(cartEntries);
        cart.setTotal(cart.getTotal() + productTotal);
        cart.setUserId(user.getId());

        cartRepository.save(cart);

        return cartEntityToDto(cartRepository.findByUserId(user.getId()));
    }

    public CartDTO updateProductFromCart(String email, String productName, int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        log.info("removeProductFromCart - process started");

        ProductDTO productDTO;
        try {
            productDTO = productServiceClient.callGetProductByProductName(productName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        if (productDTO.getStock() == 0) {
            throw new InvalidInputDataException("We are sorry, but currently: " + productName + " is out of order!");
        }

        if (productDTO.getStock() < quantity) {
            throw new InvalidInputDataException("You can not remove more than: " + productDTO.getStock() + " "
                    + productName + " Products from your shopping cart!");
        }
        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        Cart cart;
        if (cartRepository.findByUserId(user.getId()) != null) {
            cart = cartRepository.findByUserId(user.getId());
        } else {
            throw new EntityNotFoundException("Cart not found in the Database!");
        }

        List<CartEntry> cartEntries;
        Integer initialQuantity = 0;

        cartEntries = cart.getEntries();
        CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

        if (cartEntry == null) {
            throw new EntityNotFoundException("CartEntry not found in the Database!");
        } else {
            initialQuantity = cartEntry.getQuantity();
            cartEntry.setQuantity(quantity);
            cartEntry.setCart(cart);
        }

        cart.setEntries(cartEntries);

        double total = cart.getTotal() - (initialQuantity * productDTO.getPrice()) + (quantity * productDTO.getPrice());

        cart.setTotal(total);

        cartRepository.save(cart);

        return cartEntityToDto(cartRepository.findByUserId(user.getId()));
    }

    public CartDTO removeProductFromCart(String email, String productName)
            throws InvalidInputDataException, EntityNotFoundException {
        log.info("removeProductFromCart - process started");

        ProductDTO productDTO;
        try {
            productDTO = productServiceClient.callGetProductByProductName(productName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        if (productDTO.getStock() == 0) {
            throw new InvalidInputDataException("We are sorry, but currently: " + productName + " is out of order!");
        }

        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }
        Cart cart = cartRepository.findByUserId(user.getId());

        cartEntryRepository.delete(cart.getEntries().stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().get());

        cart.setEntries(cart.getEntries().stream().filter(entry -> !productName.equals(entry.getProductName())).collect(Collectors.toList()));
        cart.setTotal(cart.getTotal() - cart.getEntries().stream().filter(entry -> productName.equals(entry.getProductName())).count() * productDTO.getPrice());

        cartRepository.save(cart);

        return cartEntityToDto(cartRepository.findByUserId(user.getId()));
    }

    public void deleteCartByEmail(String email) throws EntityNotFoundException {
        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        if (cartRepository.findByUserId(user.getId()) != null) {
            Cart cart = cartRepository.findByUserId(user.getId());

            cartRepository.delete(cart);
            log.info("Cart successfully deleted!");
        } else {
            throw new EntityNotFoundException("Cart not found in the Database!");
        }
    }

    public CartDTO getCartByEmail(String email) throws EntityNotFoundException {

        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);

        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        if (cartRepository.findByUserId(user.getId()) != null) {
            Cart cart = cartRepository.findByUserId(user.getId());

            return cartEntityToDto(cart);
        } else {
            throw new EntityNotFoundException("Cart not found in the Database!");
        }
    }
}
