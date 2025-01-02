package gui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import core.DroneType;
import core.parser.DroneTypeParser;
import services.DroneSimulationInterfaceAPI;
import utils.Colors;


import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class    DroneTypeView extends JPanel {
    private final JPanel innerContentPanel;
    //private JLabel pageLabel;
    public DroneTypeView() {
        setLayout(new BorderLayout());
        setBackground(Colors.EERIE_BLACK);

        innerContentPanel = new JPanel();
        innerContentPanel.setBackground(Colors.EERIE_BLACK);
        innerContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JScrollPane scrollPane = new JScrollPane(innerContentPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        updateDroneTypeView();

        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);

    }

    private void updateDroneTypeView() {
        try {
            DroneSimulationInterfaceAPI api = new DroneSimulationInterfaceAPI();
            // Rufe die DroneType-Daten von der API ab
            Map<Integer, DroneType> droneTypes = api.fetchDrones(new DroneTypeParser(), 40, 0);

            int rows = (int) Math.ceil((double) droneTypes.size() / 3);
            innerContentPanel.setLayout(new GridLayout(rows, 6, 20, 20));
            innerContentPanel.removeAll();
            for (DroneType droneType : droneTypes.values()) {
                innerContentPanel.add(createDroneTypePanel(droneType));
            }

            innerContentPanel.revalidate();
            innerContentPanel.repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Fehler beim Laden der DroneType-Daten: " + e.getMessage(), "Fehler", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createDroneTypePanel(DroneType droneType){
        JPanel droneTypePanel = new JPanel();
        droneTypePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        droneTypePanel.setLayout(new GridLayout(11, 1));
        droneTypePanel.setBackground(Colors.GUNMETAL);

        droneTypePanel.add(createLabelHelper("ID: " + droneType.getId()));
        droneTypePanel.add(createLabelHelper("Manufacturer: " + droneType.getManufacturer()));
        droneTypePanel.add(createLabelHelper("Type Name: " + droneType.getTypeName()));
        droneTypePanel.add(createLabelHelper("Weight: " + droneType.getWeight()));
        droneTypePanel.add(createLabelHelper("Max Speed: " + droneType.getMaxSpeed()));
        droneTypePanel.add(createLabelHelper("Battery Capacity: " + droneType.getBatteryCapacity()));
        droneTypePanel.add(createLabelHelper("Control Range: " + droneType.getControlRange()));
        droneTypePanel.add(createLabelHelper("Max Carriage: " + droneType.getMaxCarriage()));

        droneTypePanel.setBorder( new FlatLineBorder( new Insets( 16, 16, 16, 16 ), null, 0, 32 ) );

        return droneTypePanel;
    }

    private JLabel createLabelHelper(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Colors.PLATINUM);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }

}