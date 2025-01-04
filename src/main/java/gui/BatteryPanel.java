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


    public BatteryPanel(int currentBatteryCapacity, int maxBatteryCapacity) {
        this.currentBatteryCapacity = currentBatteryCapacity;
        this.maxBatteryCapacity = maxBatteryCapacity;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setOpaque(false);

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

                // Calculate the scale factor
                int scaledWidth = getWidth();
                int scaledHeight = getHeight();
                int originalWidth = batteryImage.getWidth();
                int originalHeight = batteryImage.getHeight();

                float scaleX = (float) scaledWidth / originalWidth;
                float scaleY = (float) scaledHeight / originalHeight;
                float scale = Math.min(scaleX, scaleY); // Preserve aspect ratio
                int newWidth = (int) (originalWidth * scale);
                int newHeight = (int) (originalHeight * scale);

                // Calculate offsets to center the scaled image
                int xOffset = (getWidth() - newWidth) / 2;
                int yOffset = (getHeight() - newHeight) / 2;

                // Draw the scaled battery image
                g2d.drawImage(batteryImage, xOffset, yOffset, newWidth, newHeight, this);

                // Calculate the battery fill width relative to the scaled size
                int fillMargin = (int) (FILL_MARGIN * scale);
                int fillWidth = (int) ((newWidth - (fillMargin * 2)) * calculateBatteryRatio()) - 2;

                // Draw the battery fill
                g2d.setColor(getBatteryColor());
                g2d.fillRect(xOffset + fillMargin, yOffset + fillMargin, fillWidth, newHeight - (fillMargin * 2));

                // Draw the percentage text
                String percentageText = String.format("%.0f%%", calculateBatteryRatio() * 100);
                g2d.setFont(new Font("SansSerif", Font.BOLD, (int) (10 * scale))); // Scale the font size
                FontMetrics metrics = g2d.getFontMetrics();
                int textWidth = metrics.stringWidth(percentageText);
                int textHeight = metrics.getHeight();

                // Center the text over the battery icon
                int textX = xOffset + (newWidth - textWidth) / 2;
                int textY = yOffset + (newHeight + textHeight / 2) / 2;

                g2d.setColor(Color.BLACK); // Background text for better visibility
                g2d.drawString(percentageText, textX + 1, textY + 1); // Slight offset for shadow effect

                g2d.setColor(Color.WHITE); // Foreground text
                g2d.drawString(percentageText, textX, textY);
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(100, 40);
            }
        };

        add(batteryPanel);
    }

    public Dimension getPreferredSize() {
        return new Dimension(90, 30);
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
