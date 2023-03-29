package com.boot.cart.service;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.*;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.model.Cart;
import com.boot.cart.model.CartEntry;
import com.boot.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.boot.cart.model.Cart.cartEntityToDto;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CartService {
    private CartRepository cartRepository;
    private ProductPriceService productPriceService;
    private ProductServiceClient productServiceClient;

    public CartDTO addProductToCart(long userId, String productName, int quantity){
        log.info("Add product:{} quantity:{} for userId:{}", productName, quantity, userId);
        long productPrice = productPriceService.getPrice(productName);
        productServiceClient.reserve(productName, quantity);

        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        List<CartEntry> cartEntries;
        double productTotal = 0;

        if (cart == null) {
            cart = new Cart();
            cartEntries = new ArrayList<>();
            cart.setEntries(cartEntries);
            CartEntry cartEntry = new CartEntry();
            cartEntry.setProductName(productName);
            cartEntry.setPrice(productPrice);
            cartEntry.setQuantity(quantity);
            cartEntry.setCart(cart);

            cartEntries.add(cartEntry);
        } else {
            cartEntries = cart.getEntries();
            CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

            if (cartEntry != null) {
                cartEntry.setPrice(productPrice);
                cartEntry.setQuantity(cartEntry.getQuantity() + quantity);
                cartEntry.setCart(cart);
            } else {
                CartEntry newCartEntry = new CartEntry();
                newCartEntry.setProductName(productName);
                newCartEntry.setPrice(productPrice);
                newCartEntry.setQuantity(quantity);
                newCartEntry.setCart(cart);
                cartEntries.add(newCartEntry);
            }
        }

        productTotal += quantity * productPrice;

        cart.setTotal(cart.getTotal() + productTotal);
        cart.setUserId(userId);

        cartRepository.save(cart);
        //on fail do reverse operation

        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    public CartDTO updateCart(long userId, List<CartItemDTO> cartItems) {
        log.info("Updating userId:{} with {} items", userId, cartItems.size());
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        Cart cart = null;
        if (optionalCart.isPresent()) {
            cart = optionalCart.get();
        } else {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setEntries(new ArrayList<>());
            newCart.setTotal(0);
            cart = cartRepository.save(newCart);
        }

        List<CartEntry> cartEntries = cart.getEntries();

        double cartTotal = 0;

        List<BatchUpdateDTO> batchUpdate = new ArrayList<>();
        for (CartItemDTO item : cartItems) {
            try {
                Optional<CartEntry> cartEntry = cartEntries.stream()
                        .filter(entry -> item.getName().equals(entry.getProductName()))
                        .findFirst();

                long productPrice = productPriceService.getPrice(item.getName());
                if (cartEntry.isEmpty()) {
                    CartEntry newCartEntry = new CartEntry();
                    newCartEntry.setProductName(item.getName());
                    newCartEntry.setPrice(productPrice);
                    newCartEntry.setQuantity(item.getQuantity());
                    newCartEntry.setCart(cart);
                    cartEntries.add(newCartEntry);
                    batchUpdate.add(new BatchUpdateDTO(item.getName(), item.getQuantity(), Operation.SUBTRACT));

                    cartTotal += newCartEntry.getQuantity() * productPrice;
                } else {
                    if (item.getQuantity().compareTo(cartEntry.get().getQuantity()) != 0) {
                        if (item.getQuantity().compareTo(cartEntry.get().getQuantity()) > 0) {
                            batchUpdate.add(new BatchUpdateDTO(item.getName(), item.getQuantity() - cartEntry.get().getQuantity(), Operation.SUBTRACT));
                        } else {
                            batchUpdate.add(new BatchUpdateDTO(item.getName(), cartEntry.get().getQuantity() - item.getQuantity(), Operation.ADD));
                        }

                        cartTotal -= cartEntry.get().getQuantity() * cartEntry.get().getPrice();

                        cartEntry.get().setQuantity(item.getQuantity());
                        cartEntry.get().setPrice(productPrice);

                        cartTotal += cartEntry.get().getQuantity() * productPrice;
                    }
                }

                cart.setTotal(cartTotal);
            } catch (HttpClientErrorException.NotFound e) {
                throw new EntityNotFoundException("Product: " + item.getName() + " not found in the Database!");
            }
        }

        if (!batchUpdate.isEmpty()) {
            productServiceClient.batchReserve(batchUpdate);
            cartRepository.save(cart);
        }

        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    public CartDTO removeProductFromCart(long userId, String productName)
            throws EntityNotFoundException {
        log.info("Removing product:{} for userId:{}", productName, userId);

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found in the Database!"));

        List<CartEntry> matchingEntries = cart.getEntries().stream()
                .filter(entry -> productName.equals(entry.getProductName()))
                .collect(Collectors.toList());

        if(!matchingEntries.isEmpty()){
            AtomicInteger quantity = new AtomicInteger();
            matchingEntries.forEach(item-> quantity.getAndAdd(item.getQuantity()));

            productServiceClient.reserveRelease(productName, quantity.get());

            matchingEntries.forEach(item-> cart.setTotal(cart.getTotal()-item.getPrice()*item.getQuantity()));
            cart.getEntries().removeAll(matchingEntries);

            cartRepository.save(cart);
        }


        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    private List<ProductDTO> getProductDTOS(Cart cart) {
        String productParam = cart.getEntries().stream()
                .map(CartEntry::getProductName)
                .collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(productParam)) {
            return productServiceClient.callGetAllProductsFromUser(productParam, false);
        } else {
            return new ArrayList<>();
        }
    }

    public void deleteCartByUserId(long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() ->
                new EntityNotFoundException("Cart not found in the Database!"));
        if (!cart.getEntries().isEmpty()) {
            List<BatchUpdateDTO> batchUpdate = cart.getEntries().stream()
                    .map(item->new BatchUpdateDTO(item.getProductName(), item.getQuantity(), Operation.ADD))
                    .collect(Collectors.toList());
            productServiceClient.batchReserveRelease(batchUpdate);
        }
        cartRepository.delete(cart);
        log.info("Cart successfully deleted!");
    }

    public CartDTO getCartByUserId(long userId){
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        Cart cart = null;
        if (optionalCart.isPresent()) {
            cart = optionalCart.get();
        } else {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setTotal(0);
            newCart.setEntries(new ArrayList<>());
            cart = cartRepository.save(newCart);
        }

        return cartEntityToDto(cart, getProductDTOS(cart));
    }
}
