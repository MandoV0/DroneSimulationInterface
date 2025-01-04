package services;

public final class Helper {
    public static int extractID(String url) {
        int droneId = -1;
        String[] parts = url.split("/");
        droneId = Integer.parseInt(parts[parts.length - 2]);
        return droneId;
    }
}
