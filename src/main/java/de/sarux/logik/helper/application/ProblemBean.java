package de.sarux.logik.helper.application;

import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.application.group.LogikGroup;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProblemBean {
    private final Map<String, LogikProblem> problems = new HashMap<>();

    private int keyCounter = 0;

    public void init(final List<LogikGroup> groups) {
        problems.put("solve0", new LogikProblem(groups));
        keyCounter = 0;
    }

    public void clear() {
        problems.clear();
        keyCounter = 0;
    }

    public void reset(LogikProblem logikProblem) {
        problems.clear();
        problems.put("solve0", logikProblem);
    }

    public LogikProblem getProblem(String key) {
        if (problems.containsKey(key))
            return problems.get(key);
        return null;
    }

    public void addProblem(String key, LogikProblem problem) throws LogikException {
        if (!problems.containsKey(key)) {
            problems.put(key, problem);
        } else
            throw new LogikException("Problem mit Schlüssel " + key + " existiert bereits!");
    }

    public String newKey() {
        keyCounter++;
        return Integer.toString(keyCounter);
    }

    public void removeProblem(String key) {
        problems.remove(key);
    }
}