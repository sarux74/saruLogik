package de.sarux.logik.helper.application;

import static de.sarux.logik.helper.application.LogikProblemEditController.SOLVE_VIEW_NAME;
import de.sarux.logik.helper.application.detektor.*;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikLineRelation;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;
import de.sarux.logik.helper.problem.solve.ChangeResult;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "http://localhost:4200")
public class DetektorController {

    private final GeneralLogikProblemBean problemBean;

    // standard constructors
    @Autowired
    public DetektorController(GeneralLogikProblemBean problemBean) {
        this.problemBean = problemBean;
    }

    @GetMapping("/problems/{problemKey}/detektor/view/combination")
    public CombinationView getView(@PathVariable String problemKey) {
        
        final GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);

        return problem.buildCombinationView();
    }

    @PutMapping("/problems/{problemKey}/detektor/prepare")
    public String prepare(@PathVariable String problemKey, @RequestBody List<Integer> blockIds) throws LogikException {
        final GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);

        List<LineSearchResult> results = new ArrayList<>();
        for (Integer blockId : blockIds) {
            results.add(problem.searchBlock(blockId));
        }

        // Check if all results found something
        boolean allFound = results.stream().allMatch(o -> o.isFromTrue() || o.getPairIndex() > -1);
        if (!allFound) {
            throw new LogikException("Nicht alle Zeilen wurden gefunden!");
        }

        long duplicates = results.stream().filter(o -> !o.isFromTrue()).collect(Collectors.groupingBy(LineSearchResult::getPairIndex)).entrySet().stream().filter(e -> e.getValue().size() > 1).count();
        if (duplicates > 0) {
            throw new LogikException("Bitte pro Paar nur eine Ausprägung wählen!");
        }

        final GeneralLogikProblem copiedProblem = new GeneralLogikProblem(problem.getGroups());
        for (final LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            if (blockGroup.getOptionBlocks().size() == 1) {
                copyBlock(copiedProblem, blockGroup, blockGroup.getOptionBlocks().get(0));
            } else {
                for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                    if (blockIds.contains(block.getBlockId())) {
                        copyBlock(copiedProblem, blockGroup, block);
                    }
                }
            }
        }
        String newKey = problemBean.newKey();
        problemBean.addProblem(SOLVE_VIEW_NAME + newKey, copiedProblem);
        return newKey;
    }

    @PutMapping("/problems/{problemKey}/detektor/exclude")
    public ChangeResult exclude(@PathVariable String problemKey, @RequestBody List<Integer> blockIds) throws LogikException {
        final GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);

        List<LineSearchResult> results = new ArrayList<>();
        for (Integer blockId : blockIds) {
            results.add(problem.searchBlock(blockId));
        }

        // Check if all results found something
        boolean allFound = results.stream().allMatch(o -> o.isFromTrue() || o.getPairIndex() > -1);
        if (!allFound) {
            throw new LogikException("Nicht alle Zeilen wurden gefunden!");
        }

        long duplicates = results.stream().filter(o -> !o.isFromTrue()).collect(Collectors.groupingBy(LineSearchResult::getPairIndex)).entrySet().stream().filter(e -> e.getValue().size() > 1).count();
        if (duplicates > 0) {
            throw new LogikException("Bitte pro Paar nur eine Ausprägung wählen!");
        }

        List<Integer> sortedResults = results.stream().filter(o -> !o.isFromTrue() && o.getPairIndex() > -1).map(LineSearchResult::getPairIndex).collect(Collectors.toList());

        int rest = problem.excludeCombinations(sortedResults);

        ChangeResult result = new ChangeResult();
        result.getRemoveMessages().add("Übrig: " + rest);

        final List<GeneralLogikBlock> singleBlocks = problem.checkNewSingleBlocksByCombinations();

        for (GeneralLogikBlock newSingle : singleBlocks) {
            final LogikOptionBlockGroup blockGroup = problem.findBlockGroupForBlock(newSingle);
            if (blockGroup != null) {
                result.getRemoveMessages().add("Block " + blockGroup.getName() + " " + newSingle.getSubName() + " übernommen!");
                problem.handleFoundOption(blockGroup, newSingle);
            }
        }

        return result;
    }

    private void copyBlock(GeneralLogikProblem problem, LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        Map<LogikLine, LogikLine> copiedLines = new HashMap<>();
        // TODO

        GeneralLogikBlock copiedBLock = problem.newBlockGroupWithBlock(blockGroup.getName() + " " + block.getSubName());
        for (LogikLine line : block.getMainLines()) {
            if (!copiedLines.containsKey(line)) {
                // TODO
                LogikLine logikLine = problem.newMainLine(copiedBLock);
                logikLine.copyFrom(line);
                copiedLines.put(line, logikLine);
            } else {
                copiedBLock.addMainLine(copiedLines.get(line));
            }
        }

        for (LogikLine line : block.getSubLines()) {
            if (!copiedLines.containsKey(line)) {
                // TODO
                LogikLine logikLine = problem.newSubLine(copiedBLock);
                logikLine.copyFrom(line);
                copiedLines.put(line, logikLine);
            } else {
                copiedBLock.addSubLine(copiedLines.get(line));
            }
        }

        for (LogikLineRelation relation : block.getRelations()) {
            LogikLine leftLine = copiedLines.get(relation.getLeftLine());
            LogikLine rightLine = copiedLines.get(relation.getRightLine());

            copiedBLock.newRelation(leftLine, relation.getLeftGroup(), rightLine, relation.getRightGroup(), relation.getType(), relation.getRelationHint(), relation.isSubRelation());
        }

    }
}
