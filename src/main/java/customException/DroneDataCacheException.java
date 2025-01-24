package customException;

public class DroneDataCacheException extends RuntimeException {
    public DroneDataCacheException() {super();}
    public DroneDataCacheException(String message) {
        super(message);
    }

    public DroneDataCacheException(String message, Throwable reason) {
        super(message, reason);
    }
}