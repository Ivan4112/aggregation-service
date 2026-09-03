package org.aggregation.service.service;

public class UsersNotFoundException extends RuntimeException {

    public UsersNotFoundException(String message) {
        super(message);
    }
}
