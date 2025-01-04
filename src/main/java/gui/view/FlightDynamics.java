package gui.view;

import com.formdev.flatlaf.ui.FlatLineBorder;
import core.Drone;
import core.DroneType;
import core.DynamicDrone;
import core.parser.DroneParser;
import core.parser.DroneTypeParser;
import core.parser.DynamicDroneParser;
import gui.BatteryPanel;
import services.DroneSimulationInterfaceAPI;
import utils.Colors;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;

import java.util.Map;
import java.util.Objects;

public class FlightDynamics extends JPanel {
    public static final int MAX_DRONES_PER_PAGE = 32;
    private int currentPage = 0;
    private DroneSimulationInterfaceAPI api;                // Should make the API a singleton
    private JPanel contentPanel;
    private JLabel currentPageLabel;
    private Map<Integer, DroneType> droneTypesCache = Map.of();
    private Map<Integer, Drone> droneCache = Map.of();

    public FlightDynamics() {
        setLayout(new BorderLayout());

        api = new DroneSimulationInterfaceAPI();

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel();
        paginationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton prevPageButton = new JButton("<");
        JButton nextPageButton = new JButton(">");
        currentPageLabel = new JLabel(currentPage + 1 + "");

        prevPageButton.addActionListener(e -> { loadPage(currentPage - 1); });
        nextPageButton.addActionListener(e -> { loadPage(currentPage + 1); });

        // Don't change the Order. As it is a FlowLayout we need the Elements to be ordered correctly from left to right
        paginationPanel.add(prevPageButton);
        paginationPanel.add(currentPageLabel);
        paginationPanel.add(nextPageButton);

        add(paginationPanel, BorderLayout.SOUTH);

        preWarm();
        loadPage(0);
    }

    private void preWarm() {
        try {
            droneTypesCache = api.fetchDrones(new DroneTypeParser(), 40, 0);
            droneCache = api.fetchDrones(new DroneParser(), 40, 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loadPage(int page) {
        if (page < 0) { return; }

        // Remove all existing Drone Panels
        contentPanel.removeAll();

        try {
            Map<Integer, DynamicDrone> drones = api.fetchDrones(new DynamicDroneParser(), MAX_DRONES_PER_PAGE, page * MAX_DRONES_PER_PAGE);

            for (DynamicDrone drone : drones.values()) {
                contentPanel.add(createDronePanel(drone));
            }

            contentPanel.revalidate();
            contentPanel.repaint();

            currentPage = page;
            currentPageLabel.setText(currentPage + 1 + "");
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error while fetching Dynamic Drones", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDronePanel(DynamicDrone drone) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm");

        OffsetDateTime offsetDateTime = OffsetDateTime.parse(drone.getLastSeen(), inputFormatter);
        String formattedLastSeen = offsetDateTime.format(outputFormatter);
      //System.out.println(formattedLastSeen);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("TextField.borderColor"), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.setBackground(UIManager.getColor("Panel.background").darker());

        Drone d = droneCache.get(drone.getId());
        DroneType type = droneTypesCache.get(d.getDroneTypeID());

        // Drone info labels
        JLabel titleLabel = new JLabel("Drone | ID: " + drone.getId());
        titleLabel.setForeground(UIManager.getColor("TextField.foreground"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        JLabel detailsLabel = new JLabel(String.format(
                "Speed: %.2f km/h, Last Seen: %s, Status: %s",
                (double) drone.getSpeed(),formattedLastSeen, drone.getStatus()
        ));
        detailsLabel.setForeground(UIManager.getColor("TextField.foreground"));

        // Longitude & Latitude
        JLabel locationLabel = new JLabel(String.format(
                "Location: [%.6f, %.6f] | Control Range: %.2f m",
                drone.getLongitude(), drone.getLatitude(), (double) type.getControlRange()
        ));
        locationLabel.setForeground(UIManager.getColor("TextField.foreground"));

        // Add components to text panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(detailsLabel);
        textPanel.add(locationLabel);

        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        textPanel.add(statusPanel);
        statusPanel.add(new BatteryPanel(drone.getBatteryStatus(), type.getBatteryCapacity()));

        JPanel powerStatus = new JPanel();
        JLabel statusLabel = new JLabel("Status:");
        statusPanel.add(statusLabel);

        //reternary operator to check the status of the drone better than the if statment
        powerStatus.setBackground(Objects.equals(drone.getStatus(), "ON") ? Color.GREEN : Color.RED);

        statusPanel.add(powerStatus);

        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }
}
