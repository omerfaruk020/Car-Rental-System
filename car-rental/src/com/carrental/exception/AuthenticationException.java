package com.carrental.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException() { super("Invalid email or password!"); }
    public AuthenticationException(String msg) { super(msg); }
}