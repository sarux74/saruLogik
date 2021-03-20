package de.sarux.logik.helper.application;

import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.application.group.LogikGroupsBean;
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

    @GetMapping("/problems/{problemKey}/view")
    public LogicBlockView getLines(@PathVariable String problemKey) {
        return problemViewBean.getView(SOLVE_VIEW_NAME + problemKey);
    }

    @PutMapping("/problems/{problemKey}/selection")
    boolean updateSelection(@PathVariable String problemKey, @RequestBody UpdateSelectionInput updateSelectionInput) {
        LogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);
        LogicBlockView view = problemViewBean.getView(SOLVE_VIEW_NAME + problemKey);
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
        problemViewBean.initView(SOLVE_VIEW_NAME + 0, logikGroupsBean.getGroups());
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/new")
    boolean newBlock(@PathVariable String problemKey, @RequestBody NewBlockInput newBlockInput) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikBlock block = problem.newBlock(newBlockInput.getBlockName());
        if (newBlockInput.isNoDuplicates() || newBlockInput.getGroupId() != null)
            block.setNoDuplicates(true);

        LogikLine newLine = (newBlockInput.getGroupId() != null) ? null : problem.newMainLine(block);

        view.newBlock(problem.getGroups(), block, newLine);

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
                view.addBlockLine(block, problem.getGroups(), blockLine);
            }
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/relation/new")
    boolean newRelation(@PathVariable String problemKey, @RequestBody NewRelationInput newRelationInput) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

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
            } else if (type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLine(newRelationInput.getLeftLineId());
                LogikLine toLine = problem.getLine(newRelationInput.getRightLineId());
                LogikLineRelation newRelation = block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
                relatedRelations.add(newRelation);
            }
            view.updateRelation(block, relatedRelations);
        } else {
            if (type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLastMainLine(block);
                LogikLine toLine;
                if (isSubLine)
                    toLine = problem.newSubLine(block);
                else
                    toLine = problem.newMainLine(block);
                LogikLineRelation newRelation = block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
                view.newRelation(block, problem.getGroups(), newRelation);
            } else {
                if (isSubLine)
                    throw new LogikException("Eine Nebenbedingung muss auch eine richtige Beziehung haben!");
                LogikLine toLine = problem.newMainLine(block);
                view.addBlockLine(block, problem.getGroups(), toLine);
            }
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/flip")
    boolean flipBlock(@PathVariable String problemKey, @RequestBody Integer blockId) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        final LogikBlock block = problem.getBlocks().get(blockId);
        block.flip();
        view.flipBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/lineup")
    boolean lineUp(@PathVariable String problemKey, @RequestBody BlockMove blockMove) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikBlock block = problem.getBlocks().get(blockMove.getBlockId());
        if (!block.isNoDuplicates())
            throw new LogikException("Für nicht-singuläre Blöcke ist kein Verschieben vorgesehen!");

        LogikLine line = problem.getLine(blockMove.getLineId());
        int oldIndex = block.getMainLines().indexOf(line);
        if (oldIndex > 0) {
            block.getMainLines().remove(oldIndex);
            block.getMainLines().add(oldIndex - 1, line);
        }
        view.hideBlock(block);
        view.showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/linedown")
    boolean lineDown(@PathVariable String problemKey, @RequestBody BlockMove blockMove) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikBlock block = problem.getBlocks().get(blockMove.getBlockId());
        if (!block.isNoDuplicates())
            throw new LogikException("Für nicht-singuläre Blöcke ist kein Verschieben vorgesehen!");

        LogikLine line = problem.getLine(blockMove.getLineId());
        int oldIndex = block.getMainLines().indexOf(line);
        if (oldIndex > -1 && oldIndex < block.getMainLines().size() - 1) {
            block.getMainLines().remove(oldIndex);
            block.getMainLines().add(oldIndex + 1, line);
        }
        view.hideBlock(block);
        view.showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/show")
    boolean showBlock(@PathVariable String problemKey, @RequestBody Integer blockId) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikBlock block = problem.getBlocks().get(blockId);
        view.showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/problems/{problemKey}/block/hide")
    boolean hideBlock(@PathVariable String problemKey, @RequestBody Integer blockId) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikBlock block = problem.getBlocks().get(blockId);
        view.hideBlock(block);
        return true;
    }

    @PutMapping("/problems/{problemKey}/negative")
    ChangeResult findNegatives(@PathVariable String problemKey, @RequestBody Integer lineId) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikLine line = problem.getLine(lineId);
        SolveHelper helper = new SolveHelper(problem, view);
        ChangeResult result = helper.findNegatives(line);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/problems/{problemKey}/positive")
    ChangeResult findPositives(@PathVariable String problemKey, @RequestBody List<Integer> lineIds) throws LogikException {

        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikLine line = problem.getLine(lineIds.get(0));
        SolveHelper helper = new SolveHelper(problem, view);

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

    @PutMapping("/problems/{problemKey}/merge")
    boolean merge(@PathVariable String problemKey, @RequestBody MergeLines mergeLines) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        LogikLine line1 = problem.getLine(mergeLines.getLine1Id());
        LogikLine line2 = problem.getLine(mergeLines.getLine2Id());
        SolveHelper helper = new SolveHelper(problem, view);
        boolean result = helper.mergeLines(line1, line2);
        helper.checkSolvability(problem);
        return result;
    }

    @PutMapping("/problems/{problemKey}/blocking_candidates")
    boolean applyBlockingCandidates(@PathVariable String problemKey, @RequestBody BlockingCandidates blockingCandidates) throws LogikException {
        int groupId = blockingCandidates.getGroupId();
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);
        LogicBlockView view = problemViewBean.getView(problemName);

        SolveHelper solveHelper = new SolveHelper(problem, view);
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
                    changes |= removeOthers(problem, view, line, collectedElements);
                    break;
                case EXCLUDE:
                    changes |= removeCandidates(problem, view, line, collectedElements);
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        return changes;
    }


    @PutMapping("/problems/{problemKey}/case/new")
    String newCase(@PathVariable String problemKey, @RequestBody MergeLines mergeLines) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);

        LogikProblem copyProblem = copyProblem(problem);
        LogicBlockView copyView = problemViewBean.buildView(copyProblem.getGroups(), copyProblem.getBlocks());

        LogikLine line1 = copyProblem.getLine(mergeLines.getLine1Id());
        LogikLine line2 = problem.getLine(mergeLines.getLine2Id());
        SolveHelper helper = new SolveHelper(copyProblem, copyView);
        helper.mergeLines(line1, line2);
        helper.checkSolvability(problem);

        copyProblem.setCaseData(problemKey, mergeLines.getLine1Id(), mergeLines.getLine2Id());
        String key = problemBean.newKey();
        String copyProblemKey = "solve" + key;

        problemBean.addProblem(copyProblemKey, copyProblem);
        problemViewBean.addProblemView(copyProblemKey, copyView);

        return key;
    }

    @PutMapping("/problems/{problemKey}/case/close")
    boolean closeCase(@PathVariable String problemKey) throws LogikException {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        LogikProblem problem = problemBean.getProblem(problemName);

        LogikProblem parentProblem = problemBean.getProblem(SOLVE_VIEW_NAME + problem.getParentProblemKey());
        LogicBlockView parentView = problemViewBean.getView(SOLVE_VIEW_NAME + problem.getParentProblemKey());
        LogikLine line1 = parentProblem.getLine(problem.getLine1Id());
        LogikLine line2 = parentProblem.getLine(problem.getLine2Id());

        for(LogikGroup group : problem.getGroups()) {
            List<LogikElement> selectables1 = line1.getSelectables(group);
            List<LogikElement> selectables2 = line2.getSelectables(group);

            if(selectables1.size() == 1) {
                selectables2.remove(selectables1.get(0));
                parentView.updateSelection(line2, group, selectables2);
            }
            if(selectables2.size() == 1){
                selectables1.remove(selectables2.get(0));
                parentView.updateSelection(line1, group, selectables1);
            }
        }

        problemBean.removeProblem(problemName);
        problemViewBean.removeView(problemName);

        return true;
    }

    private LogikProblem copyProblem(LogikProblem problem) {
        Map<LogikLine, LogikLine> copiedLines = new HashMap<>();
        LogikProblem copyProblem = new LogikProblem(problem.getGroups());
        for(LogikBlock block: problem.getBlocks()) {

            LogikBlock copiedBLock = copyProblem.newBlock(block.getName());
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
        return copyProblem;
    }

    private boolean removeOthers(LogikProblem problem, LogicBlockView view, LogikLine line, List<Set<LogikElement>> collectedElements) {
        boolean changes = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> candidateElements = collectedElements.get(i);
            if (candidateElements != null) {
                boolean singleChange = line.getSelectableElements().get(i).removeIf(o -> !candidateElements.contains(o));

                if (singleChange) {
                    final LogikGroup group = problem.getGroup(i);
                    view.updateSelection(line, group, line.getSelectables(group));
                }
                changes |= singleChange;
            }
        }
        return changes;
    }

    private boolean removeCandidates(LogikProblem problem, LogicBlockView view, LogikLine line, List<Set<LogikElement>> collectedElements) {
        boolean changes = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> candidateElements = collectedElements.get(i);
            if (candidateElements != null) {
                boolean singleChange = line.getSelectableElements().get(i).removeIf(candidateElements::contains);
                if (singleChange) {
                    final LogikGroup group = problem.getGroup(i);
                    view.updateSelection(line, group, line.getSelectables(group));
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