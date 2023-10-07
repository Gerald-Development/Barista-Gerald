package main.java.de.voidtech.gerald.exception;

public class GeraldException extends RuntimeException {

    public GeraldException(Exception e) {
        super(e);
    }

    public GeraldException(String e) {
        super(e);
    }

}
