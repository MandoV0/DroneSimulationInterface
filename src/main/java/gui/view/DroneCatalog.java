package gui.view;

import core.DroneType;
import core.parser.DroneTypeParser;
import customException.DroneDataFetchException;
import services.DroneSimulationInterfaceAPI;
import utils.Constants;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Map;

public class DroneCatalog extends JPanel {

    public DroneCatalog() {
        setLayout(new BorderLayout());

        // Title Panel
        JPanel titlePanel = createTitlePanel();
        add(titlePanel, BorderLayout.NORTH);

        // Content Panel
        JScrollPane scrollPane = createContentScrollPane();
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createTitlePanel() {
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Drone Catalog", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(UIManager.getColor("Label.background"));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        return titlePanel;
    }

    private JLabel createInfoLabel(String text, int fontSize) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
        label.setForeground(UIManager.getColor("Label.foreground"));
        label.setAlignmentX(Component.LEFT_ALIGNMENT); // Left-aligned
        return label;
    }

    private JScrollPane createContentScrollPane() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(0, 3, 15, 15)); // 3 columns with spacing
        contentPanel.setBackground(UIManager.getColor("Panel.background"));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Fetch and display drones
        Map<Integer, DroneType> drones = fetchDrones();
        for (DroneType drone : drones.values()) {
            contentPanel.add(createDroneCard(drone));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        return scrollPane;
    }

    private Map<Integer, DroneType> fetchDrones() {
        try {
            return DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneTypeParser(), 40, 0);
        } catch (IOException | InterruptedException e) {
            throw new DroneDataFetchException("Failed to fetch drones", e);
        }
    }

    private JPanel createDroneCard(DroneType droneType) {
        // Main panel for the drone card
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIManager.getColor("Panel.background").darker());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.setPreferredSize(new Dimension(200, 250)); // Smaller card size
        panel.setLayout(new BorderLayout());

        // Top section: Icon + Name
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        // Icon at the top left
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(Constants.DRONE_ICON_PATH));
        Image scaledImage = originalIcon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH); // Smaller icon
        ImageIcon scaledIcon = new ImageIcon(scaledImage);
        JLabel iconLabel = new JLabel(scaledIcon);
        iconLabel.setHorizontalAlignment(JLabel.LEFT);
        iconLabel.setVerticalAlignment(JLabel.TOP);
        topPanel.add(iconLabel, BorderLayout.WEST);

        // Centered name
        JLabel nameLabel = new JLabel(droneType.getManufacturer() + " " + droneType.getTypeName(), JLabel.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 12)); // Slightly smaller font
        nameLabel.setForeground(UIManager.getColor("Label.foreground"));
        topPanel.add(nameLabel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);

        // Detail section below the name
        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS)); // Vertical alignment
        detailPanel.setOpaque(false);

        // Add padding to push details further down
        detailPanel.add(Box.createVerticalStrut(10));

        // Adding details
        detailPanel.add(createInfoLabel("Weight: " + droneType.getWeight() + " kg", 12));
        detailPanel.add(createInfoLabel("Max Speed: " + droneType.getMaxSpeed() + " km/h", 12));
        detailPanel.add(createInfoLabel("Battery Capacity: " + droneType.getBatteryCapacity() + " mAh", 12));
        detailPanel.add(createInfoLabel("Control Range: " + droneType.getControlRange() + " m", 12));
        detailPanel.add(createInfoLabel("Max Carriage: " + droneType.getMaxCarriage() + " kg", 12));
        panel.add(detailPanel, BorderLayout.CENTER);

        return panel;
    }
}
