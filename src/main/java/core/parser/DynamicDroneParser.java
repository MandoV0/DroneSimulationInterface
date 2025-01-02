package core.parser;

import org.json.JSONObject;
import core.DynamicDrone;

public class DynamicDroneParser implements JsonDroneParser<DynamicDrone> {
    @Override
    public DynamicDrone parse(JSONObject obj) {
        return new DynamicDrone(
                obj.getString("drone"),
                obj.getString("timestamp"),
                obj.getInt("speed"),
                obj.getDouble("align_roll"),
                obj.getDouble("align_pitch"),
                obj.getDouble("align_yaw"),
                obj.getDouble("longitude"),
                obj.getDouble("latitude"),
                obj.getInt("battery_status"),
                obj.getString("last_seen"),
                obj.getString("status")
        );
    }

    @Override
    public boolean isValid(JSONObject obj) {
        return obj.has("drone") && obj.has("timestamp");
    }

    @Override
    public String getEndpoint() {
        return "dronedynamics";
    }
}
