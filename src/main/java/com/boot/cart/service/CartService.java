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
public class CartService {

    CartEntryRepository cartEntryRepository;

    CartRepository cartRepository;

    ProductServiceClient productServiceClient;

    UserServiceClient userServiceClient;

    @Transactional
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
            }else {
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
        Cart cart = cartRepository.findByUserId(user.getId());

        List<CartEntry> cartEntries;
        long initialProductTotal = 0;
        Integer initialQuantity = 0;

        cartEntries = cart.getEntries();
        CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

        if (cartEntry == null) {
            throw new EntityNotFoundException("CartEntry not found in the Database!");
        }
        else{
            initialQuantity = cartEntry.getQuantity();
            cartEntry.setQuantity(0);
            cartEntry.setQuantity(quantity);
            cartEntry.setCart(cart);

            initialProductTotal = initialQuantity *  productDTO.getPrice();
        }

        cart.setEntries(cartEntries);

        cart.setTotal(cart.getTotal() - initialProductTotal);
        cart.setTotal(cart.getTotal()+(quantity*productDTO.getPrice()));

        cartRepository.save(cart);

        return cartEntityToDto(cartRepository.findByUserId(user.getId()));
    }

    public CartDTO removeProductFromCart(String email, String productName, int quantity)
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
        Cart cart = cartRepository.findByUserId(user.getId());

        List<CartEntry> cartEntries;
        double productTotal = 0;

            cartEntries = cart.getEntries();
            CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

            if (cartEntry == null) {
                throw new EntityNotFoundException("CartEntry not found in the Database!");
            }
            else{
                cartEntry.setQuantity(cartEntry.getQuantity() - quantity);
                cartEntry.setCart(cart);
            }

        for (int i = 0; i < quantity; i++) {
            productTotal -= productDTO.getPrice();
        }

        cart.setEntries(cartEntries);
        cart.setTotal(cart.getTotal() + productTotal);

        cartRepository.save(cart);

        return cartEntityToDto(cartRepository.findByUserId(user.getId()));
    }
//
//    public void deleteCartByEmail(String email) throws  EntityNotFoundException {
//
//        User user;
//        try {
//            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(email));
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
//        }
//
//        //TODO you might benefit from hibernate caching but you might also do the same DB query twice here(once when you want to see it's not null and 2nd time when you use it)
//        if (cartRepository.findByUser(user) != null) {
//            Cart cart = cartRepository.findByUser(user);
//
//            List<ProductDTO> prodList = cart.getProductList();
//
//            Iterator<ProductDTO> iter = prodList.iterator();
//
//            while (iter.hasNext()) {
//                ProductDTO product = iter.next();
//                iter.remove();
//
//                log.info("{} succesfully deleted from Product List",product.getProductName());
//
//            }
//            cartRepository.delete(cart);
//            log.info("Cart succesfully deleted!");
//        }
//        log.info("Cart is empty (null)!");
//    }
//
//    public CartDTO getCartByEmail(String email) throws EntityNotFoundException {
//
//        UserDTO user;
//        try {
//            UserDTO  user = userServiceClient.callGetUserByEmail(email);
//
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
//        }
//
//        if (cartRepository.findByUserId(user) != null) {
//            Cart cart = cartRepository.findByUserId(user);
//
//            return cartEntityToDto(cart);
//        } else
//            return null;
//    }

    public Set<CartDTO> getAllCarts() throws EntityNotFoundException {
        if (cartRepository.findAll().isEmpty()) {
            throw new EntityNotFoundException("No cart found in the Database!");
        }
        List<Cart> allCarts = cartRepository.findAll();
        return cartEntityToDtoList(allCarts);
    }

    public long getNumberOfActiveCarts() {
        return cartRepository.count();
    }

}
