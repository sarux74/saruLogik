package de.sarux.logik.helper.application;

import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.group.LogikGroup;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class ProblemBean {
    private LogikProblem currentProblem;

    public void init(final List<LogikGroup> groups) {
        currentProblem = new LogikProblem(groups);
    }

    public void clear() {
        currentProblem = null;
    }

    public void reset(LogikProblem logikProblem) {
        currentProblem = logikProblem;
    }
}