package utils;

import core.DynamicDrone;

public class DynamicDroneFilter extends BaseDroneFilter<DynamicDrone> {
    private int minSpeed;
    private int maxSpeed;

    // Should probably introduce some kind of range class to make the Work easier, as most attributes in the DynamicDrone can be displayed that way.
    public DynamicDroneFilter(int minSpeed, int maxSpeed) {
        addCondition(drone -> drone.getSpeed() >= minSpeed && drone.getSpeed() <= maxSpeed);
    }
}
