package gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class BatteryPanel extends JPanel {
    private final int maxBatteryCapacity;
    private final int currentBatteryCapacity;
    private final BufferedImage batteryImage;
    private static final Color BATTERY_FULL_COLOR = new Color(76, 175, 80); // Green color for battery fill
    private static final Color BATTERY_LOW_COLOR = new Color(255, 0, 0);
    // Gap between BatteryImage and Rect
    private static final int FILL_MARGIN = 4;


    BatteryPanel(int currentBatteryCapacity, int maxBatteryCapacity) {
        this.currentBatteryCapacity = currentBatteryCapacity;
        this.maxBatteryCapacity = maxBatteryCapacity;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(new JLabel("Battery Capacity: "));

        try {
            batteryImage = ImageIO.read(new File("Icons/battery.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JComponent batteryPanel = new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(batteryImage, 0, 0, this);

                int fillWidth = (int) ((batteryImage.getWidth() - (FILL_MARGIN * 2)) * calculateBatteryRatio()) - 2;

                g2d.setColor(getBatteryColor());
                g2d.fillRect(FILL_MARGIN, FILL_MARGIN, fillWidth, batteryImage.getHeight() - (FILL_MARGIN * 2));
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(batteryImage.getWidth(), batteryImage.getHeight());
            }
        };

        add(batteryPanel);
    }

    private float calculateBatteryRatio() {
        return (float) currentBatteryCapacity / maxBatteryCapacity;
    }

    private Color getBatteryColor() {
        return lerp(BATTERY_LOW_COLOR, BATTERY_FULL_COLOR, calculateBatteryRatio());
    }

    private Color lerp(Color color1, Color color2, float t) {
        t = Math.min(1.0f, Math.max(0.0f, t)); // Clamp t between 0 and 1
        int r = (int) (color1.getRed() + t * (color2.getRed() - color1.getRed()));
        int g = (int) (color1.getGreen() + t * (color2.getGreen() - color1.getGreen()));
        int b = (int) (color1.getBlue() + t * (color2.getBlue() - color1.getBlue()));
        return new Color(r, g, b);
    }
}
