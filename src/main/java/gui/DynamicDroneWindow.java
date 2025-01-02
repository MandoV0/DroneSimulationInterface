package gui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import core.Drone;
import core.DroneType;
import core.DynamicDrone;
import core.parser.DroneParser;
import core.parser.DroneTypeParser;
import core.parser.DynamicDroneParser;
import services.DroneSimulationInterfaceAPI;
import utils.Colors;
import utils.DynamicDroneFilter;


import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;

public class DynamicDroneWindow extends JPanel {
    private final JPanel innerContentPanel;
    private JLabel pageLabel;
    private int currentPage = 1;
    private final DroneSimulationInterfaceAPI api = new DroneSimulationInterfaceAPI();
    private Map<Integer, DroneType> droneTypeCache = new HashMap<>();
    private Map<Integer, Drone> droneCache = new HashMap<>();
    private DynamicDroneFilter filter;

    public DynamicDroneWindow() {
        setLayout(new BorderLayout());
        setBackground(Colors.EERIE_BLACK);

        innerContentPanel = new JPanel();
        innerContentPanel.setBackground(Colors.EERIE_BLACK);
        innerContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER)); // Or use GridLayout

        JScrollPane scrollPane = new JScrollPane(innerContentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        // TODO: Not sure when to apply filter?
        /*
            How does filter affect paging?
            What when filter gets reset while your on page x?

            Easiest way seems to just go back to page 1 when filter is applied
         */
        preWarm();
        updateDroneView();
        createPagePanel();

        add(scrollPane, BorderLayout.CENTER);
    }

    private void preWarm() {
        try {
            droneCache = api.fetchDrones(new DroneParser(), 100, 0);
            droneTypeCache = api.fetchDrones(new DroneTypeParser(), 100, 0);
        } catch (Exception e) {

        }
    }

    private void createPagePanel() {
        JPanel pagePanel = new JPanel();
        pagePanel.setLayout(new BorderLayout());
        pagePanel.setBackground(Colors.POWDER_BLUE);
    
        JButton nextButton = new JButton(">");
        JButton prevButton = new JButton("<");
        pageLabel = new JLabel(currentPage + "", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Arial", Font.BOLD, 16));
        pageLabel.setForeground(Colors.GUNMETAL);

        JTextField pageNumberField = new JTextField("" + currentPage);


        nextButton.addActionListener(e -> {
            currentPage++;
            pageLabel.setText(currentPage + "");
            pageNumberField.setText("" + currentPage);
            System.out.println("Current Page: " + currentPage);
            updateDroneView();
        });

        prevButton.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateDroneView();
            }
            System.out.println("Current Page: " + currentPage);
            pageLabel.setText(currentPage + "");
            pageNumberField.setText("" + currentPage);
        });

        pageNumberField.addActionListener(e -> {
            currentPage = Integer.parseInt(pageNumberField.getText());
            updateDroneView();
        });
    
        pagePanel.add(nextButton, BorderLayout.EAST);
        pagePanel.add(prevButton, BorderLayout.WEST);
        //pagePanel.add(pageLabel, BorderLayout.CENTER);
        pagePanel.add(pageNumberField, BorderLayout.CENTER);
        add(pagePanel, BorderLayout.SOUTH);
    }

    private void updateDroneView() {
        ArrayList<DynamicDrone> drones = new ArrayList<>();
        Map<Integer, DynamicDrone> newDrones = Map.of();
        try {
            //drones = api.fetchDroneData(new DynamicDroneParser(), 16, (currentPage - 1) * 16 + 1);
            newDrones = api.fetchDrones(new DynamicDroneParser(), 16, (currentPage - 1) * 16 + 1);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        //Sort the drones by drone ID
        drones.sort(Comparator.comparing(drone -> {String fullDroneUrl = drone.getDrone();
            String droneId = extractDroneId(fullDroneUrl);

            //Try to parse the drone ID as an integer
            try {
                return Integer.parseInt(droneId);
            }catch (NumberFormatException e){
                return 0;
            }
        }));

        int columns = 3;
        int rows = (int) Math.ceil((double) drones.size()/columns);
        innerContentPanel.setLayout(new GridLayout(rows, columns, 20, 20));
        innerContentPanel.removeAll();

        for (DynamicDrone drone : newDrones.values()) {
            innerContentPanel.add(createDronePanel(drone));
        }

        innerContentPanel.revalidate();
        innerContentPanel.repaint();
    }

    private JPanel createDronePanel(DynamicDrone drone) {
        JPanel dronePanel = new JPanel();
        dronePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dronePanel.setLayout(new GridLayout(12, 1));
        dronePanel.setBackground(Colors.GUNMETAL);

        String fullDroneUrl = drone.getDrone(); // take the value of drone from the JSON of class DynamicDrone
        String droneId = extractDroneId(fullDroneUrl); // Inilialize with the full URL

        dronePanel.add(createLabelHelper("Drone: " + droneId));
        dronePanel.add(createLabelHelper("Timestamp: " + drone.getTimestamp()));
        dronePanel.add(createLabelHelper("Speed: " + drone.getSpeed()));
        dronePanel.add(createLabelHelper("Align Roll: " + drone.getAlignRoll()));
        dronePanel.add(createLabelHelper("Align Pitch: " + drone.getAlignPitch()));
        dronePanel.add(createLabelHelper("Align Yaw: " + drone.getAlignYaw()));
        dronePanel.add(createLabelHelper("Longitude: " + drone.getLongitude()));
        dronePanel.add(createLabelHelper("Latitude: " + drone.getLatitude()));
        dronePanel.add(createLabelHelper("Battery Status: " + drone.getBatteryStatus()));
        dronePanel.add(createLabelHelper("Last Seen: " + drone.getLastSeen()));
        dronePanel.add(createLabelHelper("Status: " + drone.getStatus()));

        int id = extractId(drone.getDrone());

        if (id != -1) {
            Drone d = droneCache.get(id);
            int typeID = extractId(d.getDroneType());
            DroneType droneType = droneTypeCache.get(typeID);
            dronePanel.add(new BatteryPanel(drone.getBatteryStatus(), droneType.getBatteryCapacity()));
        }

        dronePanel.setBorder( new FlatLineBorder( new Insets( 16, 16, 16, 16 ), null, 0, 32 ) );

        return dronePanel;
    }

    private int extractId(String url) {
        String[] parts  = url.split("/");
        return parts.length > 0 ? Integer.parseInt(parts[parts.length - 2]) : -1;
    }

    private JLabel createLabelHelper(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Colors.PLATINUM);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    //Little helper method to extract the drone ID from the URL
    private String extractDroneId(String fullDroneUrl)
    {
        String droneId = fullDroneUrl;
        try {
            if (fullDroneUrl != null && !fullDroneUrl.isEmpty()){
                String[] parts = fullDroneUrl.split("/");
                droneId = parts[parts.length - 2];
            }
        }catch (Exception e){
            System.err.println("Error while extracting drone ID:" + e.getMessage());
        }
        return droneId;
    }
}