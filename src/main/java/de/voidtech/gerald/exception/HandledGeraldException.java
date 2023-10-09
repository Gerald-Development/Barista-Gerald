package main.java.de.voidtech.gerald.exception;

public class HandledGeraldException  extends RuntimeException {

    public HandledGeraldException(Exception e) {
        super(e);
    }

    public HandledGeraldException(String e) {
        super(e);
    }
}
