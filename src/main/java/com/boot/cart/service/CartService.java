package com.boot.cart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.boot.cart.dto.CartDTO;
import com.boot.cart.dto.ProductDTO;
import com.boot.cart.dto.UserDTO;
import com.boot.cart.model.Cart;
import com.boot.cart.model.CartEntry;
import com.boot.cart.repository.CartEntryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
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
            for (CartEntry cartEntry : cartEntries) {
                if (cartEntry.getProductName().equals(productName)) {
                    cartEntry.setQuantity(cartEntry.getQuantity() + quantity);
                    cartEntry.setCart(cart);
                    cartEntryRepository.save(cartEntry);
                    break;
                }
            }
            List<String> productNames =
                    cartEntries.stream()
                            .map(CartEntry::getProductName)
                            .collect(Collectors.toList());

           if(!productNames.contains(productName)){
                CartEntry cartEntry1 = new CartEntry();
                cartEntry1.setProductName(productName);
                cartEntry1.setQuantity(quantity);
                cartEntry1.setCart(cart);
                cartEntryRepository.save(cartEntry1);
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

//    public CartDTO updateProductFromCart(String email, String productName, int quantity)
//            throws InvalidInputDataException, EntityNotFoundException {
//
//        ProductDTO product;
//        try {
//            product = productServiceClient.callGetProductByProductName(productName);
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
//        }
//
//        User user;
//        try {
//            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(email));
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new EntityNotFoundException("UserName: " + email + " not found in the Database!");
//        }
//        Cart cart = cartRepository.findByUser(user);
//        List<ProductDTO> productList = cart.getProductList();
//
//        Long productsInCart = productList.stream().filter(p -> p.getProductName().equals(productName)).count();
//
//
//
//        if (productsInCart == 0) {
//            throw new InvalidInputDataException(
//                    "You currently have " + productsInCart + " Products: " + productName + " in cart!");
//        }
//
//        double productTotalRemove = 0;
//
//        List<ProductDTO> products = productList.stream().filter(p -> p.getProductName().equals(productName)).collect(Collectors.toList());
//        productList.removeAll(products);
//
//        cart.setTotal(cart.getTotal() - (products.size() * product.getProductPrice()));
//
//        for (int i = 0; i < quantity; i++) {
//            productList.add(product);
//            productTotalRemove = product.getProductPrice() + productTotalRemove;
//        }
//        cart.setTotal(cart.getTotal() + productTotalRemove);
//
//        cart.setUser(user);
//
//        cart.setProductList(productList);
//
//        cartRepository.save(cart);
//
//        return CartMapper.cartEntityToDto(cart);
//
//
//    }
//
//    public CartDTO removeProductFromCart(String email, String productName, int quantity)
//            throws InvalidInputDataException, EntityNotFoundException {
//
//        ProductDTO product;
//        try {
//            product = productServiceClient.callGetProductByProductName(productName);
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
//        }
//
//        User user;
//        try {
//            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(email));
//        } catch (HttpClientErrorException.NotFound e) {
//            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
//        }
//        Cart cart = cartRepository.findByUser(user);
//        List<ProductDTO> productList = cart.getProductList();
//
//        Long productsInCart = productList.stream().filter(p -> p.getProductName().equals(productName)).count();
//
//        if (productsInCart == 0) {
//            throw new InvalidInputDataException(
//                    "You currently have " + productsInCart + " Products: " + productName + " in cart!");
//        }
//
//        if (Math.toIntExact(productsInCart) < quantity) {
//            throw new InvalidInputDataException("You cannot remove more than " + productsInCart + " " + productName
//                    + " Products from the shopping cart!");
//        } else {
//            double productTotal = 0;
//
//            for (int i = 0; i < quantity; i++) {
//                productList.remove(product);
//                productTotal += product.getProductPrice();
//            }
//
//            cart.setUser(user);
//
//            cart.setProductList(productList);
//            cart.setLastUpdatedOn(LocalDateTime.now());
//            cart.setTotal(cart.getTotal() - productTotal);
//
//            cartRepository.save(cart);
//
//            return CartMapper.cartEntityToDto(cart);
//        }
//    }
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
