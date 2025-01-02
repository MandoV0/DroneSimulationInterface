package core.parser;

import org.json.JSONObject;

public interface JsonDroneParser<T> {
    T parse(JSONObject obj);
    boolean isValid(JSONObject obj);
    String getEndpoint();
}

