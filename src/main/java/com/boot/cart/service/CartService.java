package com.boot.cart.service;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.*;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.model.Cart;
import com.boot.cart.model.CartEntry;
import com.boot.cart.repository.CartRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.boot.cart.model.Cart.cartEntityToDto;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CartService {
    private CartRepository cartRepository;
    private ProductServiceClient productServiceClient;

    public CartDTO addProductToCart(long userId, String productName, int quantity){
        ProductInfoDTO productInfo = productServiceClient.getProductInfo(productName);
        if (quantity > productInfo.getQuantity()) {
            throw new InvalidInputDataException("Insufficient stocks");
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(null);

        List<CartEntry> cartEntries;
        double productTotal = 0;

        if (cart == null) {
            cart = new Cart();
            cartEntries = new ArrayList<>();
            cart.setEntries(cartEntries);
            CartEntry cartEntry = new CartEntry();
            cartEntry.setProductName(productName);
            cartEntry.setPrice(productInfo.getPrice());
            cartEntry.setQuantity(quantity);
            cartEntry.setCart(cart);

            cartEntries.add(cartEntry);
        } else {
            cartEntries = cart.getEntries();
            CartEntry cartEntry = cartEntries.stream().filter(entry -> productName.equals(entry.getProductName())).findFirst().orElse(null);

            if (cartEntry != null) {
                cartEntry.setPrice(productInfo.getPrice());
                cartEntry.setQuantity(cartEntry.getQuantity() + quantity);
                cartEntry.setCart(cart);
            } else {
                CartEntry newCartEntry = new CartEntry();
                newCartEntry.setProductName(productName);
                newCartEntry.setPrice(productInfo.getPrice());
                newCartEntry.setQuantity(quantity);
                newCartEntry.setCart(cart);
                cartEntries.add(newCartEntry);
            }
        }

        productTotal += quantity * productInfo.getPrice();

        cart.setTotal(cart.getTotal() + productTotal);
        cart.setUserId(userId);

        cartRepository.save(cart);
        log.info("Added product:{} quantity:{} for userId:{}", productName, quantity, userId);
        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    public CartDTO updateCart(long userId, List<CartItemDTO> cartItems) {
        Map<String, ProductInfoDTO> productInfoMap = getProductsInfo(cartItems);
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);

        double cartTotal = 0;
        if (optionalCart.isEmpty()) {
            optionalCart = Optional.of(new Cart());
            optionalCart.get().setUserId(userId);
            optionalCart.get().setEntries(new ArrayList<>());
        } else {
            cartTotal = optionalCart.get().getTotal();
        }

        for (CartItemDTO item : cartItems) {
            ProductInfoDTO productInfo = productInfoMap.get(item.getName());
            if (item.getQuantity() > productInfo.getQuantity()) {
                throw new InvalidInputDataException("Insufficient stocks");
            }

            Optional<CartEntry> cartEntry = optionalCart.get().getEntries().stream().filter(entry -> item.getName().equals(entry.getProductName())).findFirst();
            if (cartEntry.isEmpty()) {
                cartEntry = Optional.of(new CartEntry());
                cartEntry.get().setProductName(productInfo.getName());
                cartEntry.get().setPrice(productInfo.getPrice());
                cartEntry.get().setQuantity(item.getQuantity());
                cartEntry.get().setCart(optionalCart.get());
            } else {
                cartTotal -= cartEntry.get().getQuantity() * cartEntry.get().getPrice();

                cartEntry.get().setQuantity(item.getQuantity());
                cartEntry.get().setPrice(productInfo.getPrice());
            }
            optionalCart.get().getEntries().add(cartEntry.get());
            cartTotal += cartEntry.get().getQuantity() * productInfo.getPrice();
        }
        optionalCart.get().setTotal(cartTotal);

        cartRepository.save(optionalCart.get());
        log.info("Updated userId:{} with {}", userId, cartItems.stream()
                .map(item -> String.format("Product:%s Quantity:%s", item.getName(), item.getQuantity()))
                .collect(Collectors.joining(",")));

        return cartEntityToDto(optionalCart.get(), getProductDTOS(optionalCart.get()));
    }

    public CartDTO removeProductFromCart(long userId, String productName)
            throws EntityNotFoundException {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found in the Database!"));

        List<CartEntry> matchingEntries = cart.getEntries().stream()
                .filter(entry -> productName.equals(entry.getProductName()))
                .collect(Collectors.toList());

        if(!matchingEntries.isEmpty()){
            matchingEntries.forEach(item-> cart.setTotal(cart.getTotal()-item.getPrice()*item.getQuantity()));
            cart.getEntries().removeAll(matchingEntries);

            cartRepository.save(cart);
        }
        log.info("Removed product:{} for userId:{}", productName, userId);

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
        cartRepository.delete(cart);
        log.info("Cart for user:{} successfully deleted!", userId);
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

    private Map<String, ProductInfoDTO> getProductsInfo(List<CartItemDTO> cartItems){
        return Arrays.stream(productServiceClient.getProductsInfo(cartItems.stream().map(item->item.getName()).collect(Collectors.toList())))
                .collect(Collectors.toMap(ProductInfoDTO::getName, item->item));
    }
}
