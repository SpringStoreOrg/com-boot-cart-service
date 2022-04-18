package com.boot.cart.util;

public class Constants {

    public static final String GET_USER_BY_EMAIL = "/getUserByEmail?email={email}";
    public static final String UPDATE_USER = "/updateUserByUserName/";

    public static final String GET_PRODUCT_BY_PRODUCT_NAME = "/getProductByProductName?productName={productName}";
    public static final String UPDATE_PRODUCT_BY_PRODUCT_NAME = "/updateProductByProductName/{productName}";
    public static final String GET_ALL_PRODUCTS = "/getAllProducts";

    //Regular expression used for email validation
    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";

}
