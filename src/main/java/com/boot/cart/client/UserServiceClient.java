package com.boot.cart.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;
import com.boot.services.dto.UserDTO;

@Component
public class UserServiceClient {

    @Autowired
    private RestTemplate userServiceRestTemplate;

    public UserDTO callGetUserByEmail(String email) {

        return userServiceRestTemplate.getForEntity(Constants.GET_USER_BY_EMAIL, UserDTO.class, email).getBody();
    }

}
