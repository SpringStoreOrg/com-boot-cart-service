package com.boot.cart.exception;

public class EntityNotFoundException extends RuntimeException {
    /**
     *
     */
    private static final long serialVersionUID = 7825297071944927383L;

    public EntityNotFoundException(String message) {
        super(message);
    }
}
