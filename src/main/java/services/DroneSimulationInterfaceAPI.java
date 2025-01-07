package services;

import core.DroneBase;
import core.parser.JsonDroneParser;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import java.util.logging.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DroneSimulationInterfaceAPI {
    private static final Logger log = Logger.getLogger(DroneSimulationInterfaceAPI.class.getName());
    private final String BASEURL = "http://dronesim.facets-labs.com/api/";
    private String TOKEN = "b2d431185fd5a8670e99e3efdcb2afe193083931";

    private final HttpClient httpClient;
    private final int SECONDS_TILL_TIMEOUT = 300;

    private ScheduledExecutorService scheduler;

    public <T extends DroneBase> DroneSimulationInterfaceAPI(JsonDroneParser<T> parser, int limit, int offset) {
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(SECONDS_TILL_TIMEOUT))
                .build();

        startAutoUpdate(parser, limit, offset);
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
            throw new IOException("API Fetch failed with status code: " + response.statusCode());
        }

        log.log(Level.INFO, "Fetched data from endpoint " + endpointUrl + ": " + response);
        return new JSONObject(response.body());
    }

    public <T extends DroneBase> Map<Integer, T> fetchDrones(JsonDroneParser<T> parser, int limit, int offset) throws IOException, InterruptedException {
        JSONObject jsonObject = fetchDataFromEndpoint(parser.getEndpoint(), limit, offset);
        JSONArray jsonFile = jsonObject.getJSONArray("results");

        Map<Integer, T> data = new HashMap<Integer, T>();

        for (int i = 0; i < jsonFile.length(); i++) {
            JSONObject o = jsonFile.getJSONObject(i);
            if (parser.isValid(o)) {
                T x = parser.parse(o);
                data.put(x.getId(), x);
            }
        }
        return data;
    }

    private URI createURI(String endpointUrl, int limit, int offset) {
        try {
            return new URI(BASEURL + endpointUrl + "/?format=json&limit=" + limit + "&offset=" + offset);
        }
        catch (URISyntaxException e) {
            throw new RuntimeException("Error while constructing URI." + e.getMessage());
        }
    }
    private boolean isSchedulerRunning = false;

    private <T extends DroneBase> void startAutoUpdate(JsonDroneParser<T> parser, int limit, int offset){
        if (isSchedulerRunning) {
            log.log(Level.WARNING, "Scheduler is already running");
            return;
        }


        scheduler = Executors.newSingleThreadScheduledExecutor();
        isSchedulerRunning = true;

        Runnable updateTask = () -> {
            try {
                log.log(Level.INFO, "Data Update Start");
                Map<Integer, ?> data = fetchDrones(parser,limit,offset);
                log.log(Level.INFO, "Data Update Finish");
            } catch (IOException | InterruptedException e){
                log.log(Level.SEVERE, "Error while fetching data from endpoint " + parser.getEndpoint(), e);
            }
        };
        scheduler.scheduleAtFixedRate(updateTask, 0, 120, TimeUnit.SECONDS);
    }

    public void stopAutoUpdate() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            log.log(Level.INFO, "Auto Update Stopped");

        }
    }

    /**
    Not needed currently as TOKEN is useless without connection to the VPN, so no need to hide it.
    */
    private void loadToken() {
        try (FileInputStream inputStream = new FileInputStream("config.properties") ) {
            Properties properties = new Properties();
            properties.load(inputStream);
            TOKEN = properties.getProperty("API_TOKEN");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration files");
        }
    }
}
