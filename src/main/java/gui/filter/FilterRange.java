package gui.filter;

import javax.swing.*;
import java.awt.*;

public class FilterRange extends JComponent {
    private final JSpinner minWeightSpinner;
    private final JSpinner maxWeightSpinner;
    private final double minWeight;
    private final double maxWeight;

    public FilterRange(String hint, double min, double max) {
        minWeight = min;
        maxWeight = max;

        setLayout(new FlowLayout(FlowLayout.CENTER));
        add(new JLabel(hint));

        SpinnerNumberModel minWeightModel = new SpinnerNumberModel(min, min, max, 1);
        SpinnerNumberModel maxWeightModel = new SpinnerNumberModel(max, min, max, 1);

        minWeightSpinner = new JSpinner(minWeightModel);
        maxWeightSpinner = new JSpinner(maxWeightModel);

        JSpinner.NumberEditor minWeightEditor = new JSpinner.NumberEditor(minWeightSpinner, "##0");
        JSpinner.NumberEditor maxWeightEditor = new JSpinner.NumberEditor(maxWeightSpinner, "##0");

        minWeightSpinner.setEditor(minWeightEditor);
        maxWeightSpinner.setEditor(maxWeightEditor);

        minWeightSpinner.addChangeListener(e -> {
            double minVal = (Double) minWeightSpinner.getValue();
            double maxVal = (Double) maxWeightSpinner.getValue();
            if (minVal > maxVal) {
                maxWeightSpinner.setValue(minVal);
            }
        });

        maxWeightSpinner.addChangeListener(e -> {
            double minVal = (Double) minWeightSpinner.getValue();
            double maxVal = (Double) maxWeightSpinner.getValue();
            if (maxVal < minVal) {
                minWeightSpinner.setValue(maxVal);
            }
        });

        add(new JLabel("Min:"));
        add(minWeightSpinner);
        add(new JLabel("Max:"));
        add(maxWeightSpinner);
    }

    public double getCurrentMinWeight() {
        return (Double) minWeightSpinner.getValue();
    }

    public double getCurrentMaxWeight() {
        return (Double) maxWeightSpinner.getValue();
    }

    public void setCurrentMinWeight(double minWeight) {
        minWeightSpinner.setValue(minWeight);
    }

    public void setCurrentMaxWeight(double maxWeight) {
        maxWeightSpinner.setValue(maxWeight);
    }

    public void reset() {
        minWeightSpinner.setValue(minWeight);
        maxWeightSpinner.setValue(maxWeight);
    }
}
