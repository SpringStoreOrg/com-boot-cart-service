package com.boot.cart.util;

public class Constants {

    public static final String GET_USER_BY_EMAIL = "/?email={email}";

    public static final String GET_PRODUCT_BY_PRODUCT_NAME = "/{productName}";
    public static final String UPDATE_PRODUCT_BY_PRODUCT_NAME = "/{productName}";
    public static final String GET_ALL_PRODUCTS = "/products";
    public static final String GET_ALL_PRODUCTS_FOR_USER = "/?productNames={products}&includeInactive={includeInactive}";

    //Regular expression used for email validation
    public static final String EMAIL_REGEXP = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-z" + "A-Z]{2,7}$";

}
