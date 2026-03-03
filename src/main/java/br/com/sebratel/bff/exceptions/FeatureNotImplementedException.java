package br.com.sebratel.bff.exceptions;

public class FeatureNotImplementedException extends RuntimeException {
    public FeatureNotImplementedException(String message) {
        super(message);
    }
}