package de.sarux.logik.helper.application;

import de.sarux.logik.helper.problem.util.MergeLines;
import de.sarux.logik.helper.problem.util.UpdateSelectionInput;
import de.sarux.logik.helper.problem.solve.ChangeResult;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;
import de.sarux.logik.helper.problem.solve.SolveHelper;
import de.sarux.logik.helper.problem.view.group.BlockingCandidates;
import de.sarux.logik.helper.problem.view.group.GroupViewHandler;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "http://localhost:4200")
public class SolverController {

    public static final String SOLVE_VIEW_NAME = "solve";
    private final GeneralLogikProblemBean problemBean;

    // standard constructors
    @Autowired
    public SolverController(GeneralLogikProblemBean problemBean) {
        this.problemBean = problemBean;
    }

    @PutMapping("/problems/{problemKey}/selection")
    boolean updateSelection(@PathVariable String problemKey, @RequestBody UpdateSelectionInput updateSelectionInput) {
        GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);
        LogikLine line = problem.getLine(updateSelectionInput.getLineId());
        LogikGroup group = problem.getGroup(updateSelectionInput.getGroupId());
        final List<LogikElement> selectedElements = new ArrayList<>();
        for (Integer index : updateSelectionInput.getSelection()) {
            selectedElements.add(group.getElements().get(index));
        }

        problem.updateSelection(line, group, selectedElements);
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/show")
    boolean showBlock(@PathVariable String problemKey, @RequestBody Integer blockGroupId) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        final LogikOptionBlockGroup blockGroup = problem.findBlockGroup(blockGroupId);
        blockGroup.setHide(false);
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/hide")
    boolean hideBlock(@PathVariable String problemKey, @RequestBody Integer blockGroupId) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        LogikOptionBlockGroup blockGroup = problem.findBlockGroup(blockGroupId);
        blockGroup.setHide(true);
        return true;
    }

    @PutMapping("/problems/{problemKey}/negative")
    ChangeResult findNegatives(@PathVariable String problemKey, @RequestBody Integer lineId) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
  
        LogikLine line = problem.getLine(lineId);
        SolveHelper helper = new SolveHelper(problem);
        ChangeResult result = helper.findNegatives(line);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/problems/{problemKey}/positive")
    ChangeResult findPositives(@PathVariable String problemKey, @RequestBody List<Integer> lineIds) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
      
        LogikLine line = problem.getLine(lineIds.get(0));
        SolveHelper helper = new SolveHelper(problem);

        List<LogikLine> mergeLines = null;
        if (lineIds.size() > 1) {
            mergeLines = new ArrayList<>(lineIds.size() - 1);
            for (int i = 1; i < lineIds.size(); i++) {
                mergeLines.add(problem.getLine(lineIds.get(i)));
            }
        }

        ChangeResult result = helper.findPositives(line, mergeLines);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/problems/{problemKey}/merge")
    boolean merge(@PathVariable String problemKey, @RequestBody MergeLines mergeLines) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
      
        LogikLine line1 = problem.getLine(mergeLines.getLine1Id());
        LogikLine line2 = problem.getLine(mergeLines.getLine2Id());
        SolveHelper helper = new SolveHelper(problem);
        boolean result = helper.mergeLines(line1, line2);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/problems/{problemKey}/blocking_candidates")
    boolean applyBlockingCandidates(@PathVariable String problemKey, @RequestBody BlockingCandidates blockingCandidates) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
 
        GroupViewHandler solveHelper = new GroupViewHandler(problem);
        return solveHelper.excludeBlockingCandidates(blockingCandidates);
    }
}
