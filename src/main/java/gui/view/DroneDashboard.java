package gui.view;

import core.drone.Drone;
import core.drone.DroneType;
import core.drone.DynamicDrone;
import core.parser.DroneParser;
import core.parser.DroneTypeParser;
import gui.components.APIErrorPanel;
import gui.components.BatteryPanel;
import services.DroneSimulationInterfaceAPI;
import services.Helper;
import exception.DroneAPIException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DroneDashboard extends JPanel {
    private final Logger log = Logger.getLogger(DroneDashboard.class.getName());
    private final JPanel dronesPanel;
    private final JPanel droneInfoLabel;
    private Map<Integer, DroneType> droneTypesCache = Map.of();
    private Map<Integer, Drone> droneCache = Map.of();
    private static final int DRONE_SAMPLE_SIZE = 80;

    /**
     * Initializes the DroneDashboard
     */
    public DroneDashboard() {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        dronesPanel = new JPanel();
        dronesPanel.setLayout(new BoxLayout(dronesPanel, BoxLayout.Y_AXIS));
        dronesPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane leftScrollPane = new JScrollPane(dronesPanel);
        leftScrollPane.setBorder(BorderFactory.createEtchedBorder(UIManager.getColor("Panel.background").brighter(),
                UIManager.getColor("Panel.background").darker()));
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);                     // 16 Smoother scrolling

        leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.weightx = 0.3; // Fills 20% of the screen horizontally
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(leftScrollPane, gridBagConstraints);

        droneInfoLabel = new JPanel();
        droneInfoLabel.setBorder(BorderFactory.createEtchedBorder(UIManager.getColor("Panel.background").brighter(),
                UIManager.getColor("Panel.background").darker()));
        droneInfoLabel.setBackground(UIManager.getColor("Panel.background"));

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.weightx = 0.7; // Fills 80% of the screen
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(droneInfoLabel, gridBagConstraints);

        showPlaceholder();
        loadDrones();
        cacheDroneData();
    }

    /**
     * Loads drones and displays them in the panel.
     */
    private void loadDrones() {
        try {
            // Only 40 Drones exist, so we only fetch 40 drones.
            Map<Integer, Drone> drones = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneParser(), 40, 0);

            for (Drone drone : drones.values()) {
                dronesPanel.add(createDroneButton(drone.getId()));
            }

            log.log(Level.INFO, "Successfully fetched " + drones.size() + " Drones.");

            dronesPanel.revalidate();
            dronesPanel.repaint();
        } catch (DroneAPIException e) {
            log.log(Level.SEVERE, "Failed to load Drones. Creating Error handling panel.");
            showErrorPanel(e);
        }
    }

    /**
     * Preloads data for drone types and caches it to improve performance.
     */
    private void cacheDroneData() {
        try {
            droneTypesCache = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneTypeParser(), 40, 0);
            droneCache = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneParser(), 40, 0);
        } catch (DroneAPIException e) {
            log.log(Level.SEVERE, "Failed to load Drones for Cache.");
            showErrorPanel(e);
        }
    }

    /**
     * Displays an error panel when an API exception occurs.
     *
     * @param exception The exception to display in the error panel.
     */
    private void showErrorPanel(DroneAPIException exception) {
        APIErrorPanel errorPanel = new APIErrorPanel(action -> {
            showPlaceholder();
            cacheDroneData();
            loadDrones();
        }, exception.getMessage());

        droneInfoLabel.removeAll();
        droneInfoLabel.add(errorPanel);
        droneInfoLabel.revalidate();
        droneInfoLabel.repaint();
    }

    /**
     * Called when User clicks on a Button, to load Drone Information
     * Calculates Custom Information from existing Data.
     * - How much Battery time is left
     * - Drone Carriage ballast
     * @param id The ID of the drone to display
     */
    private void loadDronePage(int id) {
        droneInfoLabel.removeAll();
        droneInfoLabel.setLayout(new BorderLayout(10, 10));
        droneInfoLabel.setBackground(UIManager.getColor("Panel.background"));
        droneInfoLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<DynamicDrone> dynamicDrones = new ArrayList<>();
        try {
            dynamicDrones = DroneSimulationInterfaceAPI.getInstance().fetchDrones(id, DRONE_SAMPLE_SIZE, 0);
        } catch (DroneAPIException e) {
            log.log(Level.SEVERE, "Failed to load Drone Sample.");
            showErrorPanel(e);
            throw new DroneAPIException("Failed to load Drone sample");
        }
        log.log(Level.INFO, "Successfully loaded " + dynamicDrones.size() + " Drones with a sample size of: " + DRONE_SAMPLE_SIZE);

        if (!dynamicDrones.isEmpty()) {
            DynamicDrone latestDynamicDrone = dynamicDrones.get(dynamicDrones.size() - 1);
            Drone latestDrone = droneCache.get(latestDynamicDrone.getId());
            DroneType latestDroneType = droneTypesCache.get(latestDrone.getDroneTypeID());

            // Create the StatusBar (ON/OFF, TimeStamp, SerialNumber...)
            JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
            statusBar.setOpaque(false);
            statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            OffsetDateTime dateTime = OffsetDateTime.parse(latestDynamicDrone.getTimestamp());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            String formattedDate = dateTime.format(formatter);

            // Add timestamp
            JLabel timestampLabel = new JLabel("Timestamp: " + formattedDate);
            timestampLabel.setFont(new Font("Arial", Font.BOLD, 12));
            statusBar.add(new BatteryPanel(latestDynamicDrone.getBatteryStatus(), latestDroneType.getBatteryCapacity()));

            // Add components to status bar
            statusBar.add(timestampLabel);
            statusBar.add(createStatusIndicator(latestDynamicDrone));
            statusBar.add(new JLabel("SN: " + latestDrone.getSerialNumber()));

            // Create main info panel
            JPanel mainInfo = new JPanel(new GridLayout(4, 1, 10, 10));
            mainInfo.setBackground(UIManager.getColor("Panel.background").brighter());

            // Calculates the total distance and average speed the drone traveled for the last DRONE_SAMPLE_SIZE samples.
            double totalDistance = 0;
            double averageSpeed = 0;
            for (int i = 1; i < dynamicDrones.size(); i++) {
                DynamicDrone prev = dynamicDrones.get(i - 1);
                DynamicDrone curr = dynamicDrones.get(i);
                totalDistance += Helper.haversineDistance(prev.getLongitude(), prev.getLatitude(), curr.getLongitude(), curr.getLatitude());
                averageSpeed += curr.getSpeed();
            }
            averageSpeed = averageSpeed / dynamicDrones.size();

            double carriageLast = (double) latestDrone.getCarriageWeight() / (double) latestDroneType.getWeight();
            carriageLast = carriageLast * 100;                  // To get the actual percentage

            // Formatting last seen to get the right DateTime
            OffsetDateTime lastSeenTime = OffsetDateTime.parse(latestDynamicDrone.getLastSeen());
            String formattedLastSeen = lastSeenTime.format(formatter);

            // Create info boxes with data
            mainInfo.add(createInfoBox("Current Speed", String.format("%.1f km/h", (double) latestDynamicDrone.getSpeed())));
            mainInfo.add(createInfoBox("Total Distance", String.format("%.2f km", totalDistance / 1000)));
            mainInfo.add(createInfoBox("Location",
                    String.format("%.6f, %.6f", latestDynamicDrone.getLongitude(), latestDynamicDrone.getLatitude())));
            mainInfo.add(createInfoBox("Average Speed", String.format("%.2f km/h", averageSpeed)));
            mainInfo.add(createInfoBox("Carriage Last", String.format("%.2f", carriageLast) + " %"));
            mainInfo.add(createInfoBox("Last Seen", formattedLastSeen));

            mainInfo.add(createInfoBox("General Info", "<html>" + "Carriage Type: "
                    + latestDrone.getCarriageType() + "<br/>Manufacturer: "
                    + latestDroneType.getManufacturer() + "<br/>Model: "
                    + latestDroneType.getTypeName() + "</html>")); // Need to add html for line breaks to work

            // Add all components to main panel
            droneInfoLabel.add(statusBar, BorderLayout.NORTH);
            droneInfoLabel.add(mainInfo, BorderLayout.CENTER);
        } else {
            // Error handling. But should even get here anyway
        }

        droneInfoLabel.revalidate();
        droneInfoLabel.repaint();
    }
    /**
     * Creates a status bar showing drone status, timestamp, and serial number.
     *
     * @param latestDynamicDrone The latest dynamic drone data
     * @return A JPanel representing the status bar.
     */

    private JPanel createStatusIndicator(DynamicDrone latestDynamicDrone) {
        JPanel statusIndicator = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(latestDynamicDrone.getStatus().equals("ON") ? Color.GREEN : Color.RED);
                g2.fillOval(0, 0, 20, 20);
            }
        };
        statusIndicator.setPreferredSize(new Dimension(20, 20));
        return statusIndicator;
    }

    // Helper method to create consistent info boxes
    private JPanel createInfoBox(String title, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBackground(UIManager.getColor("Panel.background").brighter());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Panel.foreground").darker().darker().darker()),
                BorderFactory.createEmptyBorder(10, 15, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(UIManager.getColor("Label.foreground").brighter());

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private JButton createDroneButton(int id) {
        JButton button = new JButton("Drone " + id);
        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setMinimumSize(new Dimension(120, 50));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        button.setPreferredSize(new Dimension(100, 50));

        button.setFont(new Font("Arial", Font.BOLD, 16));

        button.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Font currentFont = button.getFont();
                int buttonWidth = button.getWidth();
                int newFontSize = Math.max(buttonWidth / 20, 16);
                button.setFont(new Font(currentFont.getName(), currentFont.getStyle(), newFontSize));
            }
        });

        button.addActionListener(e -> loadDronePage(id));
        return button;
    }

    // Placeholder before a drone is selected
    private void showPlaceholder() {
        droneInfoLabel.removeAll();

        JPanel placeholderPanel = new JPanel();
        placeholderPanel.setLayout(new BoxLayout(placeholderPanel, BoxLayout.Y_AXIS));
        placeholderPanel.setBorder(BorderFactory.createEmptyBorder(100, 50, 100, 50));

        JLabel placeholderLabel = new JLabel("No data to show.");
        placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholderLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        placeholderPanel.add(placeholderLabel);

        JLabel placeholderLabel2 = new JLabel("Click on a Drone to view details.");
        placeholderLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeholderLabel2.setFont(new Font("Arial", Font.ITALIC, 16));
        placeholderPanel.add(placeholderLabel2);

        droneInfoLabel.setLayout(new BorderLayout());
        droneInfoLabel.add(placeholderPanel, BorderLayout.CENTER);

        droneInfoLabel.revalidate();
        droneInfoLabel.repaint();
    }
}