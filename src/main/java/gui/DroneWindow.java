package gui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import core.Drone;
import core.parser.DroneParser;
import services.DroneFilterService;
import services.DroneSimulationInterfaceAPI;
import utils.Colors;
import utils.DefaultDroneFilter;


import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DroneWindow extends JPanel {
    private final JPanel innerContentPanel;
    private DefaultDroneFilter filter;

    public DroneWindow() {
        setLayout(new BorderLayout());

        filter = new DefaultDroneFilter("All Types", 0, 1000);

        createToolBar();

        innerContentPanel = new JPanel();
        innerContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        innerContentPanel.setBackground(Colors.EERIE_BLACK);

        JScrollPane scrollPane = new JScrollPane(innerContentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        updateDroneView();

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }

    private void createToolBar() {
        JToolBar toolBar = new JToolBar();

        JButton filterButton = createToolBarButtonHelper("Filter Drones", "Icons/filter.png");
        filterButton.addActionListener(e -> {
            DroneFilterWindow filterWindow = new DroneFilterWindow(this, filter);
            filterWindow.setVisible(true);
        });
        toolBar.add(filterButton);

        //toolBar.addSeparator();
        JButton refreshButton = createToolBarButtonHelper("Reset Filter and refresh", "Icons/refresh.png");
        refreshButton.addActionListener(e -> {
            filter = new DefaultDroneFilter("All Types", 0, 1000);
            updateDroneView();
        });

        toolBar.add(refreshButton);

        add(toolBar, BorderLayout.NORTH);
    }

    private JButton createToolBarButtonHelper(String tooltip, String iconPath) {
        ImageIcon icon = new ImageIcon(iconPath);
        JButton button = new JButton();
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setIcon(new ImageIcon(icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH)));
        button.setToolTipText(tooltip);
        button.setBackground(Colors.GUNMETAL);
        button.setForeground(Colors.PLATINUM);
        return button;
    }

    private void updateDroneView() {
        DroneSimulationInterfaceAPI api = new DroneSimulationInterfaceAPI();

        Map<Integer, Drone> drones = Map.of();
        try {
            drones = api.fetchDrones(new DroneParser(), 40, 0);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        innerContentPanel.removeAll();

        DroneFilterService droneFilterService = new DroneFilterService();
        List<Drone> filteredDrones = droneFilterService.filterDrones(drones.values(), filter);

        System.out.println(filteredDrones.size() + " Drones after Filtering");

        innerContentPanel.setPreferredSize(new Dimension(850, filteredDrones.size() * 120));

        for (Drone drone : filteredDrones) {
            innerContentPanel.add(createDronePanel(drone));
        }

        innerContentPanel.revalidate();
        innerContentPanel.repaint();
    }

    private JPanel createDronePanel(Drone drone) {
        JPanel dronePanel = new JPanel();

        dronePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dronePanel.setLayout(new GridLayout(5, 1));
        dronePanel.setBackground(Colors.GUNMETAL);

        JLabel droneIDLabel = createLabelHelper(drone.getId() + "");
        droneIDLabel.setFont(new Font("Arial", Font.BOLD, 32));

        JLabel droneCarriageTypeLabel = createLabelHelper("Carriage Type: " + drone.getCarriageType());
        JLabel droneCarriageWeightLabel = createLabelHelper("Carriage Weight: " + drone.getCarriageWeight() + "g");
        JLabel droneSerialNumberLabel = createLabelHelper("Serial Number: " + drone.getSerialNumber());

        dronePanel.add(droneIDLabel);
        dronePanel.add(droneCarriageTypeLabel);
        dronePanel.add(droneCarriageWeightLabel);
        dronePanel.add(droneSerialNumberLabel);

        dronePanel.setBorder( new FlatLineBorder( new Insets( 16, 16, 16, 16 ), null, 0, 32 ) );

        return dronePanel;
    }

    private JLabel createLabelHelper(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Colors.PLATINUM);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

    public void setFilter(DefaultDroneFilter filter) {
        this.filter = filter;
        updateDroneView();
    }
}
