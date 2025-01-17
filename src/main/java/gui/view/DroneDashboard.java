package gui.view;

import core.Drone;
import core.DroneType;
import core.DynamicDrone;
import core.parser.DroneParser;
import core.parser.DroneTypeParser;
import gui.BatteryPanel;
import services.DroneSimulationInterfaceAPI;
import services.Helper;

import gui.filter.FilterRange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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

    public DroneDashboard() {
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();

        dronesPanel = new JPanel();
        dronesPanel.setLayout(new BoxLayout(dronesPanel, BoxLayout.Y_AXIS));
        dronesPanel.setBackground(UIManager.getColor("Panel.background"));

        JScrollPane leftScrollPane = new JScrollPane(dronesPanel);
        leftScrollPane.setBorder(BorderFactory.createEtchedBorder(UIManager.getColor("Panel.background").brighter(),
                UIManager.getColor("Panel.background").darker()));
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        leftScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(leftScrollPane, gridBagConstraints);

        droneInfoLabel = new JPanel();
        droneInfoLabel.setBorder(BorderFactory.createEtchedBorder(UIManager.getColor("Panel.background").brighter(),
                UIManager.getColor("Panel.background").darker()));
        droneInfoLabel.setBackground(UIManager.getColor("Panel.background"));

        gridBagConstraints.gridx = 1;
        gridBagConstraints.weighty = 1;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        add(droneInfoLabel, gridBagConstraints);

        showPlaceholder();

        try {
            loadDrones();
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Failed to load Drones.");
        }
        preWarm();
    }

    private void loadDrones() throws IOException, InterruptedException {
        Map<Integer, Drone> drones = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneParser(), 40, 0);
        for (Drone drone : drones.values()) {
            dronesPanel.add(createDroneButton(drone.getId()));
        }

        log.log(Level.INFO, "Successfully fetched " + drones.size() + " Drones.");

        dronesPanel.revalidate();
        dronesPanel.repaint();
    }

    private void preWarm() {
        try {
            droneTypesCache = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneTypeParser(), 40, 0);
            droneCache = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneParser(), 40, 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void loadDronePage(int id) {
        droneInfoLabel.removeAll();
        droneInfoLabel.setLayout(new BorderLayout(10, 10));
        droneInfoLabel.setBackground(new Color(245, 245, 245));
        droneInfoLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        List<DynamicDrone> dynamicDrones = new ArrayList<>();
        try {
            dynamicDrones = DroneSimulationInterfaceAPI.getInstance().fetchDrones(id, 40, 0);
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Failed to load Drone Sample.");
            throw new RuntimeException(e);
        }
        log.log(Level.INFO, "Successfully loaded " + dynamicDrones.size() + " Drones.");

        if (!dynamicDrones.isEmpty()) {
            DynamicDrone latestDrone = dynamicDrones.get(dynamicDrones.size() - 1);

            JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
            statusBar.setBackground(new Color(230, 230, 230));
            statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm");
            JLabel timestampLabel;

            /* BURAK PLEASE DO NOT CHANGE ITTTT*/

            Drone drone = droneCache.get(id);
            System.out.println(drone.getCreated());
            if(drone != null){
                try {
                    OffsetDateTime offsetDateTime = OffsetDateTime.parse(drone.getCreated(), inputFormatter);
                    String formattedCreated = offsetDateTime.format(outputFormatter);
                    timestampLabel = new JLabel("Timestamp: " + formattedCreated);
                } catch (DateTimeParseException e) {
                    timestampLabel = new JLabel("Timestamp: Invalid date format");
                }
            }
            else {
                timestampLabel = new JLabel("Timestamp: Drone not found");
            }

            timestampLabel.setFont(new Font("Arial", Font.BOLD, 12));
            statusBar.add(timestampLabel);

            JPanel statusIndicator = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(latestDrone.getStatus().equals("ON") ? Color.GREEN : Color.RED);
                    g2.fillOval(0, 0, 20, 20);
                }
            };
            statusIndicator.setPreferredSize(new Dimension(20, 20));

            statusBar.add(statusIndicator);

            JPanel mainInfo = new JPanel(new GridLayout(4, 1, 10, 10));
            mainInfo.setBackground(new Color(245, 245, 245));

            double totalDistance = 0;
            for (int i = 1; i < dynamicDrones.size(); i++) {
                DynamicDrone prev = dynamicDrones.get(i - 1);
                DynamicDrone curr = dynamicDrones.get(i);
                totalDistance += Helper.haversineDistance(prev.getLongitude(), prev.getLatitude(), curr.getLongitude(), curr.getLatitude());
            }

            mainInfo.add(createInfoBox("Speed", String.format("%.1f km/h", (double) latestDrone.getSpeed())));
            mainInfo.add(createInfoBox("Total Distance", String.format("%.2f km", totalDistance / 1000)));
            mainInfo.add(createInfoBox("Location",
                    String.format("%.6f, %.6f", latestDrone.getLongitude(), latestDrone.getLatitude())));

            JPanel lastInfoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
            lastInfoPanel.setBackground(new Color(245, 245, 245));

            lastInfoPanel.add(createInfoBox("Last Seen", "Yesterday oder so"));
            lastInfoPanel.add(createInfoBox("Carriage Last", "Mock Info"));
            mainInfo.add(lastInfoPanel);

            droneInfoLabel.add(statusBar, BorderLayout.NORTH);
            droneInfoLabel.add(mainInfo, BorderLayout.CENTER);
        }

        droneInfoLabel.revalidate();
        droneInfoLabel.repaint();
    }

    private JPanel createInfoBox(String title, String value) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 15, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(Color.GRAY);

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