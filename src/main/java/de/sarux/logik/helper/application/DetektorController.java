package de.sarux.logik.helper.application;

import de.sarux.logik.helper.application.detektor.*;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.application.group.LogikGroupsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("detektor")
@CrossOrigin(origins = "http://localhost:4200")
public class DetektorController {
    public static final String DETEKTOR_VIEW_NAME = "detektor";
    private final DetektorBean detektorBean;
    private final ProblemViewBean problemViewBean;
    private final ProblemBean problemBean;
    private final LogikGroupsBean logikGroupsBean;

    // standard constructors
    @Autowired
    public DetektorController(DetektorBean detektorBean, ProblemViewBean problemViewBean, LogikGroupsBean logikGroupsBean, ProblemBean problemBean) {
        this.detektorBean = detektorBean;
        this.problemBean = problemBean;
        this.problemViewBean = problemViewBean;
        this.logikGroupsBean = logikGroupsBean;
    }

    @GetMapping("/lines")
    public LogicBlockView getLines() {
        return problemViewBean.getView(DETEKTOR_VIEW_NAME);
    }

    @PutMapping("/selection")
    boolean updateSelection(@RequestBody UpdateSelectionInput updateSelectionInput) {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogicBlockView view = problemViewBean.getView(DETEKTOR_VIEW_NAME);
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
        detektorBean.init(logikGroupsBean.getGroups());
        problemViewBean.initView(DETEKTOR_VIEW_NAME, logikGroupsBean.getGroups());
        return true;
    }

    @PutMapping("/block/new")
    boolean updateSelection(@RequestBody NewBlockInput newBlockInput) {
        final LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikBlock block = detektorBean.getCurrentProblem().newBlock(newBlockInput.getBlockName());
        if (newBlockInput.isNoDuplicates() || newBlockInput.getGroupId() != null)
            block.setNoDuplicates(true);

        LogikLine newLine = (newBlockInput.getGroupId() != null) ? null : problem.newMainLine(block);

        problemViewBean.getView(DETEKTOR_VIEW_NAME).newBlock(detektorBean.getCurrentProblem().getGroups(), block, newLine);

        if (newBlockInput.getGroupId() != null) {
            LogikGroup blockGroup = problem.getGroup(newBlockInput.getGroupId());
            for (LogikElement element : blockGroup.getElements()) {
                LogikLine blockLine = problem.newMainLine(block);
                List<LogikElement> elements = blockLine.getSelectableElements().get(newBlockInput.getGroupId());
                elements.removeIf(o -> o != element);
                problemViewBean.getView(DETEKTOR_VIEW_NAME).addBlockLine(block, problem.getGroups(), blockLine);
            }
        }
        return true;
    }

    @PutMapping("/blockpair/new")
    boolean addNewPair(@RequestBody NewBlockInput newBlockInput) {
        final LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikBlockPair blockPair = detektorBean.getCurrentProblem().newBlockPair(newBlockInput.getBlockName());

        LogikLine newLine = problem.newMainLine(blockPair.getTrueBlock());
        problemViewBean.getView(DETEKTOR_VIEW_NAME).newBlock(detektorBean.getCurrentProblem().getGroups(), blockPair.getTrueBlock(), newLine);

        newLine = problem.newMainLine(blockPair.getFalseBlock());
        problemViewBean.getView(DETEKTOR_VIEW_NAME).newBlock(detektorBean.getCurrentProblem().getGroups(), blockPair.getFalseBlock(), newLine);
        return true;
    }

