package customException;


import java.io.IOException;

public class APIFetchException extends IOException {
    public APIFetchException() {super();}
    public APIFetchException(String message) {
        super(message);
    }

    public APIFetchException(String message, Throwable reason) {
        super(message, reason);
    }
}