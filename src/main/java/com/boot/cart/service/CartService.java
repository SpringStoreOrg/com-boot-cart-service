package com.boot.cart.service;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.dto.*;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.model.Cart;
import com.boot.cart.model.CartEntry;
import com.boot.cart.repository.CartRepository;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class CartService {
    private CartRepository cartRepository;
    private ProductServiceClient productServiceClient;

    public CartItemResponse addProductToCart(long userId, String productSlug, int quantity){
        log.info("Add product:{} quantity:{} for userId:{} started", productSlug, quantity, userId);
        ProductInfoDTO productInfo = productServiceClient.getProductInfoByProductName(productSlug);
        int notInStock = 0;
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);

        List<CartEntry> cartEntries = null;
        double productTotal = 0;
        Integer updatedQuantity = quantity;
        Cart cart = null;
        if (optionalCart.isEmpty()) {
            cart = new Cart();
            cartEntries = new ArrayList<>();
            cart.setEntries(cartEntries);

            cartEntries.add(getNewCartEntry(cart, productSlug, productInfo.getPrice(), quantity));
        } else {
            cart = optionalCart.get();
            productTotal = cart.getTotal();
            cartEntries = cart.getEntries();
            Optional<CartEntry> optionalCartEntry = cartEntries.stream()
                    .filter(entry -> productSlug.equals(entry.getProductName()))
                    .findFirst();
            if (optionalCartEntry.isPresent()) {
                optionalCartEntry.get().setPrice(productInfo.getPrice());
                optionalCartEntry.get().setQuantity(optionalCartEntry.get().getQuantity() + quantity);
                updatedQuantity = optionalCartEntry.get().getQuantity();
            } else {
                cartEntries.add(getNewCartEntry(cart, productSlug, productInfo.getPrice(), quantity));
            }
        }

        if (updatedQuantity > productInfo.getQuantity()) {
            notInStock = updatedQuantity - productInfo.getQuantity();
        }

        productTotal += quantity * productInfo.getPrice();

        cart.setTotal(productTotal);
        cart.setUserId(userId);

        cartRepository.save(cart);
        log.info("Add product:{} quantity:{} for userId:{} finished", productSlug, quantity, userId);
        return new CartItemResponse(notInStock, updatedQuantity, productSlug, productTotal);
    }

    public CartItemResponse updateCartItem(long userId, String productSlug, int quantity) {
        log.info("Update product:{} quantity:{} for userId:{} started", productSlug, quantity, userId);
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if (optionalCart.isEmpty()) {
            throw new EntityNotFoundException("Cart not found in the Database!");
        }

        double productTotal = optionalCart.get().getTotal();
        Cart cart = optionalCart.get();

        List<CartEntry> cartEntries = cart.getEntries();
        Optional<CartEntry> optionalCartEntry = cartEntries.stream()
                .filter(entry -> productSlug.equals(entry.getProductName()))
                .findFirst();
        if (optionalCartEntry.isEmpty()) {
            throw new EntityNotFoundException("Cart entry not found in the Database!");
        }

        ProductInfoDTO productInfo = productServiceClient.getProductInfoByProductName(productSlug);
        int notInStock = 0;
        if (quantity > productInfo.getQuantity()) {
            notInStock = quantity - productInfo.getQuantity();
        }

        productTotal -= optionalCartEntry.get().getQuantity() * optionalCartEntry.get().getPrice();
        optionalCartEntry.get().setPrice(productInfo.getPrice());
        optionalCartEntry.get().setQuantity(quantity);

        productTotal += quantity * productInfo.getPrice();

        cart.setTotal(productTotal);

        cartRepository.save(cart);
        log.info("Update product:{} quantity:{} for userId:{} finished", productSlug, quantity, userId);
        return new CartItemResponse(notInStock, quantity, productSlug, productTotal);
    }

    public void updateCartBatch(long userId, List<CartItemDTO> cartItems) {
        String cartSlugs = cartItems.stream()
                .map(CartItemDTO::getSlug)
                .collect(Collectors.joining(","));
        log.info("Update batch products:{} for userId:{} started", cartSlugs, userId);
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        List<CartEntry> cartEntries = null;
        final Cart cart = optionalCart.isEmpty()?new Cart():optionalCart.get();
        if (optionalCart.isEmpty()) {
            cartEntries = new ArrayList<>();
            cart.setEntries(cartEntries);
            cart.setUserId(userId);
        }else{
            cartEntries = cart.getEntries();
        }

        final AtomicDouble productTotal = new AtomicDouble(cart.getTotal());

        List<ProductInfoDTO> productInfos = productServiceClient.getProductInfo(cartSlugs);
        Map<String, ProductInfoDTO> productInfoMap = productInfos.stream().collect(Collectors.toMap(ProductInfoDTO::getSlug, item->item));
        Map<String, CartEntry> cartEntriesMap = cartEntries.stream().collect(Collectors.toMap(CartEntry::getProductName, item->item));
        cartItems.forEach(item -> {
            ProductInfoDTO productInfo = productInfoMap.get(item.getSlug());
            CartEntry cartEntry = cartEntriesMap.get(item.getSlug());
            if (productInfo != null) {
                if (cartEntry != null) {
                    cartEntry.setPrice(productInfo.getPrice());
                    cartEntry.setQuantity(cartEntry.getQuantity() + item.getQuantity());
                } else {
                    cart.getEntries().add(getNewCartEntry(cart, item.getSlug(), productInfo.getPrice(), item.getQuantity()));
                }
                productTotal.getAndAdd(item.getQuantity() * productInfo.getPrice());
            }
        });

        cart.setTotal(productTotal.get());
        cartRepository.save(cart);
        log.info("Update batch products:{} for userId:{} finished", cartSlugs, userId);
    }

    public CartItemResponse removeProductFromCart(long userId, String productSlug)
            throws EntityNotFoundException {
        log.info("Remove product:{} for userId:{} started", productSlug, userId);
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found in the Database!"));

        List<CartEntry> matchingEntries = cart.getEntries().stream()
                .filter(entry -> productSlug.equals(entry.getProductName()))
                .collect(Collectors.toList());

        if(!matchingEntries.isEmpty()){
            matchingEntries.forEach(item-> cart.setTotal(cart.getTotal()-item.getPrice()*item.getQuantity()));
            cart.getEntries().removeAll(matchingEntries);

            cartRepository.save(cart);
        }

        log.info("Remove product:{} for userId:{} finished", productSlug, userId);
        return new CartItemResponse(0, 0, productSlug, cart.getTotal());
    }

    public void deleteCartByUserId(long userId) {
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if(!optionalCart.isEmpty()) {
            cartRepository.delete(optionalCart.get());
            log.info("Cart for user:{} successfully deleted!", userId);
        }
        log.info("No Cart found for user: {} !", userId);
    }

    public CartDTO getCartByUserId(long userId) {
        Optional<Cart> optionalCart = cartRepository.findByUserId(userId);
        if (optionalCart.isPresent()) {
            Map<String, ProductDTO> productsMap = new HashMap<>();
            getProductDTOS(optionalCart.get()).stream().forEach(item -> productsMap.put(item.getSlug(), item));
            List<CartEntryDTO> entries = optionalCart.get().getEntries().stream().map(entry -> {
                ProductDTO productDTO = productsMap.get(entry.getProductName());
                return new CartEntryDTO().setQuantity(entry.getQuantity())
                        .setSlug(productDTO.getSlug())
                        .setName(productDTO.getName())
                        .setPrice(productDTO.getPrice())
                        .setThumbnail(productDTO.getThumbnail());
            }).collect(Collectors.toList());
            return new CartDTO(optionalCart.get().getTotal(), entries);
        } else {
            Cart newCart = new Cart();
            newCart.setUserId(userId);
            newCart.setTotal(0);
            newCart.setEntries(new ArrayList<>());
            cartRepository.save(newCart);

            return new CartDTO(0, new ArrayList<>());
        }
    }

    private List<ProductDTO> getProductDTOS(Cart cart) {
        String productParam = cart.getEntries().stream()
                .map(CartEntry::getProductName)
                .collect(Collectors.joining(","));

        if (StringUtils.isNotBlank(productParam)) {
            return productServiceClient.callGetAllProductsFromUser(productParam, false, Integer.MAX_VALUE).getBody().getProducts();
        } else {
            return new ArrayList<>();
        }
    }

    private CartEntry getNewCartEntry(Cart cart, String productSlug, double price, Integer quantity){
        CartEntry newCartEntry = new CartEntry();
        newCartEntry.setProductName(productSlug);
        newCartEntry.setPrice(price);
        newCartEntry.setQuantity(quantity);
        newCartEntry.setCart(cart);

        return newCartEntry;
    }
}
