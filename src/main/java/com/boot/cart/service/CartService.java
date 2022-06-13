package com.boot.cart.service;

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
import org.jetbrains.annotations.NotNull;
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

        Cart cart = cartRepository.findByUserId(user.getId()).orElse(null);

        List<CartEntry> cartEntries;
        double productTotal = 0;

        if (cart == null) {
            cart = new Cart();
            cartEntries = new ArrayList<>();
            CartEntry cartEntry = new CartEntry();
            cartEntry.setProductName(productName);
            cartEntry.setPrice(productDTO.getPrice());
            cartEntry.setQuantity(quantity);
            cartEntry.setCart(cart);

            cartEntries.add(cartEntry);
        } else {
            cartEntries = cart.getEntries();
            CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

            if (cartEntry != null) {
                cartEntry.setPrice(productDTO.getPrice());
                cartEntry.setQuantity(cartEntry.getQuantity() + quantity);
                cartEntry.setCart(cart);
            } else {
                CartEntry newCartEntry = new CartEntry();
                newCartEntry.setProductName(productName);
                newCartEntry.setPrice(productDTO.getPrice());
                newCartEntry.setQuantity(quantity);
                newCartEntry.setCart(cart);
                cartEntries.add(newCartEntry);
            }
        }

        for (int i = 0; i < quantity; i++) {
            productTotal += productDTO.getPrice();
        }

        cart.setEntries(cartEntries);
        cart.setTotal(cart.getTotal() + productTotal);
        cart.setUserId(user.getId());

        cartRepository.save(cart);

        return cartEntityToDto(cart, getProductDTOS(cart));
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
            throw new InvalidInputDataException("You can not add more than: " + productDTO.getStock() + " "
                    + productName + " Products to your shopping cart!");
        }
        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() ->
                new EntityNotFoundException("Cart not found in the Database!"));

        List<CartEntry> cartEntries;
        Integer initialQuantity = 0;

        cartEntries = cart.getEntries();
        CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

        if (cartEntry == null) {
            throw new EntityNotFoundException("CartEntry not found in the Database!");
        } else {
            initialQuantity = cartEntry.getQuantity();
            cartEntry.setQuantity(quantity);
        }

        double total = cart.getTotal() - (initialQuantity * productDTO.getPrice()) + (quantity * productDTO.getPrice());

        cart.setTotal(total);

        cartRepository.save(cart);

        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    public CartDTO removeProductFromCart(String email, String productName)
            throws  EntityNotFoundException {
        log.info("removeProductFromCart - process started");

        ProductDTO productDTO;
        try {
            productDTO = productServiceClient.callGetProductByProductName(productName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() ->
         new EntityNotFoundException("Cart not found in the Database!"));

        List<CartEntry> matchingEntries = cart.getEntries().stream().filter(entry -> productName.equals(entry.getProductName())).collect(Collectors.toList());
        cart.setTotal(cart.getTotal() - cart.getEntries().stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().get().getQuantity() * productDTO.getPrice());
        cart.getEntries().removeAll(matchingEntries);

        cartRepository.save(cart);

        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    private List<ProductDTO> getProductDTOS(@NotNull Cart cart) {
        String productParam = cart.getEntries().stream().map(CartEntry::getProductName).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(productParam)) {
          return productServiceClient.callGetAllProductsFromUser(productParam, false);
        } else {
            return new ArrayList<>();
        }
    }

    public void deleteCartByEmail(String email) throws EntityNotFoundException {
        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() ->
                new EntityNotFoundException("Cart not found in the Database!"));
            cartRepository.delete(cart);
            log.info("Cart successfully deleted!");

    }

    public CartDTO getCartByEmail(String email) throws EntityNotFoundException {

        UserDTO user;
        try {
            user = userServiceClient.callGetUserByEmail(email);

        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        Cart cart = cartRepository.findByUserId(user.getId()).orElseThrow(() ->
                new EntityNotFoundException("Cart not found in the Database!"));
        cartRepository.save(cart);

        return cartEntityToDto(cart, getProductDTOS(cart));
    }
}
