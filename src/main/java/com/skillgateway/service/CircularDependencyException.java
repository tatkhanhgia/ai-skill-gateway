package com.skillgateway.service;

public class CircularDependencyException extends RuntimeException {
    public CircularDependencyException(String message) {
        super(message);
    }
}
