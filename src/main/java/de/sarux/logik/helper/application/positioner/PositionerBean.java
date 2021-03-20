package de.sarux.logik.helper.application.positioner;

import de.sarux.logik.helper.application.LogikException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PositionerBean {
    private final Map<String, Positioner> positioners = new HashMap<>();

    public void clear() {
        positioners.clear();
    }


    public Positioner getPositioner(String key) {
        if (positioners.containsKey(key))
            return positioners.get(key);
        return null;
    }

    public void addPositioner(String key, Positioner positioner) throws LogikException {
        positioners.put(key, positioner);
    }

    public void removePositioner(String key) {
        positioners.remove(key);
    }
}