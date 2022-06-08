package com.boot.cart.client;

import com.boot.cart.dto.UserDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.cart.util.Constants;

@Component
@AllArgsConstructor
public class UserServiceClient {


    private RestTemplate userServiceRestTemplate;

    public UserDTO callGetUserByEmail(String email) {

        return userServiceRestTemplate.getForEntity(Constants.GET_USER_BY_EMAIL, UserDTO.class, email).getBody();
    }

}
