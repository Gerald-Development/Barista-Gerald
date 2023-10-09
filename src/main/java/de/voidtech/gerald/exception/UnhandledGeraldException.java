package main.java.de.voidtech.gerald.exception;

public class UnhandledGeraldException extends RuntimeException {

    public UnhandledGeraldException(Exception e) {
        super(e);
    }

    public UnhandledGeraldException(String e) {
        super(e);
    }

}
