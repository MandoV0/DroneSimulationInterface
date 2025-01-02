package services;


import core.DroneBase;
import utils.BaseDroneFilter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class DroneFilterService {

    public <T extends DroneBase> List<T> filterDrones(Collection<T> drones, BaseDroneFilter<T> filter) {
        return drones.stream()
                .filter(drone -> meetsCondition(drone, filter))
                .collect(Collectors.toList());
    }

    private <T extends DroneBase> boolean meetsCondition(T drone, BaseDroneFilter<T> filter) {
        return filter.getConditions().stream()
                .allMatch(condition -> condition.evaluate(drone));
    }
}
