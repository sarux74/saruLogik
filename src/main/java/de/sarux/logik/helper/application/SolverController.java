package de.sarux.logik.helper.application;

import de.sarux.logik.helper.*;
import de.sarux.logik.helper.group.LogikGroup;
import de.sarux.logik.helper.group.LogikGroupsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("solve")
@CrossOrigin(origins = "http://localhost:4200")
public class SolverController {
    public static final String SOLVE_VIEW_NAME = "solve";
    private final ProblemBean problemBean;
    private final ProblemViewBean problemViewBean;
    private final LogikGroupsBean logikGroupsBean;

    // standard constructors
    @Autowired
    public SolverController(ProblemBean problemBean, ProblemViewBean problemViewBean, LogikGroupsBean logikGroupsBean) {
        this.problemBean = problemBean;
        this.problemViewBean = problemViewBean;
        this.logikGroupsBean = logikGroupsBean;
    }

    @GetMapping("/lines")
    public LogicBlockView getLines() {
        return problemViewBean.getView(SOLVE_VIEW_NAME);
    }

    @PutMapping("/selection")
    boolean updateSelection(@RequestBody UpdateSelectionInput updateSelectionInput) {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogicBlockView view = problemViewBean.getView("solve");
        LogikLine line = problem.getLine(updateSelectionInput.getLineId());
        LogikGroup group = problem.getGroup(updateSelectionInput.getGroupId());
        final List<LogikElement> selectedElements = new ArrayList<>();
        for (Integer index : updateSelectionInput.getSelection()) {
            selectedElements.add(group.getElements().get(index));
        }

        problem.updateSelection(line, group, selectedElements);
        view.updateSelection(line, group, selectedElements);
        return true;
    }

    @PutMapping("/new")
    boolean newProblem() {
        problemBean.init(logikGroupsBean.getGroups());
        problemViewBean.initView(SOLVE_VIEW_NAME, logikGroupsBean.getGroups());
        return true;
    }

    @PutMapping("/block/new")
    boolean newBlock(@RequestBody NewBlockInput newBlockInput) {
        final LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problemBean.getCurrentProblem().newBlock(newBlockInput.getBlockName());
        if (newBlockInput.isNoDuplicates() || newBlockInput.getGroupId() != null)
            block.setNoDuplicates(true);

        LogikLine newLine = (newBlockInput.getGroupId() != null) ? null : problem.newMainLine(block);

        problemViewBean.getView(SOLVE_VIEW_NAME).newBlock(problemBean.getCurrentProblem().getGroups(), block, newLine);

        if (newBlockInput.getGroupId() != null) {
            LogikGroup blockGroup = problem.getGroup(newBlockInput.getGroupId());
            for (LogikElement element : blockGroup.getElements()) {
                LogikLine blockLine = problem.newMainLine(block);
                List<LogikElement> elements = blockLine.getSelectableElements().get(newBlockInput.getGroupId());
                elements.removeIf(o -> o != element);
                if (newBlockInput.isExcludeSameShortNames()) {
                    int groupId = 0;
                    for (List<LogikElement> otherElements : blockLine.getSelectableElements()) {
                        if (groupId != newBlockInput.getGroupId()) {
                            otherElements.removeIf(o -> o.getShortName().equals(element.getShortName()));
                        }
                        groupId++;
                    }
                }
                problemViewBean.getView(SOLVE_VIEW_NAME).addBlockLine(block, problem.getGroups(), blockLine);
            }
        }
        return true;
    }

