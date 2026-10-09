package br.com.sebratel.bff.exceptions;

public class InvalidMassiveRequestException extends RuntimeException {
    public InvalidMassiveRequestException(String message) {
        super(message);
    }
}
