package core.parser;

import org.json.JSONObject;
import core.DroneType;

public class DroneTypeParser implements JsonDroneParser<DroneType> {
    @Override
    public DroneType parse(JSONObject obj) {
        return new DroneType(
                obj.getInt("id"),
                obj.getString("manufacturer"),
                obj.getString("typename"),
                obj.getInt("weight"),
                obj.getInt("max_speed"),
                obj.getInt("battery_capacity"),
                obj.getInt("control_range"),
                obj.getInt("max_carriage")
        );
    }

    @Override
    public boolean isValid(JSONObject obj) {
        return obj.has("id") && obj.has("manufacturer");
    }

    @Override
    public String getEndpoint() {
        return "dronetypes";
    }
}
