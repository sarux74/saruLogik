package de.sarux.logik.helper.application;

import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.application.group.LogikGroupsBean;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class GeneralLogikProblemBean {
    private final Map<String, GeneralLogikProblem> problems = new HashMap<>();

    private int keyCounter = 0;
    private final List<LogikGroup> groups;

    @Autowired
    public GeneralLogikProblemBean(LogikGroupsBean logikGroupsBean) {
        this.groups = logikGroupsBean.getGroups();
        init();
    }
    
    public void init() {
        problems.clear();
        problems.put("solve0", new GeneralLogikProblem(groups));
        keyCounter = 0;
    }

    public void reset(GeneralLogikProblem logikProblem) {
        problems.clear();
        problems.put("solve0", logikProblem);
    }

    public GeneralLogikProblem getProblem(String key) {
        if (problems.containsKey(key))
            return problems.get(key);
        return null;
    }

    public void addProblem(String key, GeneralLogikProblem problem) throws LogikException {
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