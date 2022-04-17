package com.boot.cart.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.client.UserServiceClient;
import com.boot.cart.exception.EntityNotFoundException;
import com.boot.cart.exception.InvalidInputDataException;
import com.boot.cart.repository.CartRepository;
import com.boot.services.dto.CartDTO;
import com.boot.services.mapper.CartMapper;
import com.boot.services.mapper.ProductMapper;
import com.boot.services.mapper.UserMapper;
import com.boot.services.model.Cart;
import com.boot.services.model.Product;
import com.boot.services.model.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CartService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductServiceClient productServiceClient;

    @Autowired
    UserServiceClient userServiceClient;

    public CartDTO addProductToCart(String email, String productName, int quantity)
            throws InvalidInputDataException, EntityNotFoundException {
        log.info("addProductToCart - process started");
        Product product;
        try {
            product = ProductMapper.DtoToProductEntity(productServiceClient.callGetProductByProductName(productName));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        if (product.getProductStock() == 0) {
            throw new InvalidInputDataException("We are sorry, but currently: " + productName + " is out of order!");
        }

        if (product.getProductStock() < quantity) {
            throw new InvalidInputDataException("You can not add more than: " + product.getProductStock() + " "
                    + productName + " Products to your shopping cart!");
        }
        User user;
        try {
            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(email));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }
        Cart cart = cartRepository.findByUser(user);

        List<Product> productList;
        double productTotal = 0;
        if (cart == null) {
            cart = new Cart();
            productList = new ArrayList<>();
        } else {
            productList = cart.getProductList();
        }

        cart.setUser(user);

        for (int i = 0; i < quantity; i++) {
            productList.add(product);

            productTotal += product.getProductPrice();
        }

        cart.setProductList(productList);
        cart.setTotal(cart.getTotal() + productTotal);

        productServiceClient.callUpdateProductByProductName(productName, ProductMapper.ProductEntityToDto(product));

        //TODO here you might have problems if saving cart changes fails. you should handle somehow the rollback on the stock.
        cartRepository.save(cart);

        return CartMapper.cartEntityToDto(cart);

    }

    public CartDTO updateProductFromCart(String userName, String productName, int quantity)
            throws InvalidInputDataException, EntityNotFoundException {

        Product product;
        try {
            product = ProductMapper.DtoToProductEntity(productServiceClient.callGetProductByProductName(productName));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        User user;
        try {
            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(userName));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
        }
        Cart cart = cartRepository.findByUser(user);
        List<Product> productList = cart.getProductList();

        Long productsInCart = productList.stream().filter(p -> p.getProductName().equals(productName)).count();



        if (productsInCart == 0) {
            throw new InvalidInputDataException(
                    "You currently have " + productsInCart + " Products: " + productName + " in cart!");
        }

        double productTotalRemove = 0;

        List<Product> products = productList.stream().filter(p -> p.getProductName().equals(productName)).collect(Collectors.toList());
        productList.removeAll(products);

        cart.setTotal(cart.getTotal() - (products.size() * product.getProductPrice()));
        //	product.setProductStock(product.getProductStock() + quantity);

        for (int i = 0; i < quantity; i++) {
            productList.add(product);
            productTotalRemove = product.getProductPrice() + productTotalRemove;
        }
        cart.setTotal(cart.getTotal() + productTotalRemove);
        //	product.setProductStock(product.getProductStock() - quantity);

        cart.setUser(user);

        cart.setProductList(productList);

        cartRepository.save(cart);
        productServiceClient.callUpdateProductByProductName(productName, ProductMapper.ProductEntityToDto(product));

        return CartMapper.cartEntityToDto(cart);


    }

    public CartDTO removeProductFromCart(String userName, String productName, int quantity)
            throws InvalidInputDataException, EntityNotFoundException {

        Product product;
        try {
            product = ProductMapper.DtoToProductEntity(productServiceClient.callGetProductByProductName(productName));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Product: " + productName + " not found in the Database!");
        }

        User user;
        try {
            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(userName));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
        }
        Cart cart = cartRepository.findByUser(user);
        List<Product> productList = cart.getProductList();

        Long productsInCart = productList.stream().filter(p -> p.getProductName().equals(productName)).count();

        if (productsInCart == 0) {
            throw new InvalidInputDataException(
                    "You currently have " + productsInCart + " Products: " + productName + " in cart!");
        }

        if (Math.toIntExact(productsInCart) < quantity) {
            throw new InvalidInputDataException("You cannot remove more than " + productsInCart + " " + productName
                    + " Products from the shopping cart!");
        } else {
            double productTotal = 0;

            for (int i = 0; i < quantity; i++) {
                productList.remove(product);
                productTotal += product.getProductPrice();
            }

            cart.setUser(user);

            cart.setProductList(productList);
            cart.setLastUpdatedOn(LocalDateTime.now());
            cart.setTotal(cart.getTotal() - productTotal);

            //product.setProductStock(product.getProductStock() + quantity);

            cartRepository.save(cart);
            productServiceClient.callUpdateProductByProductName(productName, ProductMapper.ProductEntityToDto(product));

            return CartMapper.cartEntityToDto(cart);
        }
    }

    public void deleteCartByUserName(String userName) throws InvalidInputDataException, EntityNotFoundException {

        User user;
        try {
            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(userName));
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("UserName: " + userName + " not found in the Database!");
        }

        //TODO you might benefit from hibernate caching but you might also do the same DB query twice here(once when you want to see it's not null and 2nd time when you use it)
        if (cartRepository.findByUser(user) != null) {
            Cart cart = cartRepository.findByUser(user);

            List<Product> prodList = cart.getProductList();

            Iterator<Product> iter = prodList.iterator();

            while (iter.hasNext()) {
                Product product = iter.next();
                iter.remove();
                //it is nicer to use parameterized logging instead of string concatenation 
                log.info(product.getProductName() + " - succesfully deleted from Product List");
                product.setProductStock(product.getProductStock() + 1);
                log.info(product.getProductName() + " Productstock succesfully updated! currently "
                        + product.getProductStock() + " products in stock!");

                productServiceClient.callUpdateProductByProductName(product.getProductName(),
                        ProductMapper.ProductEntityToDto(product));

            }
            cartRepository.delete(cart);
            log.info("Cart succesfully deleted!");
        }
        log.info("Cart is empty (null)!");
    }

    public CartDTO getCartByEmail(String email) throws EntityNotFoundException {

        User user;
        try {
            user = UserMapper.DtoToUserEntity(userServiceClient.callGetUserByEmail(email));

        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("Email: " + email + " not found in the Database!");
        }

        if (cartRepository.findByUser(user) != null) {
            Cart cart = cartRepository.findByUser(user);

            return CartMapper.cartEntityToDto(cart);
        } else
            return null;
    }

    public Set<CartDTO> getAllCarts() throws EntityNotFoundException {
        if (cartRepository.findAll().isEmpty()) {
            throw new EntityNotFoundException("No cart found in the Database!");
        }
        List<Cart> allCarts = cartRepository.findAll();
        return CartMapper.cartEntityToDtoList(allCarts);
    }

    public long getNumberOfActiveCarts() {
        return cartRepository.count();
    }

}
