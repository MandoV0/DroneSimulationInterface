package gui;



import gui.filter.FilterRange;
import utils.DefaultDroneFilter;

import javax.swing.*;
import java.awt.*;

// TODO: Should make this generic to work with all DroneTypes
/*
Should probably split all filtering Types (ComboBox, Range and so on) into their own JComponents.
Could then remove the ugly gridBagLayout and dynamically add required Components from the DroneFilters Condition.
 */
public class DroneFilterWindow extends JDialog {
    private JComboBox<String> carriageTypeComboBox;

    private final Object window;
    private final DefaultDroneFilter currentFilter;

    private FilterRange weightRange;

    public DroneFilterWindow(Object window, DefaultDroneFilter currentFilter) {
        setTitle("Drone Filter");
        this.window = window;
        this.currentFilter = currentFilter;
        init();
        pack();
    }

    private void init() {
        String[] carriageTypes = {"All Types", "NOT", "ACT", "SEN"};
        carriageTypeComboBox = new JComboBox<>(carriageTypes);

        setLayout(new GridLayout(3, 1));

        JPanel carriageTypePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        carriageTypePanel.add(new JLabel("Carriage Type:"));
        carriageTypePanel.add(carriageTypeComboBox);
        add(carriageTypePanel);

        weightRange = new FilterRange("Weight Range (g):", 0, 1000);
        add(weightRange);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton applyButton = new JButton("Apply");
        JButton cancelButton = new JButton("Cancel");
        JButton resetButton = new JButton("Reset");

        applyButton.addActionListener(e -> {
            createFilter();
            dispose();
        });

        cancelButton.addActionListener(e -> {

            dispose();
        });

        resetButton.addActionListener(e -> {
            resetFilter();
        });

        buttonPanel.add(applyButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel);

        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        carriageTypeComboBox.setSelectedItem(currentFilter.getCarriageType());
    }

    public void resetFilter() {
        weightRange.reset();
        carriageTypeComboBox.setSelectedIndex(0);
    }

    private void createFilter() {
        System.out.println(carriageTypeComboBox.getSelectedItem().toString());
        double min = weightRange.getCurrentMinWeight();
        double max = weightRange.getCurrentMaxWeight();

        DefaultDroneFilter filter = new DefaultDroneFilter(carriageTypeComboBox.getSelectedItem().toString(), (int) min, (int) max);

        //window.setFilter(filter);
    }
}