    @PutMapping("/relation/new")
    boolean newRelation(@RequestBody NewRelationInput newRelationInput) throws LogikException {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikBlock block = problem.findBlock(newRelationInput.getBlockId());
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
            problemViewBean.getView(DETEKTOR_VIEW_NAME).updateRelation(block, relatedRelations);
        } else {
            if (type != LogikRelationType.NONE) {
                LogikLine fromLine = problem.getLastMainLine(block);
                LogikLine toLine;
                if (isSubLine)
                    toLine = problem.newSubLine(block);
                else
                    toLine = problem.newMainLine(block);
                LogikLineRelation newRelation = block.newRelation(fromLine, groupFrom, toLine, groupTo, type, value, isSubLine);
                problemViewBean.getView(DETEKTOR_VIEW_NAME).newRelation(block, problem.getGroups(), newRelation);
            } else {
                if (isSubLine)
                    throw new LogikException("Eine Nebenbedingung muss auch eine richtige Beziehung haben!");
                LogikLine toLine = problem.newMainLine(block);
                problemViewBean.getView(DETEKTOR_VIEW_NAME).addBlockLine(block, problem.getGroups(), toLine);
            }
        }
        return true;
    }

    @PutMapping("/block/flip")
    boolean flipBlock(@RequestBody Integer blockId) throws LogikException {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikBlock block = problem.findBlock(blockId);
        block.flip();
        problemViewBean.getView(DETEKTOR_VIEW_NAME).flipBlock(problem.getGroups(), block);
        return true;
    }


    @PutMapping("/block/show")
    boolean showBlock(@RequestBody Integer blockId) throws LogikException {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikBlock block = problem.findBlock(blockId);
        problemViewBean.getView(DETEKTOR_VIEW_NAME).showBlock(problem.getGroups(), block);
        return true;
    }

    @PutMapping("/block/hide")
    boolean hideBlock(@RequestBody Integer blockId) {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikBlock block = problem.findBlock(blockId);
        problemViewBean.getView(DETEKTOR_VIEW_NAME).hideBlock(block);
        return true;
    }

    @PutMapping("/negative")
    ChangeResult findNegatives(@RequestBody Integer lineId) {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();
        LogikLine line = problem.getLine(lineId);
        DetektorHelper helper = new DetektorHelper(problem, problemViewBean.getView(DETEKTOR_VIEW_NAME));
        return helper.findNegatives(line);
    }

    @GetMapping("/view/combination")
    public CombinationView getView() {
        final List<String> blockIds = new ArrayList<>();
        final List<List<Boolean>> truths = new ArrayList<>();

        final LogikDetektorProblem detektorProblem = detektorBean.getCurrentProblem();

        for (LogikBlockPair pair : detektorProblem.getBlockPairs()) {
            LogikBlock trueBlock = pair.getTrueBlock();
            String name = trueBlock.getName().substring(0, trueBlock.getName().length() - 5);
            blockIds.add(name);
        }

        for (List<LogikBlock> combination : detektorProblem.getPossibleCombinations()) {
            List<Boolean> booleanCombination = new ArrayList<>();
            for (int i = 0; i < combination.size(); i++) {

                LogikBlockPair pair = detektorProblem.getBlockPairs().get(i);
                booleanCombination.add(combination.get(i) == pair.getTrueBlock());


            }
            truths.add(booleanCombination);
        }
        return new CombinationView(blockIds, truths);
    }

    @PutMapping("/prepare")
    public void prepare(@RequestBody List<Integer> blockIds) throws LogikException {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();

        List<LineSearchResult> results = new ArrayList<>();
        for (Integer blockId : blockIds) {
            results.add(problem.searchBlock(blockId));
        }

        // Check if all results found something
        boolean allFound = results.stream().allMatch(o -> o.isFromTrue() || o.getPairIndex() > -1);
        if (!allFound)
            throw new LogikException("Nicht alle Zeilen wurden gefunden!");

        long duplicates = results.stream().filter(o -> !o.isFromTrue()).collect(Collectors.groupingBy(LineSearchResult::getPairIndex)).entrySet().stream().filter(e -> e.getValue().size() > 1).count();
        if (duplicates > 0)
            throw new LogikException("Bitte pro Paar nur eine Ausprägung wählen!");

        List<LineSearchResult> sortedResults = results.stream().filter(o -> !o.isFromTrue() && o.getPairIndex() > -1).sorted(Comparator.comparing(LineSearchResult::getPairIndex)).collect(Collectors.toList());

        problemBean.init(problem.getGroups());
        for (LogikBlock block : problem.getTrueBlocks()) {
            copyBlock(problemBean, block);
        }

        for (LineSearchResult result : sortedResults) {
            LogikBlockPair pair = detektorBean.getCurrentProblem().getBlockPairs().get(result.getPairIndex());
            copyBlock(problemBean, (result.isPairTruth()) ? pair.getTrueBlock() : pair.getFalseBlock());
        }

        // TODO: Key
        problemViewBean.buildProblemView(problemBean.getProblem("solve0"));
    }

    @PutMapping("/exclude")
    public ChangeResult exclude(@RequestBody List<Integer> blockIds) throws LogikException {
        LogikDetektorProblem problem = detektorBean.getCurrentProblem();

        // For compatibility with older problems
        problem.checkBlockNames();
        List<LineSearchResult> results = new ArrayList<>();
        for (Integer blockId : blockIds) {
            results.add(problem.searchBlock(blockId));
        }

        // Check if all results found something
        boolean allFound = results.stream().allMatch(o -> o.isFromTrue() || o.getPairIndex() > -1);
        if (!allFound)
            throw new LogikException("Nicht alle Zeilen wurden gefunden!");

        long duplicates = results.stream().filter(o -> !o.isFromTrue()).collect(Collectors.groupingBy(LineSearchResult::getPairIndex)).entrySet().stream().filter(e -> e.getValue().size() > 1).count();
        if (duplicates > 0)
            throw new LogikException("Bitte pro Paar nur eine Ausprägung wählen!");

        List<LineSearchResult> sortedResults = results.stream().filter(o -> !o.isFromTrue() && o.getPairIndex() > -1).sorted(Comparator.comparing(LineSearchResult::getPairIndex)).collect(Collectors.toList());

        List<List<LogikBlock>> removedCombinations = new ArrayList<>();
        List<List<LogikBlock>> possibleCombinations = problem.getPossibleCombinations();
        for (List<LogikBlock> combination : possibleCombinations) {
            if (isRemoved(combination, sortedResults))
                removedCombinations.add(combination);
        }

        possibleCombinations.removeAll(removedCombinations);
        ChangeResult result = new ChangeResult();
        result.getRemoveMessages().add("Übrig: " + possibleCombinations.size());
        List<LogikBlock> firstCombination = possibleCombinations.get(0);
        int length = firstCombination.size();
        boolean changed = false;
        for (int i = 0; i < length; i++) {
            boolean allEqual = true;
            LogikBlock firstBlock = firstCombination.get(i);

            for (int j = 1; j < possibleCombinations.size(); j++) {
                if (possibleCombinations.get(j).get(i) != firstBlock) {
                    allEqual = false;
                    break;
                }
            }
            if (allEqual) {
                result.getRemoveMessages().add("Block " + firstBlock.getName() + " übernommen!");
                problem.moveLogikBlock(i, firstBlock);
                length--;
                i--;
                changed = true;
            }
        }

        if (changed) {
            problemViewBean.buildDetektorView(problem);
        }
        return result;
    }

    private boolean isRemoved(List<LogikBlock> combination, List<LineSearchResult> sortedResults) {
        for (LineSearchResult result : sortedResults) {
            LogikBlock block = combination.get(result.getPairIndex());
            LogikBlockPair pair = detektorBean.getCurrentProblem().getBlockPairs().get(result.getPairIndex());
            LogikBlock compareBlock = result.isPairTruth() ? pair.getTrueBlock() : pair.getFalseBlock();
            if (block != compareBlock) return false;
        }
        return true;
    }

    private void copyBlock(ProblemBean problem, LogikBlock block) {
        Map<LogikLine, LogikLine> copiedLines = new HashMap<>();
        // TODO
        LogikBlock copiedBLock = problemBean.getProblem("solve0").newBlock(block.getName());
        for (LogikLine line : block.getMainLines()) {
            if (!copiedLines.containsKey(line)) {
                // TODO
                LogikLine logikLine = problemBean.getProblem("solve0").newMainLine(copiedBLock);
                logikLine.copyFrom(line);
                copiedLines.put(line, logikLine);
            } else {
                copiedBLock.addMainLine(copiedLines.get(line));
            }
        }

        for (LogikLine line : block.getSubLines()) {
            if (!copiedLines.containsKey(line)) {
                // TODO
                LogikLine logikLine = problemBean.getProblem("solve0").newSubLine(copiedBLock);
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