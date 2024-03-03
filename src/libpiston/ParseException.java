package libpiston;

public class ParseException extends Exception {
    public ParseException(String message, Throwable e) {
        super(message, e);
    }
}
