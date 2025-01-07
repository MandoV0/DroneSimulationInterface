package core;

public class DroneType extends DroneBase {
    private final String manufacturer;
    private final String typeName;
    private final int weight;
    private final int maxSpeed;
    private final int batteryCapacity;
    private final int controlRange;
    private final int maxCarriage;

    public DroneType(int id, String manufacturer, String typeName, int weight, int maxSpeed, int batteryCapacity, int controlRange, int maxCarriage) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.typeName = typeName;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.batteryCapacity = batteryCapacity;
        this.controlRange = controlRange;
        this.maxCarriage = maxCarriage;
    }

    public  int getId() { return id;}

    public String getManufacturer() {
        return manufacturer;
    }

    public String getTypeName() {
        return typeName;
    }

    public int getWeight() {
        return weight;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public int getBatteryCapacity() {
        return batteryCapacity;
    }

    public int getControlRange() {
        return controlRange;
    }

    public int getMaxCarriage() {
        return maxCarriage;
    }

}
