package customException;

public class DroneDataFetchException extends RuntimeException {
    public DroneDataFetchException() {super();}
    public DroneDataFetchException(String message) {
        super(message);
    }

    public DroneDataFetchException(String message, Throwable reason) {
        super(message, reason);
    }
}