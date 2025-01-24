package gui;

import customException.LoadingException;
import gui.view.DroneCatalog;
import gui.view.DroneDashboard;
import gui.view.FlightDynamics;
import services.DroneSimulationInterfaceAPI;
import utils.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class MainWindow extends JFrame {
    private DroneSimulationInterfaceAPI droneAPI;

    public MainWindow() {
        setTitle("Drone Simulation Interface");
        setSize(900, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);

        try {
            BufferedImage appIcon = ImageIO.read(getClass().getResource(Constants.APP_ICON_PATH));

            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                Taskbar.getTaskbar().setIconImage(appIcon);
            }
            else {
                setIconImage(appIcon);
            }

        } catch (IOException e) {
            System.err.println("Failed to load App Icon: " + e.getMessage());
            throw new LoadingException("Failed to load App Icon", e);
        }
        createTaskBar();
    }

    public void createTaskBar() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Drone Catalog", new DroneCatalog());
        tabbedPane.addTab("Flight Dynamics", new FlightDynamics());
        tabbedPane.addTab("Drone Dashboard", new DroneDashboard());
        add(tabbedPane);
    }
}