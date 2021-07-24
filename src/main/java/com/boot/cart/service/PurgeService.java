package com.boot.cart.service;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.cart.client.ProductServiceClient;
import com.boot.cart.repository.CartRepository;
import com.boot.services.mapper.ProductMapper;
import com.boot.services.model.Cart;
import com.boot.services.model.Product;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "spring.enable.scheduling")
public class PurgeService {

	@Autowired
	CartRepository cartRepository;
	
	@Autowired
	ProductServiceClient productServiceClient;

	@Scheduled(cron = "${cron.expression}", zone = "Europe/Paris")
	public void emptyProductsFromCart() {
		Set<Cart> cartSet = cartRepository.findByLastUpdatedOnBefore(LocalDateTime.now().minusHours(3));

		for (Cart cart : cartSet) {

			List<Product> prodList = cart.getProductList();

			Iterator<Product> iter = prodList.iterator();

			while (iter.hasNext()) {
				Product product = iter.next();
				iter.remove();
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

	}

}
