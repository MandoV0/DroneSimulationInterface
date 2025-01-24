package customException;

public class LoadingException extends RuntimeException{
    public LoadingException(){super();}
    public LoadingException(String message) {
        super(message);
    }

    public LoadingException(String message, Throwable reason) {
        super(message, reason);
    }
}
