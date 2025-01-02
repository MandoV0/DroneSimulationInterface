package core.parser;

import core.Drone;

public class DroneParser implements JsonDroneParser<Drone> {
    @Override
    public Drone parse(org.json.JSONObject obj) {
        return new Drone(
                obj.getInt("id"),
                obj.getString("serialnumber"),
                obj.getString("carriage_type"),
                obj.getInt("carriage_weight"),
                obj.getString("dronetype"),
                obj.getString("created")
        );
    }

    @Override
    public boolean isValid(org.json.JSONObject obj) {
        return obj.has("carriage_type") && obj.has("carriage_weight");
    }

    @Override
    public String getEndpoint() {
        return "drones";
    }
}

