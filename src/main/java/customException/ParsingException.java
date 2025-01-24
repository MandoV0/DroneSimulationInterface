package customException;

public class ParsingException extends RuntimeException {
    public ParsingException(){super();};
    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(String message, Throwable reason) {
        super(message, reason);
    }
}