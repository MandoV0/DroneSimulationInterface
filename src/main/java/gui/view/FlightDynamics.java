package gui.view;

import core.Drone;
import core.DroneType;
import core.DynamicDrone;
import core.parser.DroneParser;
import core.parser.DroneTypeParser;
import core.parser.DynamicDroneParser;
import gui.BatteryPanel;
import gui.DroneFilterWindow;
import services.DroneSimulationInterfaceAPI;
import utils.AutoRefresh;
import utils.DefaultDroneFilter;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FlightDynamics extends JPanel {
    private static final Logger log = Logger.getLogger(FlightDynamics.class.getName());
    public static final int MAX_DRONES_PER_PAGE = 32;
    private int currentPage = 0;
    private final JPanel contentPanel;
    private final JLabel currentPageLabel;
    private Map<Integer, DroneType> droneTypesCache = Map.of();
    private Map<Integer, Drone> droneCache = Map.of();
    private DefaultDroneFilter defaultDroneFilter = new DefaultDroneFilter("NOT", 0, Integer.MAX_VALUE);

    public FlightDynamics() {
        setLayout(new BorderLayout());

        // Top panel for title
        JPanel titlePanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Flight Dynamics", JLabel.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        titleLabel.setOpaque(true);
        titleLabel.setBackground(UIManager.getColor("Panel.background"));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        add(titlePanel, BorderLayout.NORTH);

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        contentPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10), BorderFactory.createEtchedBorder(UIManager.getColor("Panel.background").brighter(),
                UIManager.getColor("Panel.background").darker())));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(UIManager.getColor("Panel.background").brighter());
        ImageIcon refreshIcon = new ImageIcon(new ImageIcon(getClass().getResource("/Icons/filter.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH));

        JButton filterButton = new JButton(refreshIcon);
        filterButton.setToolTipText("Filter Drones");
        filterButton.setBackground(UIManager.getColor("Button.background"));
        filterButton.addActionListener(e -> {
            DroneFilterWindow df = new DroneFilterWindow(this, defaultDroneFilter);
            df.setVisible(true);
        }
        );

        toolBar.add(filterButton);
        add(toolBar, BorderLayout.PAGE_START);

        JScrollPane scrollPane = new JScrollPane(contentPanel) {
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, width, height);
                titlePanel.setVisible(getVerticalScrollBar().getValue() == 0);
            }
        };
        scrollPane.getVerticalScrollBar().addAdjustmentListener(e -> {
            titlePanel.setVisible(scrollPane.getVerticalScrollBar().getValue() == 0);
        });
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        JPanel paginationPanel = new JPanel();
        paginationPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(UIManager.getColor("Panel.background"));
        JButton prevPageButton = new JButton("< Previous");
        JButton nextPageButton = new JButton("Next >");
        currentPageLabel = new JLabel("Page: " + (currentPage + 1));
        currentPageLabel.setForeground(UIManager.getColor("Label.foreground"));

        prevPageButton.addActionListener(e -> loadPage(currentPage - 1));
        nextPageButton.addActionListener(e -> loadPage(currentPage + 1));

        paginationPanel.add(prevPageButton);
        paginationPanel.add(currentPageLabel);
        paginationPanel.add(nextPageButton);
        add(paginationPanel, BorderLayout.SOUTH);

        preWarm();
        loadPage(0);

        JTextField pageInputField = new JTextField(5);
        pageInputField.addActionListener(e -> {
            try {
                int targetPage = Integer.parseInt(pageInputField.getText().trim());
                if (targetPage > 0) {
                    loadPage(targetPage - 1);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid page number. Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        paginationPanel.add(pageInputField);

        AutoRefresh refresh = new AutoRefresh();
        refresh.start(() -> loadPage(currentPage), 60, 45, TimeUnit.SECONDS);
    }

    private void preWarm() {
        try {
            droneTypesCache = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneTypeParser(), 40, 0);
            droneCache = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DroneParser(), 40, 0);
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Failed to cache DroneTypes/Drones.");
        }
    }

    private void loadPage(int page) {
        if (page < 0) {
            return;
        }

        contentPanel.removeAll();

        if (droneCache.isEmpty() || droneTypesCache.isEmpty()) {
            log.log(Level.INFO, "Drone Cache is empty. Attempting to cache drones.");
            preWarm();
        }

        try {
            Map<Integer, DynamicDrone> drones = DroneSimulationInterfaceAPI.getInstance().fetchDrones(new DynamicDroneParser(), MAX_DRONES_PER_PAGE, page * MAX_DRONES_PER_PAGE);

            /* Filter doesn't work properly with paging as we only have the MAX_DRONES_PER_PAGE to work with per page. */

            for (DynamicDrone drone : drones.values()) {
                contentPanel.add(createDronePanel(drone));
                contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            }

            contentPanel.revalidate();
            contentPanel.repaint();

            currentPage = page;
            currentPageLabel.setText("Page: " + (currentPage + 1));
        } catch (IOException | InterruptedException e) {
            log.log(Level.SEVERE, "Failed to fetch DynamicDrone during page load: " + page);
            JOptionPane.showMessageDialog(this, "Error while fetching Dynamic Drones", "ERROR", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDronePanel(DynamicDrone drone) {
        JPanel panel = new JPanel();

        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("Panel.background").darker(), 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.setBackground(UIManager.getColor("Panel.background"));

        Drone d = null;
        DroneType type = null;

        try {
            d = droneCache.get(drone.getId());
            type = droneTypesCache.get(d.getDroneTypeID());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Drone cache is empty. Cant create Drone Panel.");
        }

        JLabel titleLabel = new JLabel("Drone | ID: " + drone.getId());
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel detailsLabel = new JLabel(String.format(
                "Speed: %.2f km/h, Last Seen: %s, Status: %s",
                (double) drone.getSpeed(), OffsetDateTime.parse(drone.getLastSeen()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm")), drone.getStatus()
        ));
        detailsLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JLabel locationLabel = new JLabel(String.format(
                "Location: [%.6f, %.6f] | Control Range: %.2f m",
                drone.getLongitude(), drone.getLatitude(), (double) type.getControlRange()
        ));
        locationLabel.setForeground(UIManager.getColor("Label.disabledForeground"));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(detailsLabel);
        textPanel.add(locationLabel);

        JPanel statusPanel = new JPanel();
        statusPanel.setOpaque(false);
        statusPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));

        BatteryPanel batteryPanel = new BatteryPanel(drone.getBatteryStatus(), type.getBatteryCapacity());
        statusPanel.add(batteryPanel);

        JPanel powerStatus = new JPanel();
        powerStatus.setBackground(Objects.equals(drone.getStatus(), "ON") ? Color.GREEN : Color.RED);
        powerStatus.setPreferredSize(new Dimension(15, 15));
        statusPanel.add(powerStatus);

        textPanel.add(statusPanel);
        panel.add(textPanel, BorderLayout.CENTER);
        return panel;
    }

    public void setFilter(DefaultDroneFilter filter) {
        defaultDroneFilter = filter;
        log.log(Level.INFO, "Set Drone Filter");
        loadPage(currentPage);
    }
}