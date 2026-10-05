package com.sujal.finalproject.exception;

public class InvalidArchiveException extends RuntimeException {

    public InvalidArchiveException(String message) {
        super(message);
    }

    public InvalidArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
}