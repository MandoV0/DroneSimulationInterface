package core.parser;

import core.Drone;
import org.json.JSONObject;
import customException.ParsingException;

public class DroneParser implements JsonDroneParser<Drone> {
    @Override
    public Drone parse(JSONObject obj) {
        try{
            if(!isValid(obj)){
                throw new ParsingException("Invalid drone object");
            }
            return new Drone(
                    obj.getInt("id"),
                    obj.getString("serialnumber"),
                    obj.getString("carriage_type"),
                    obj.getInt("carriage_weight"),
                    obj.getString("dronetype"),
                    obj.getString("created")
            );
        }catch (ParsingException e){
            throw new ParsingException("Error parsing drone object/JSON", e);
        }
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

