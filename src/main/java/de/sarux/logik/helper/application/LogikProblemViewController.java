package de.sarux.logik.helper.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.sarux.logik.helper.LogikBlock;
import de.sarux.logik.helper.LogikElement;
import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.group.LogikGroup;
import de.sarux.logik.helper.LogikLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import static de.sarux.logik.helper.application.SolverController.SOLVE_VIEW_NAME;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("solve")
public class LogikProblemViewController {

    private final ProblemViewBean problemViewBean;

    // standard constructors
    @Autowired
    public LogikProblemViewController(ProblemBean problemBean, ProblemViewBean problemViewBean) {
        this.problemBean = problemBean;
        this.problemViewBean = problemViewBean;
    }

    private final ProblemBean problemBean;

    @GetMapping(path = "/problems/{problemKey}/view/group")
    public List<LogicBlockViewLine> getBuildViewGroup(@PathVariable String problemKey, @RequestParam(name = "groupId") Integer groupId) throws JsonProcessingException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        SolveHelper solveHelper = new SolveHelper(problem, view);

        return solveHelper.buildGroupViewLines(groupId);
    }

    @GetMapping(path = "/problems/{problemKey}/view/block/comparable")
    public List<IdNamePair> getComparableBlocks(@PathVariable String problemKey) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);

        OptionalInt numGroups = problem.getGroups().stream().mapToInt(o -> o.getElements().size()).min();
        List<IdNamePair> comparableBlocks;
        if (numGroups.isPresent()) {
            comparableBlocks = problem.getBlocks().stream().filter(o -> o.isNoDuplicates() && o.getMainLines().size() == numGroups.getAsInt()).map(o -> new IdNamePair(o.getBlockId(), o.getName())).collect(Collectors.toList());
        } else {
            comparableBlocks = new ArrayList<>();
        }

        return comparableBlocks;
    }

    @GetMapping(path = "/problems/{problemKey}/view/blockcompare")
    public BlockCompareView getBlockCompareView(@PathVariable String problemKey, @RequestParam(name = "blockId1") Integer blockId1, @RequestParam(name = "blockId2") Integer blockId2) throws JsonProcessingException {

        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);

        final BlockCompareView blockCompareView = new BlockCompareView();
        final List<LogicBlockViewLine> viewLines = new ArrayList<>();
        final LogikBlock block1 = problem.getBlocks().get(blockId1);
        final LogikBlock block2 = problem.getBlocks().get(blockId2);

        LogicBlockViewLine blockLine = LogicBlockViewBuilder.buildBlockViewLine(block1);
        viewLines.add(blockLine);
        for (LogikLine line : block1.getMainLines()) {
            LogicBlockViewLine viewLine = LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), block1, line);
            viewLines.add(viewLine);
        }

        LogicBlockViewLine separatorLine = LogicBlockViewBuilder.buildMainSeparatorLine(block1);
        viewLines.add(separatorLine);

        blockLine = LogicBlockViewBuilder.buildBlockViewLine(block2);
        viewLines.add(blockLine);
        for (LogikLine line : block2.getMainLines()) {
            LogicBlockViewLine viewLine = LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), block2, line);
            viewLines.add(viewLine);
        }

        separatorLine = LogicBlockViewBuilder.buildMainSeparatorLine(block2);
        viewLines.add(separatorLine);

        List<Integer> block1LineIds = block2.getMainLines().stream().map(LogikLine::getLineId).collect(Collectors.toList());
        List<Integer> block2LineIds = block1.getMainLines().stream().map(LogikLine::getLineId).collect(Collectors.toList());

        List<Integer> sameLines = new ArrayList<>(block1LineIds);
        sameLines.removeIf(o -> !block2LineIds.contains(o));

        int size = block1LineIds.size();
        int reducedSize = block1LineIds.size() - sameLines.size();
        boolean[][] proposed = new boolean[reducedSize][reducedSize];


        int rowIndex = 0;
        for (int i = 0; i < size; i++) {
            LogikLine line1 = block1.getMainLines().get(i);
            if (!sameLines.contains(line1.getLineId())) {
                int colIndex = 0;

                for (int j = 0; j < size; j++) {
                    LogikLine line2 = block2.getMainLines().get(j);
                    if (!sameLines.contains(line2.getLineId())) {
                        boolean propose = !isExcluded(problem.getGroups(), line1, line2);
                        proposed[rowIndex][colIndex] = propose;
                        colIndex++;
                    }
                }
                rowIndex++;
            }
        }

        blockCompareView.setViewLines(viewLines);
        block1LineIds.removeAll(sameLines);
        blockCompareView.setBlock1LineIds(block1LineIds);
        block2LineIds.removeAll(sameLines);
        blockCompareView.setBlock2LineIds(block2LineIds);
        blockCompareView.setProposed(proposed);
        return blockCompareView;
    }

    private boolean isExcluded(List<LogikGroup> groups, LogikLine line1, LogikLine line2) {
        // Check if some group excludes both lines
        for (LogikGroup group : groups) {
            int counter = 0;
            List<LogikElement> selectables = line1.getSelectables(group);
            List<LogikElement> findSelectables = line2.getSelectables(group);
            for (LogikElement element : selectables) {
                if (findSelectables.contains(element)) {
                    counter++;
                    break;
                }
            }
            if (counter == 0) {
                return true;
            }
        }
        return false;
    }

    @GetMapping(path = "/problems/{problemKey}/view/multiple")
    public List<LogicBlockViewLine> getMultipleRelations(@PathVariable String problemKey) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);

        MultipleRelationBuilder multipleRelationBuilder = new MultipleRelationBuilder(problem);
        return multipleRelationBuilder.build();
    }

}
