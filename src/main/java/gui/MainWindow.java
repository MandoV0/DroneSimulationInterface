package gui;

import gui.view.DroneCatalog;
import gui.view.DroneDashboard;
import gui.view.FlightDynamics;
import services.DroneSimulationInterfaceAPI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class MainWindow extends JFrame {
    private DroneSimulationInterfaceAPI droneAPI;

    public MainWindow() {
        setTitle("Drone Simulation Interface");
        setSize(900, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set application icon dynamically
        setAppIcon();

        createTaskBar();
    }

    private void setAppIcon() {
        try {
            // Dynamically locate the icon within resources
            URL iconURL = getClass().getResource("/Icons/appicon.png");
            if (iconURL != null) {
                setIconImage(ImageIO.read(iconURL));
                // Special case for macOS to set the Dock icon
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    Taskbar taskbar = Taskbar.getTaskbar();
                    taskbar.setIconImage(ImageIO.read(iconURL));
                }
            } else {
                System.err.println("App icon not found at the specified path!");
            }
        } catch (IOException e) {
            System.err.println("Failed to load App Icon: " + e.getMessage());
        }
    }
    public void createTaskBar() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Drone Catalog", new DroneCatalog());
        tabbedPane.addTab("Flight Dynamics", new FlightDynamics());
        tabbedPane.addTab("Drone Dashboard", new DroneDashboard());
        add(tabbedPane);
    }
}