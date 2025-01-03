package src.main;

import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.FlatLightLaf;
import src.main.gui.MainWindow;
import src.main.utils.RootLogger;

public class Main
{
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel( new FlatLightLaf() );
        } catch( Exception ex ) {
            System.err.println("Failed to initialize FlatLightLeaf Look and Feel");
        }

        try {
            RootLogger.init();
        }
        catch( Exception ex ) {
            System.err.println("Failed to initialize RootLogger");
        }

        EventQueue.invokeLater( () ->  {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        }
        );
    }
}