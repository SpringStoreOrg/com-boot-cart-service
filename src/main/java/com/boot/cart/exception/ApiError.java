package com.boot.cart.exception;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ApiError {
    private int status;
    private String message;
    private String error;

    public ApiError(HttpStatus httpStatus, String message) {
        if (httpStatus != null) {
            this.status = httpStatus.value();
            this.error = httpStatus.getReasonPhrase();
        }
        this.message = message;
    }
}
