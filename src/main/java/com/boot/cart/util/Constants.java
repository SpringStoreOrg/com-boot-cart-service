package com.boot.cart.util;

public class Constants {

	//public static final String BASE_URL_USER = "http://localhost:8080/";
	public static final String BASE_URL_USER = "https://spring-store-user-service.herokuapp.com/";
	//TODO instead of hardcoding this url in here you could put them in application.properties file and inject them in the Config class where you create the RestTemplate using @Value("${user.service.url}"). Value annotation is from org.springframework.beans.factory.annotation. 
	public static final String GET_USER_BY_EMAIL = BASE_URL_USER + "getUserByEmail?email=";
	public static final String UPDATE_USER = BASE_URL_USER + "updateUserByUserName/";
	
	//public static final String BASE_URL_PRODUCT = "http://localhost:8081/";
	public static final String BASE_URL_PRODUCT = "https://spring-store-product-service.herokuapp.com/";
	public static final String GET_PRODUCT_BY_PRODUCT_NAME = BASE_URL_PRODUCT + "getProductByProductName?productName=";
	public static final String UPDATE_PRODUCT_BY_PRODUCT_NAME = BASE_URL_PRODUCT + "updateProductByProductName/";
	public static final String GET_ALL_PRODUCTS = BASE_URL_PRODUCT + "getAllProducts";
	

}
