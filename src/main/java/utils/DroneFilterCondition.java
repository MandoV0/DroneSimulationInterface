package utils;

import core.DroneBase;

public interface DroneFilterCondition<T extends DroneBase> {
    boolean evaluate(T drone);
}
