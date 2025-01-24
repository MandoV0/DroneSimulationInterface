package services;

import core.DroneBase;
import core.DynamicDrone;
import core.parser.DynamicDroneParser;
import core.parser.JsonDroneParser;
import customException.APIFetchException;
import customException.InvalidURIException;
import customException.ParsingException;
import customException.TokenLoadException;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.util.logging.*;

/**
 * The DroneSimulationInterfaceAPI class provides an interface for interacting with a dronesim API.
 * This class is implemented as a Singleton to ensure a single instance is used across the application, as it is stateless.
 * It supports fetching all different drone types (Drone, DynamicDrone, DroneType) data from the API using parsers and configurable parameters.
 */
public final class DroneSimulationInterfaceAPI {
    private static final Logger log = Logger.getLogger(DroneSimulationInterfaceAPI.class.getName());
    private final String BASEURL = "http://dronesim.facets-labs.com/api/";
    private String TOKEN = "b2d431185fd5a8670e99e3efdcb2afe193083931";

    private final HttpClient httpClient;
    private final int SECONDS_TILL_TIMEOUT = 300;

    private static DroneSimulationInterfaceAPI instance;

    /**
     * DroneSimulationInterfaceAPI is a Singleton as it is stateless.
     * No need to create multiple Instances for each use.
     * @return Instance of the DroneSimulationInterfaceAPI
     */
    public static DroneSimulationInterfaceAPI getInstance() {
        if (instance == null) {
            instance = new DroneSimulationInterfaceAPI();
        }
        return instance;
    }

    private  <T extends DroneBase> DroneSimulationInterfaceAPI() {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(SECONDS_TILL_TIMEOUT))
                .build();
    }

    private JSONObject fetchDataFromEndpoint(String endpointUrl, int limit, int offset) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(createURI(endpointUrl, limit, offset))
                .header("Authorization", "Token " + TOKEN)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            log.log(Level.SEVERE, "Error fetching data from endpoint " + endpointUrl + "Limit and Offset: " + limit + " " + offset);
            throw new APIFetchException("API Fetch failed with status code: " + response.statusCode());
        }

        log.log(Level.INFO, "Fetched data from endpoint " + endpointUrl + ": " + response);
        return new JSONObject(response.body());
    }

    /**
     * Fetches drones from the API using the given JsonDroneParser {@code parser}.
     * The parser determines the type of drone and validates the API response.
     *
     * @param parser The JsonDroneParser to parse the API response into drone classes
     * @param limit The maximum number of results to fetch from the API
     * @param offset The starting position of results for pagination
     * @param <T> A type that extends DroneBase
     * @return A map of drone IDs to their respective parsed drone class
     * @throws IOException If an I/O error occurs during the API call
     * @throws InterruptedException If the API call is interrupted
     */
    public <T extends DroneBase> Map<Integer, T> fetchDrones(JsonDroneParser<T> parser, int limit, int offset) throws IOException, InterruptedException {
        JSONObject jsonObject = fetchDataFromEndpoint(parser.getEndpoint(), limit, offset);
        JSONArray jsonFile = jsonObject.getJSONArray("results");

        Map<Integer, T> data = new HashMap<Integer, T>();

        for (int i = 0; i < jsonFile.length(); i++) {
            JSONObject o = jsonFile.getJSONObject(i);
          try{
              if (parser.isValid(o)) {
                  T x = parser.parse(o);
                  data.put(x.getId(), x);
              }

            } catch (Exception e) {
            log.log(Level.SEVERE, "Error parsing dynamic data for  drone at index:"+ i);
            throw new ParsingException("Error parsing dynamic data for drone at index:"+ i, e);}

        }
        return data;

    }

    /**
     * Fetches the dynamic drone data for a specific drone {@code id} from the API.
     * This method retrieves data using the drone ID {@code id} and parses it into DynamicDrone class.
     *
     * @param id The ID of the drone to fetch dynamic data for
     * @param limit The maximum number of results to fetch from the API
     * @param offset The starting position of results for pagination
     * @return A list of DynamicDrone objects containing the parsed dynamic data
     * @throws IOException If an I/O error occurs during the API call
     * @throws InterruptedException If the API call is interrupted
     */
    public ArrayList<DynamicDrone> fetchDrones(int id, int limit, int offset) throws IOException, InterruptedException {
        JSONObject jsonObject = fetchDataFromEndpoint(id + "/dynamics", limit, offset);
        JSONArray jsonFile = jsonObject.getJSONArray("results");

        ArrayList<DynamicDrone> data = new ArrayList<>();
        DynamicDroneParser parser = new DynamicDroneParser();

        for (int i = 0; i < jsonFile.length(); i++) {
            JSONObject o = jsonFile.getJSONObject(i);
            try{
                if (parser.isValid(o)) {
                data.add(parser.parse(o));
                }
            } catch (Exception e) {
                log.log(Level.SEVERE, "Error parsing dynamic data for drone ID: " + id);
                throw new ParsingException("Error parsing dynamic data for drone ID: " + id +" drone at index:"+ i, e);
            }
        }

        return data;
    }

    private URI createURI(String endpointUrl, int limit, int offset) {
        try {
            return new URI(BASEURL + endpointUrl + "/?format=json&limit=" + limit + "&offset=" + offset);
        }
        catch (URISyntaxException e) {
            throw new InvalidURIException("Error while constructing URI." + e.getMessage());
        }
    }

    /**
     * Loads the API token from a configuration file.
     * This method reads the token from a "config.properties" file and sets it for authentication.
     * Note: This method is currently not needed as the token is already hardcoded.
     */
    private void loadToken() {
        try (FileInputStream inputStream = new FileInputStream("config.properties") ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            TOKEN = properties.getProperty("API_TOKEN");
        } catch (IOException e) {
            throw new TokenLoadException("Failed to load configuration files");
        }
    }
}