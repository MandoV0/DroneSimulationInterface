package gui.view;

import core.DroneType;
import core.parser.DroneTypeParser;
import services.DroneSimulationInterfaceAPI;
import utils.Colors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

public class DroneCatalog extends JPanel {
    public DroneCatalog() {
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(UIManager.getColor("Panel.background"));

        DroneSimulationInterfaceAPI api = new DroneSimulationInterfaceAPI();
        Map<Integer, DroneType> drones = Map.of();
        try {
            drones = api.fetchDrones(new DroneTypeParser(), 40, 0);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (DroneType drone : drones.values()) {
            contentPanel.add(createDronePanel(drone));
        }

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createDronePanel(DroneType droneType) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIManager.getColor("TextField.borderColor"), 1),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        panel.setBackground(UIManager.getColor("Panel.background").darker());
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel titleLabel = new JLabel(droneType.getManufacturer() + " " + droneType.getTypeName(), JLabel.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(UIManager.getColor("TextField.foreground"));
        JLabel detailsLabel = new JLabel("Weight: " + droneType.getWeight() + "kg, Max Speed: " + droneType.getMaxSpeed() + " km/h");

        // Add to panel
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(detailsLabel);

        panel.add(textPanel, BorderLayout.CENTER);

        // Click listener for more info
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showDroneDetails(droneType);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                panel.setBackground(UIManager.getColor("Button.background").brighter());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                panel.setBackground(UIManager.getColor("Panel.background").darker());
            }
        });

        return panel;
    }

    private void showDroneDetails(DroneType drone) {
        String details = "<html><b>Manufacturer:</b> " + drone.getManufacturer() +
                "<br><b>Type:</b> " + drone.getTypeName() +
                "<br><b>Weight:</b> " + drone.getWeight() + "kg" +
                "<br><b>Max Speed:</b> " + drone.getMaxSpeed() + " km/h" +
                "<br><b>Battery Capacity:</b> " + drone.getBatteryCapacity() + "mAh" +
                "<br><b>Control Range:</b> " + drone.getControlRange() + "m" +
                "<br><b>Max Carriage:</b> " + drone.getMaxCarriage() + "kg</html>";

        JOptionPane.showMessageDialog(null, details, "Drone Details", JOptionPane.INFORMATION_MESSAGE);
    }
}
