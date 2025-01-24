package customException;

public class FlightDynamicDataException extends RuntimeException {
    public FlightDynamicDataException() {super();}

    public FlightDynamicDataException(String message) {
        super(message);
    }

    public FlightDynamicDataException(String message, Throwable reason) {
        super(message, reason);
    }
}