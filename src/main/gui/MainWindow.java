package src.main.gui;

import com.formdev.flatlaf.ui.FlatLineBorder;
import src.main.core.Drone;
import src.main.core.parser.DroneParser;
import src.main.services.DroneFilterService;
import src.main.services.DroneSimulationInterfaceAPI;
import src.main.utils.DroneFilter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {

    // Color Pallet: TODO: dont Hardcode Color Pallet
    public static final Color PLATINUM = new Color(216, 219, 226);
    public static final Color POWDER_BLUE = new Color(169, 188, 208);
    public static final Color MOONSTONE = new Color(88, 164, 176);
    public static final Color GUNMETAL = new Color(39, 43, 52);
    public static final Color EERIE_BLACK = new Color(27, 27, 30);

    private final JPanel contentPanel;

    public MainWindow() {
        setTitle("Drone Simulation Interface");
        setSize(900, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            setIconImage(ImageIO.read(new File("Icons/appicon.png")));
        } catch (IOException e) {
            System.err.println("Failed to load App Icon: " + e.getMessage());
        }

        createToolBar();

        contentPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20)); // Flowlayout for now with hori 20 & Verti 20
        //contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));  //**
        contentPanel.setBackground(EERIE_BLACK); //Set the backround color of the contentPanel
        //contentPanel.setBackground(new Color(27, 27, 30));

        DroneSimulationInterfaceAPI api = new DroneSimulationInterfaceAPI();

        ArrayList<Drone> drones = null;
        try {
            drones = api.fetchDroneData(new DroneParser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println(drones.size() + " Drones fetched");

        if (drones != null) {
            for (int i = 0; i < drones.size(); i++) {
                contentPanel.add(createDronePanel(drones.get(i)));
            }
        }

        //contentPanel.setVisible(true);

        //Füge JScrollPane hinzu
        JScrollPane scrollPane = new JScrollPane(contentPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // ScrollPane just for Vertical Scrolling

        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // Smooth Scrolling
        contentPanel.setPreferredSize(new Dimension(850, drones.size() * 120)); // Adjust the size of the contentPanel (120 Pixel per Drone)
        add(scrollPane, BorderLayout.CENTER); // ScrollPane add to the Center of the BoderLayout
    }

    private void createToolBar() {
        JToolBar toolBar = new JToolBar();

        JComboBox<String> droneTypeComboBox = new JComboBox<>(new String[]{
                "All Types",
                "NOT",
                "ACT",
                "SEN"
        });
        droneTypeComboBox.addActionListener(e -> filterDronesByType( (String)droneTypeComboBox.getSelectedItem() ) );

        toolBar.add(new JLabel("Refresh: "));
        JButton refreshButton = createToolBarButtonHelper("Refresh", "🔄");
        refreshButton.addActionListener(e -> {
            DroneFilterWindow filterWindow = new DroneFilterWindow();
            filterWindow.setVisible(true);
        });
        toolBar.add(refreshButton);
        toolBar.addSeparator();
        toolBar.add(new JLabel("Filter: "));
        toolBar.add(droneTypeComboBox);

        add(toolBar, BorderLayout.NORTH);
    }

    private JButton createToolBarButtonHelper(String tooltip, String text) {
        JButton button = new JButton(text);
        button.setToolTipText(tooltip);
        button.setBackground(GUNMETAL);
        button.setForeground(PLATINUM);
        return button;
    }

    private void filterDronesByType(String type) {
        System.out.println("Filtering Drones by Type: " + type);

        // Not sure if we should refetch or work with the existing Drones.

        DroneSimulationInterfaceAPI api = new DroneSimulationInterfaceAPI();

        ArrayList<Drone> drones = null;
        try {
            drones = api.fetchDrones();
            drones = api.fetchDroneData(new DroneParser());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (drones == null) {
            System.err.println("No Drones to Filter");
            return;
        }

        contentPanel.removeAll();

        DroneFilter filter = new DroneFilter.Builder()
                .weightRange(0, 10)
                .carriageType("ACT")
                .build();

        DroneFilterService droneFilterService = new DroneFilterService();
        List<Drone> filteredDrones = droneFilterService.filterDrones(drones, filter);

        System.out.println(filteredDrones.size() + " Drones after Filtering");

        for (int i = 0; i < filteredDrones.size(); i++) {
            contentPanel.add(createDronePanel(filteredDrones.get(i)));
        }

        /*
        for (int i = 0; i < drones.size(); i++) {
            if ( type.equals(drones.get(i).getCarriageType()) || type.equals("All Types") ) {
                contentPanel.add(createDronePanel(drones.get(i)));
            }
        }*/

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private JPanel createDronePanel(Drone drone) {
        JPanel dronePanel = new JPanel();

        dronePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        dronePanel.setLayout(new GridLayout(5, 1));
        dronePanel.setBackground(GUNMETAL);

        JLabel droneIDLabel = createLabelHelper(drone.getId() + "");
        droneIDLabel.setFont(new Font("Arial", Font.BOLD, 32));

        JLabel droneCarriageTypeLabel = createLabelHelper("Carriage Type: " + drone.getCarriageType());
        JLabel droneCarriageWeightLabel = createLabelHelper("Carriage Weight: " + drone.getCarriageWeight() + "g");
        JLabel droneSerialNumberLabel = createLabelHelper("Serial Number: " + drone.getSerialNumber());

        // TODO: Drone Type is a URL to the Dronetypes API, have to compare ID and fetch Information from Dronetypes
        //JLabel droneTypeLabel = createLabelHelper("Drone Type: " + drone.getDroneType());

        dronePanel.add(droneIDLabel);
        dronePanel.add(droneCarriageTypeLabel);
        dronePanel.add(droneCarriageWeightLabel);
        dronePanel.add(droneSerialNumberLabel);

        dronePanel.setBorder( new FlatLineBorder( new Insets( 16, 16, 16, 16 ), null, 0, 32 ) );

        return dronePanel;
    }

    private JLabel createLabelHelper(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(PLATINUM);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        return label;
    }
}
