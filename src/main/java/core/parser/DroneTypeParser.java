package core.parser;

import org.json.JSONObject;
import core.DroneType;
import customException.ParsingException;

public class DroneTypeParser implements JsonDroneParser<DroneType> {
    @Override
    public DroneType parse(JSONObject obj) {
        try {
            if (!isValid(obj)) {
                throw new ParsingException("Invalid drone type object");
            }
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
        } catch (ParsingException e) {
            throw new ParsingException("Error parsing drone type object/JSON", e);
        }

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
