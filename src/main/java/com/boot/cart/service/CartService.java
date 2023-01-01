package com.boot.cart.service;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.CartItemDTO;
import com.boot.cart.dto.ProductDTO;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.model.Cart;
import com.boot.cart.model.CartEntry;
import com.boot.cart.repository.CartEntryRepository;
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
import java.util.stream.Collectors;

import static com.boot.cart.model.Cart.cartEntityToDto;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CartService {

    CartEntryRepository cartEntryRepository;

    CartRepository cartRepository;

    ProductServiceClient productServiceClient;


    public CartDTO addProductToCart(long userId, String productName, int quantity)
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

        Cart cart = cartRepository.findByUserId(userId).orElse(null);

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
        cart.setUserId(userId);

        cartRepository.save(cart);

        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    public CartDTO updateCart(long userId, List<CartItemDTO> cartItems)
            throws InvalidInputDataException, EntityNotFoundException{
        log.info("updateProductToCartOnLogin - process started");
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

        for (CartItemDTO item : cartItems) {
            try {
                ProductDTO productDTO = productServiceClient.callGetProductByProductName(item.getName());
                if (productDTO.getStock() <  item.getQuantity()) {
                    throw new InvalidInputDataException("We are sorry, but currently: " + item.getName() + " is out of order!");
                }

                CartEntry cartEntry = cartEntries.stream().filter(entry -> productDTO.getName().equals(entry.getProductName())).findFirst().orElse(null);

                if (cartEntry == null) {
                    CartEntry newCartEntry = new CartEntry();
                    newCartEntry.setProductName(productDTO.getName());
                    newCartEntry.setPrice(productDTO.getPrice());
                    newCartEntry.setQuantity(item.getQuantity());
                    newCartEntry.setCart(cart);
                    cartEntries.add(newCartEntry);

                    cartTotal += newCartEntry.getQuantity() * productDTO.getPrice();
                } else {
                    cartTotal-=cartEntry.getQuantity()*cartEntry.getPrice();

                    cartEntry.setQuantity(item.getQuantity());
                    cartEntry.setPrice(productDTO.getPrice());

                    cartTotal+=cartEntry.getQuantity() * productDTO.getPrice();
                }

                cart.setTotal(cartTotal);
            } catch (HttpClientErrorException.NotFound e) {
                throw new EntityNotFoundException("Product: " + item.getName() + " not found in the Database!");
            }
        }

        cartRepository.save(cart);
        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    public CartDTO removeProductFromCart(long userId, String productName)
            throws EntityNotFoundException {
        log.info("removeProductFromCart - process started");

        try {
            productServiceClient.callGetProductByProductName(productName);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() ->
                new EntityNotFoundException("Cart not found in the Database!"));

        List<CartEntry> matchingEntries = cart.getEntries().stream().filter(entry -> productName.equals(entry.getProductName())).collect(Collectors.toList());

        matchingEntries.stream().forEach(item-> cart.setTotal(cart.getTotal()- item.getPrice()*item.getQuantity()));

        cart.getEntries().removeAll(matchingEntries);

        cartRepository.save(cart);

        return cartEntityToDto(cart, getProductDTOS(cart));
    }

    private List<ProductDTO> getProductDTOS(Cart cart) {
        String productParam = cart.getEntries().stream().map(CartEntry::getProductName).collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(productParam)) {
            return productServiceClient.callGetAllProductsFromUser(productParam, false);
        } else {
            return new ArrayList<>();
        }
    }

    public void deleteCartByUserId(long userId) throws EntityNotFoundException {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() ->
                new EntityNotFoundException("Cart not found in the Database!"));
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
