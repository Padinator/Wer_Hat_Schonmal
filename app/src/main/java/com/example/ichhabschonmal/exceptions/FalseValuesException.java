package com.example.ichhabschonmal.exceptions;

public class FalseValuesException extends Exception {
    private String message = "false values are given";

    public FalseValuesException() {

    }

    public FalseValuesException(String message) {
        super(message);
    }

    public String getMessage() {
        return message;
    }
}
