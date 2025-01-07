package gui;

import core.Drone;
import core.parser.DroneParser;
import core.parser.JsonDroneParser;
import gui.view.DroneCatalog;
import gui.view.DroneDashboard;
import gui.view.FlightDynamics;
import services.DroneSimulationInterfaceAPI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {
    private DroneSimulationInterfaceAPI droneAPI;

    public MainWindow() {

        setTitle("Drone Simulation Interface");
        setSize(900, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        initalizeAPI();

        try {
            setIconImage(ImageIO.read(new File("Icons/appicon.png")));
        } catch (IOException e) {
            System.err.println("Failed to load App Icon: " + e.getMessage());
        }

        createTaskBar();
    }

    private void initalizeAPI() {
        try{
            JsonDroneParser<Drone> parser = new DroneParser();

            droneAPI = new DroneSimulationInterfaceAPI(parser,100,0);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                droneAPI.stopAutoUpdate();
            }));
        } catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "API Initialisation = " + e.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
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