    @PutMapping("/relation/new")
    boolean newRelation(@RequestBody NewRelationInput newRelationInput) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problem.getBlocks().get(newRelationInput.getBlockId());
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
            } else if(type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLine(newRelationInput.getLeftLineId());
                LogikLine toLine = problem.getLine(newRelationInput.getRightLineId());
                LogikLineRelation newRelation = block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
                relatedRelations.add(newRelation);
            }
            problemViewBean.getView(SOLVE_VIEW_NAME).updateRelation(block, relatedRelations);
        } else {
            if (type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLastMainLine(block);
                LogikLine toLine;
                if (isSubLine)
                    toLine = problem.newSubLine(block);
                else
                    toLine = problem.newMainLine(block);
                LogikLineRelation newRelation = block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
                problemViewBean.getView(SOLVE_VIEW_NAME).newRelation(block, problem.getGroups(), newRelation);
            } else {
                if (isSubLine)
                    throw new LogikException("Eine Nebenbedingung muss auch eine richtige Beziehung haben!");
                LogikLine toLine = problem.newMainLine(block);
                problemViewBean.getView(SOLVE_VIEW_NAME).addBlockLine(block, problem.getGroups(), toLine);
            }
        }
        return true;
    }

    @PutMapping("/block/flip")
    boolean flipBlock(@RequestBody Integer blockId) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problem.getBlocks().get(blockId);
        block.flip();
        problemViewBean.getView(SOLVE_VIEW_NAME).flipBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/block/lineup")
    boolean lineUp(@RequestBody BlockMove blockMove) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problem.getBlocks().get(blockMove.getBlockId());
        if (!block.isNoDuplicates())
            throw new LogikException("Für nicht-singuläre Blöcke ist kein Verschieben vorgesehen!");

        LogikLine line = problem.getLine(blockMove.getLineId());
        int oldIndex = block.getMainLines().indexOf(line);
        if (oldIndex > 0) {
            block.getMainLines().remove(oldIndex);
            block.getMainLines().add(oldIndex - 1, line);
        }
        problemViewBean.getView(SOLVE_VIEW_NAME).hideBlock(block);
        problemViewBean.getView(SOLVE_VIEW_NAME).showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/block/linedown")
    boolean lineDown(@RequestBody BlockMove blockMove) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problem.getBlocks().get(blockMove.getBlockId());
        if (!block.isNoDuplicates())
            throw new LogikException("Für nicht-singuläre Blöcke ist kein Verschieben vorgesehen!");

        LogikLine line = problem.getLine(blockMove.getLineId());
        int oldIndex = block.getMainLines().indexOf(line);
        if (oldIndex > -1 && oldIndex < block.getMainLines().size() - 1) {
            block.getMainLines().remove(oldIndex);
            block.getMainLines().add(oldIndex + 1, line);
        }
        problemViewBean.getView(SOLVE_VIEW_NAME).hideBlock(block);
        problemViewBean.getView(SOLVE_VIEW_NAME).showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/block/show")
    boolean showBlock(@RequestBody Integer blockId) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problem.getBlocks().get(blockId);
        problemViewBean.getView(SOLVE_VIEW_NAME).showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/block/hide")
    boolean hideBlock(@RequestBody Integer blockId) {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikBlock block = problem.getBlocks().get(blockId);
        problemViewBean.getView(SOLVE_VIEW_NAME).hideBlock(block);
        return true;
    }

    @PutMapping("/negative")
    ChangeResult findNegatives(@RequestBody Integer lineId) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikLine line = problem.getLine(lineId);
        SolveHelper helper = new SolveHelper(problem, problemViewBean.getView(SOLVE_VIEW_NAME));
        ChangeResult result = helper.findNegatives(line);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/positive")
    ChangeResult findPositives(@RequestBody List<Integer> lineIds) throws LogikException {

        LogikProblem problem = problemBean.getCurrentProblem();
        LogikLine line = problem.getLine(lineIds.get(0));
        SolveHelper helper = new SolveHelper(problem, problemViewBean.getView(SOLVE_VIEW_NAME));

        List<LogikLine> mergeLines = null;
        if (lineIds.size() > 1) {
            mergeLines = new ArrayList<>(lineIds.size() - 1);
            for (int i = 1; i < lineIds.size(); i++)
                mergeLines.add(problem.getLine(lineIds.get(i)));
        }

        ChangeResult result = helper.findPositives(line, mergeLines);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/merge")
    boolean merge(@RequestBody MergeLines mergeLines) throws LogikException {
        LogikProblem problem = problemBean.getCurrentProblem();
        LogikLine line1 = problem.getLine(mergeLines.getLine1Id());
        LogikLine line2 = problem.getLine(mergeLines.getLine2Id());
        SolveHelper helper = new SolveHelper(problem, problemViewBean.getView(SOLVE_VIEW_NAME));
        boolean result = helper.mergeLines(line1, line2);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/blocking_candidates")
    boolean applyBlockingCandidates(@RequestBody BlockingCandidates blockingCandidates) throws LogikException {
        int groupId = blockingCandidates.getGroupId();
        LogikProblem problem = problemBean.getCurrentProblem();
        SolveHelper solveHelper = new SolveHelper(problemBean.getCurrentProblem(), problemViewBean.getView(SOLVE_VIEW_NAME));
        List<LogicBlockViewLine> groupLines = solveHelper.buildGroupViewLines(blockingCandidates.getGroupId());
        List<Set<LogikElement>> collectedElements = new ArrayList<>();
        for (int i = 0; i < problem.getGroups().size(); i++) {
            collectedElements.add(new HashSet<>());
        }
        List<LogicBlockViewLine> reducedLines = new ArrayList<>();
        for (int index : blockingCandidates.getSelectedLines())
            reducedLines.add(groupLines.get(index));

        for (LogicBlockViewLine line : reducedLines) {
            for (int i = 0; i < problem.getGroups().size(); i++) {
                int fixedIndex = fixIndex(i, groupId);
                LogikGroup group = problem.getGroup(fixedIndex);
                List<Integer> viewIndizes = line.getView().get(i).getSelectableValues();
                Set<LogikElement> elements = collectedElements.get(fixedIndex);
                viewIndizes.forEach(o -> elements.add(group.getElements().get(o)));
            }
        }
        for (int i = 0; i < collectedElements.size(); i++) {
            if (collectedElements.get(i).size() > reducedLines.size()) {
                collectedElements.remove(i);
                collectedElements.add(i, null);
            }
        }
        boolean changes = false;
        for (LogikLine line : problem.getLines()) {
            BlockingType type = findBlockingType(line, collectedElements);
            switch (type) {
                case IMPOSSIBLE:
                    throw new LogikException("Widersprüchlicher BlockingType");
                case INCLUDE:
                    changes |= removeOthers(line, collectedElements);
                    break;
                case EXCLUDE:
                    changes |= removeCandidates(line, collectedElements);
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        return changes;
    }

    private boolean removeOthers(LogikLine line, List<Set<LogikElement>> collectedElements) {
        boolean changes = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> candidateElements = collectedElements.get(i);
            if (candidateElements != null) {
                boolean singleChange = line.getSelectableElements().get(i).removeIf(o -> !candidateElements.contains(o));

                if (singleChange) {
                    final LogikGroup group = problemBean.getCurrentProblem().getGroup(i);
                    problemViewBean.getView(SOLVE_VIEW_NAME).updateSelection(line, group, line.getSelectables(group));
                }
                changes |= singleChange;
            }
        }
        return changes;
    }

    private boolean removeCandidates(LogikLine line, List<Set<LogikElement>> collectedElements) {
        boolean changes = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> candidateElements = collectedElements.get(i);
            if (candidateElements != null) {
                boolean singleChange = line.getSelectableElements().get(i).removeIf(o -> candidateElements.contains(o));
                if (singleChange) {
                    final LogikGroup group = problemBean.getCurrentProblem().getGroup(i);
                    problemViewBean.getView(SOLVE_VIEW_NAME).updateSelection(line, group, line.getSelectables(group));
                }
            }
        }
        return changes;
    }

    private BlockingType findBlockingType(LogikLine line, List<Set<LogikElement>> collectedElements) throws LogikException {
        boolean include = false;
        boolean exclude = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> blockingElements = collectedElements.get(i);
            if (blockingElements != null) {
                List<LogikElement> selectableElements = line.getSelectableElements().get(i);
                int countInc = 0;
                int countExc = 0;
                for (LogikElement element : selectableElements) {
                    if (blockingElements.contains(element))
                        countInc++;
                    else countExc++;
                }
                if (countInc == 0 && countExc > 0)
                    exclude = true;
                else if (countInc > 0 && countExc == 0)
                    include = true;
                else if (countInc == 0 && countExc == 0)
                    throw new LogikException("Widersprüchliche Vergleiche bei Blocking Candidates");
            }
        }
        if (include && exclude)
            return BlockingType.IMPOSSIBLE;
        else if (include)
            return BlockingType.INCLUDE;
        else if (exclude)
            return BlockingType.EXCLUDE;
        else return BlockingType.UNKNOWN;
    }

    private int fixIndex(int index, int frontId) {
        if (index == 0) return frontId;
        if (index <= frontId) return index - 1;
        return index;
    }
}