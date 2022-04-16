package com.boot.cart.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;
import com.boot.services.dto.UserDTO;

public class UserServiceClient {

    @Autowired
    private RestTemplate userServiceRestTemplate;

    public UserDTO callGetUserByEmail(String email) {

        //TODO you could use getForEntity with uriVariables instead of string concatenation
        return userServiceRestTemplate.getForEntity(Constants.GET_USER_BY_EMAIL + email, UserDTO.class).getBody();
    }

    public void callUpdateUser(String userName, UserDTO user) {
        userServiceRestTemplate.exchange(Constants.UPDATE_USER + userName, HttpMethod.PUT, new HttpEntity<>(user), String.class);
    }
}
