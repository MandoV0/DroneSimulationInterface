package core;

public class Drone extends DroneBase {
    private final String carriageType;
    private final int carriageWeight;
    private final String droneType;
    private final String created;

    public Drone(int id, String serialNumber, String carriageType, int carriageWeight, String droneType, String created) {
        this.id = id;
        this.serialNumber = serialNumber;
        this.carriageType = carriageType;
        this.carriageWeight = carriageWeight;
        this.droneType = droneType;
        this.created = created;
    }

    public String getCarriageType() { return carriageType; }
    public int getCarriageWeight() { return carriageWeight; }
    public String getDroneType() { return droneType; }
    public String getCreated() { return created; }
}