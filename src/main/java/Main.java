import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.*;
import gui.MainWindow;
import utils.RootLogger;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel( new FlatDarculaLaf() );
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