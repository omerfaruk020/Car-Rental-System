package com.carrental.exception;

public class DuplicateLicensePlateException extends RuntimeException {
    public DuplicateLicensePlateException(String plate) { super("A vehicle with this license plate is already registered: " + plate); }
}