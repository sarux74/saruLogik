package de.sarux.logik.helper;

public class LogikException extends Throwable {
    public LogikException(String s) {
        super(s);
    }

    public LogikException(String s, Exception e) {
        super(s,e);
    }
}
