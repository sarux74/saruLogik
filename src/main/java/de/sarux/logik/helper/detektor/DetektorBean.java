package de.sarux.logik.helper.detektor;

import de.sarux.logik.helper.LogikBlock;
import de.sarux.logik.helper.group.LogikGroup;
import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.application.LogicBlockView;
import de.sarux.logik.helper.application.LogicBlockViewBuilder;
import de.sarux.logik.helper.application.LogicBlockViewLine;
import de.sarux.logik.helper.application.ViewLineType;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
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