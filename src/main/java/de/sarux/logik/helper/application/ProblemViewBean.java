package de.sarux.logik.helper.application;

import de.sarux.logik.helper.LogikBlock;
import de.sarux.logik.helper.LogikException;
import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.detektor.LogikDetektorProblem;
import de.sarux.logik.helper.group.LogikGroup;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Getter
public class ProblemViewBean {
    @Getter(AccessLevel.PRIVATE)
    private final Map<String, LogicBlockView> views = new HashMap<>();

    public LogicBlockView getView(String name) {
        return views.get(name);
    }

    private LogicBlockView buildView(List<LogikGroup> groups, List<LogikBlock> blocks) throws LogikException {
        LogicBlockView currentView = new LogicBlockView(groups.size());

        for (LogikBlock block : blocks) {
            LogicBlockViewLine blockLine = LogicBlockViewBuilder.buildBlockViewLine(block);
            currentView.addLine(blockLine);

            List<LogicBlockViewLine> blockLines = LogicBlockViewBuilder.buildBlockLines(groups, block);
            currentView.addLines(blockLines);
        }

        final LogicBlockViewLine addViewLine = new LogicBlockViewLine(ViewLineType.ADD_BLOCK, null, null, null);
        currentView.addLine(addViewLine);
        return currentView;
    }

    public void clear() {
        views.clear();
    }

    public void buildProblemView(LogikProblem problem) throws LogikException {
        LogicBlockView logicBlockView = buildView(problem.getGroups(), problem.getBlocks());
        views.put(SolverController.SOLVE_VIEW_NAME, logicBlockView);
    }

    public void initView(String solveViewName, List<LogikGroup> groups) {
        LogicBlockView currentView = new LogicBlockView(groups.size());

        final LogicBlockViewLine addViewLine = new LogicBlockViewLine(ViewLineType.ADD_BLOCK, null, null, null);
        currentView.addLine(addViewLine);
        views.put(solveViewName, currentView);
    }

    public void buildDetektorView(LogikDetektorProblem problem) throws LogikException {
        LogicBlockView logicBlockView = buildView(problem.getGroups(), problem.getAllBlocks());
        views.put(DetektorController.DETEKTOR_VIEW_NAME, logicBlockView);
    }
}