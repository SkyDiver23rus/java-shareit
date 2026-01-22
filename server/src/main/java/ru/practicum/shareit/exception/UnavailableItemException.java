package ru.practicum.shareit.server.exception;

public class UnavailableItemException extends RuntimeException {
    public UnavailableItemException(String message) {
        super(message);
    }
}