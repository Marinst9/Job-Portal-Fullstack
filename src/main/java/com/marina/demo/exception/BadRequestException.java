package com.marina.demo.exception;

/** Невалиден влез од клиентот → 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
