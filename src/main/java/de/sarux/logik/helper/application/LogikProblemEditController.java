package de.sarux.logik.helper.application;

import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikLineRelation;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;
import de.sarux.logik.helper.problem.LogikRelationType;
import de.sarux.logik.helper.problem.edit.BlockMove;
import de.sarux.logik.helper.problem.edit.NewBlockInput;
import de.sarux.logik.helper.problem.edit.NewRelationInput;
import de.sarux.logik.helper.problem.edit.ProblemEditHandler;
import de.sarux.logik.helper.problem.solve.SolveHelper;
import de.sarux.logik.helper.problem.util.MergeLines;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockView;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewBuilder;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "http://localhost:4200")
public class LogikProblemEditController {

    public static final String SOLVE_VIEW_NAME = "solve";
    private final GeneralLogikProblemBean problemBean;

    // standard constructors
    @Autowired
    public LogikProblemEditController(GeneralLogikProblemBean problemBean) {
        this.problemBean = problemBean;
    }

    @GetMapping("/problems/{problemKey}/view")
    public GeneralLogikBlockView getLines(@PathVariable String problemKey) throws LogikException {
        final GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);
        return GeneralLogikBlockViewBuilder.build(problem);
    }

    @PutMapping("/problems/{problemKey}/block/new")
    boolean newBlock(@PathVariable String problemKey, @RequestBody NewBlockInput newBlockInput) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        final ProblemEditHandler problemEditHandler = new ProblemEditHandler(problem);
        problemEditHandler.newBlock(newBlockInput);
        return true;
    }

    @PutMapping("/problems/{problemKey}/relation/new")
    boolean newRelation(@PathVariable String problemKey, @RequestBody NewRelationInput newRelationInput) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        GeneralLogikBlock block = problem.findBlock(newRelationInput.getBlockId()); //
        LogikGroup groupFrom = problem.getGroup(newRelationInput.getGroupFrom());
        LogikGroup groupTo = problem.getGroup(newRelationInput.getGroupTo());
        LogikRelationType type = LogikRelationType.valueOf(newRelationInput.getRelationType());
        String value = newRelationInput.getRelationHint();
        boolean isSubLine = (newRelationInput.getSubLine() != null && newRelationInput.getSubLine());

        if (newRelationInput.getLeftLineId() != null && newRelationInput.getRightLineId() != null) {
            final List<LogikLineRelation> relatedRelations = block.getRelations().stream().filter(o -> o.getLeftLine().getLineId() == newRelationInput.getLeftLineId() && o.getRightLine().getLineId() == newRelationInput.getRightLineId() && ((isSubLine && o.isSubRelation()) || (!isSubLine && !o.isSubRelation()))).collect(Collectors.toList());

            Optional<LogikLineRelation> relationOpt = relatedRelations.stream().filter(o -> o.getLeftGroup() == groupFrom).findFirst();
            if (relationOpt.isPresent()) {
                LogikLineRelation relation = relationOpt.get();

                if (type == LogikRelationType.NONE) {
                    block.getRelations().remove(relation);
                    relatedRelations.remove(relation);
                } else {
                    relation.setLeftGroup(groupFrom);
                    relation.setRightGroup(groupTo);
                    relation.setType(type);
                    relation.setRelationHint(value);
                }
            } else if (type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLine(newRelationInput.getLeftLineId());
                LogikLine toLine = problem.getLine(newRelationInput.getRightLineId());
                LogikLineRelation newRelation = block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
                relatedRelations.add(newRelation);
            }
        } else {
            if (type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLastMainLine(block);
                LogikLine toLine;
                if (isSubLine) {
                    toLine = problem.newSubLine(block);
                } else {
                    toLine = problem.newMainLine(block);
                }
                block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
            } else {
                if (isSubLine) {
                    throw new LogikException("Eine Nebenbedingung muss auch eine richtige Beziehung haben!");
                }
                problem.newMainLine(block);
            }
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/flip")
    boolean flipBlock(@PathVariable String problemKey, @RequestBody Integer blockId) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        final GeneralLogikBlock block = problem.findBlock(blockId);
        block.flip();
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/lineup")
    boolean lineUp(@PathVariable String problemKey, @RequestBody BlockMove blockMove) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        GeneralLogikBlock block = problem.findBlock(blockMove.getBlockId());
        if (!block.isNoDuplicates()) {
            throw new LogikException("Für nicht-singuläre Blöcke ist kein Verschieben vorgesehen!");
        }

        LogikLine line = problem.getLine(blockMove.getLineId());
        int oldIndex = block.getMainLines().indexOf(line);
        if (oldIndex > 0) {
            block.getMainLines().remove(oldIndex);
            block.getMainLines().add(oldIndex - 1, line);
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/linedown")
    boolean lineDown(@PathVariable String problemKey, @RequestBody BlockMove blockMove) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        GeneralLogikBlock block = problem.findBlock(blockMove.getBlockId());
        if (!block.isNoDuplicates()) {
            throw new LogikException("Für nicht-singuläre Blöcke ist kein Verschieben vorgesehen!");
        }

        LogikLine line = problem.getLine(blockMove.getLineId());
        int oldIndex = block.getMainLines().indexOf(line);
        if (oldIndex > -1 && oldIndex < block.getMainLines().size() - 1) {
            block.getMainLines().remove(oldIndex);
            block.getMainLines().add(oldIndex + 1, line);
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/case/new")
    String newCase(@PathVariable String problemKey, @RequestBody MergeLines mergeLines) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        GeneralLogikProblem copyProblem = copyProblem(problem);

        LogikLine line1 = copyProblem.getLine(mergeLines.getLine1Id());
        LogikLine line2 = problem.getLine(mergeLines.getLine2Id());
        SolveHelper helper = new SolveHelper(copyProblem);
        helper.mergeLines(line1, line2);
        helper.checkSolvability(problem);

        copyProblem.setCaseData(problemKey, mergeLines.getLine1Id(), mergeLines.getLine2Id());
        String key = problemBean.newKey();
        String copyProblemKey = "solve" + key;

        problemBean.addProblem(copyProblemKey, copyProblem);

        return key;
    }

    // TODO: refresh not working
    @PutMapping("/problems/{problemKey}/case/close")
    boolean closeCase(@PathVariable String problemKey) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);

        GeneralLogikProblem parentProblem = problemBean.getProblem(SOLVE_VIEW_NAME + problem.getParentProblemKey());
        LogikLine line1 = parentProblem.getLine(problem.getLine1Id());
        LogikLine line2 = parentProblem.getLine(problem.getLine2Id());

        for (LogikGroup group : problem.getGroups()) {
            List<LogikElement> selectables1 = line1.getSelectables(group);
            List<LogikElement> selectables2 = line2.getSelectables(group);

            if (selectables1.size() == 1) {
                selectables2.remove(selectables1.get(0));
            }
            if (selectables2.size() == 1) {
                selectables1.remove(selectables2.get(0));
            }
        }

        problemBean.removeProblem(problemName);

        return true;
    }

    private GeneralLogikProblem copyProblem(GeneralLogikProblem problem) {
        Map<LogikLine, LogikLine> copiedLines = new HashMap<>();
        GeneralLogikProblem copyProblem = new GeneralLogikProblem(problem.getGroups());
        for (LogikOptionBlockGroup optionGroup : problem.getOptionBlockGroups()) {

            final LogikOptionBlockGroup newOptionGroup = copyProblem.newBlockGroup(optionGroup.getName());
            for (GeneralLogikBlock block : optionGroup.getOptionBlocks()) {

                GeneralLogikBlock copiedBLock = copyProblem.newBlock(newOptionGroup, block.getSubName());
                for (LogikLine line : block.getMainLines()) {
                    if (!copiedLines.containsKey(line)) {
                        LogikLine logikLine = copyProblem.newMainLine(copiedBLock);
                        logikLine.copyFrom(line);
                        copiedLines.put(line, logikLine);
                    } else {
                        copiedBLock.addMainLine(copiedLines.get(line));
                    }
                }

                for (LogikLine line : block.getSubLines()) {
                    if (!copiedLines.containsKey(line)) {
                        LogikLine logikLine = copyProblem.newSubLine(copiedBLock);
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
        return copyProblem;
    }
}
