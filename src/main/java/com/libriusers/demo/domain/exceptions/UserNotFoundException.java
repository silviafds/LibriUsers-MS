package com.libriusers.demo.domain.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("Review com ID " + id + " não encontrado.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}
