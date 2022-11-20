package de.android.werhatschonmal.exceptions;

public class GamerException extends FalseValuesException {
    private String message = "index is incorrect";

    public GamerException() {

    }

    public GamerException(String message) {
        super(message);
    }

    public String getMessage() {
        return message;
    }
}
