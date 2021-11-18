package me.june.domain;

public class InvariantException extends RuntimeException {
    public InvariantException(String message) {
        super(message);
    }

    private static final long serialVersionUID = 1L;
}