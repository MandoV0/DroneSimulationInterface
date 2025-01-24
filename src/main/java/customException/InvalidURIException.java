package customException;


public class InvalidURIException extends RuntimeException {
    public InvalidURIException() {super();}
    public InvalidURIException(String message) {
        super(message);
    }

    public InvalidURIException(String message, Throwable reason) {
        super(message, reason);
    }
}