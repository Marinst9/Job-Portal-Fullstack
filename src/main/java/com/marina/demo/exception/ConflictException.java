package com.marina.demo.exception;

/** Барањето е во конфликт со постоечка состојба (пр. двојна апликација) → 409. */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
