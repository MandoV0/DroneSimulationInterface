package customException;

public class TokenLoadException extends RuntimeException {
   public TokenLoadException() {super();}
    public TokenLoadException(String message) {
        super(message);
    }

    public TokenLoadException(String message, Throwable reason) {
        super(message, reason);
    }
}
