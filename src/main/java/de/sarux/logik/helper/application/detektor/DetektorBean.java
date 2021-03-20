package de.sarux.logik.helper.application.detektor;

import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class DetektorBean {
    private LogikDetektorProblem currentProblem;

    public void init(final List<LogikGroup> groups) {
        currentProblem = new LogikDetektorProblem(groups);
    }


    public void clear() {
        currentProblem = null;
    }

    public void reset(LogikDetektorProblem problem) {
        currentProblem = problem;
    }
}