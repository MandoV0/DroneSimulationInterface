package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class MainWindow extends JFrame {

    public MainWindow() {

        setTitle("Drone Simulation Interface");
        setSize(900, 1000);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            setIconImage(ImageIO.read(new File("Icons/appicon.png")));
        } catch (IOException e) {
            System.err.println("Failed to load App Icon: " + e.getMessage());
        }

        createTaskBar();
    }

    public void createTaskBar() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Drone", new DroneWindow());
        tabbedPane.addTab("Dynamic Drone", new DynamicDroneWindow());
        tabbedPane.addTab("Drone Types", new DroneTypeView());
        add(tabbedPane);
    }
}