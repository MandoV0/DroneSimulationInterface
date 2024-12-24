package src.main.core;

public class DynamicDrone extends DroneBase {
    private String drone;
    private String timestamp;
    private int speed;
    private int allign_roll;
    private int allign_pitch;
    private int allign_yaw;
    private double longitude;
    private double latitude;
    private int battery_status;
    private String last_seen;
    private int status;

    public DynamicDrone(String drone, String timestamp, int speed, int allign_roll, int allign_pitch, int allign_yaw, double longitude, double latitude, int battery_status, String last_seen, int status) {
        this.drone = drone;
        this.timestamp = timestamp;
        this.speed = speed;
        this.allign_roll = allign_roll;
        this.allign_pitch = allign_pitch;
        this.allign_yaw = allign_yaw;
        this.longitude = longitude;
        this.latitude = latitude;
        this.battery_status = battery_status;
        this.last_seen = last_seen;
        this.status = status;
    }

    public String getDrone() {
        return drone;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getSpeed() {
        return speed;
    }

    public int getAllign_roll() {
        return allign_roll;
    }

    public int getAllign_pitch() {
        return allign_pitch;
    }

    public int getAllign_yaw() {
        return allign_yaw;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getBattery_status() {
        return battery_status;
    }

    public String getLast_seen() {
        return last_seen;
    }

    public int getStatus() {
        return status;
    }
}
