package de.sarux.logik.helper.application;

import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.application.group.LogikGroup;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SolveHelper {
    private final LogikProblem problem;
    private final LogicBlockView view;

    public SolveHelper(LogikProblem problem, LogicBlockView currentView) {
        this.problem = problem;
        this.view = currentView;
    }

    public ChangeResult findNegatives(LogikLine findLine) {

        ChangeResult result = new ChangeResult();

        // First try to reduce
        for (LogikLine line : problem.getLines()) {
            if (line != findLine) {
                boolean isExcluded = checkExcluded(line, findLine);
                if (isExcluded) {
                    List<LogikElement> singles = findSingles(line);
                    if (!singles.isEmpty())
                        updateExclusion(singles, findLine, result);
                }
            }
        }

        List<LogikElement> singles = findSingles(findLine);
        if (singles.isEmpty())
            return result;


        // Find in lines
        for (LogikLine line : problem.getLines()) {
            if (line != findLine) {
                boolean isExcluded = checkExcluded(line, findLine);
                if (isExcluded) {
                    updateExclusion(singles, line, result);
                }
            }
        }

        // Find in relations
        for (LogikBlock block : problem.getBlocks()) {
            for (LogikLineRelation relation : block.getRelations()) {
                if (relation.getLeftLine() == findLine) {
                    boolean isExcluded = relation.getLeftGroup() == relation.getRightGroup() && checkExcludingRelation(relation.getType());
                    if (isExcluded) {
                        updateExclusion(singles, relation.getRightLine(), result);
                    }
                } else if (relation.getRightLine() == findLine) {
                    boolean isExcluded = relation.getLeftGroup() == relation.getRightGroup() && checkExcludingRelation(relation.getType());
                    if (isExcluded) {
                        updateExclusion(singles, relation.getLeftLine(), result);
                    }
                }
            }
        }
        return result;
    }


    private void updateExclusion(List<LogikElement> singles, LogikLine rightLine, ChangeResult result) {
        boolean allFound = false;

        for (LogikElement single : singles) {
            List<LogikElement> selectables = rightLine.getSelectables(single.getGroup());
            boolean found = selectables.remove(single);
            if (found) {
                result.getChangedLines().add(rightLine.getLineId());
                String message = "Entferne " + single + " aus Zeile " + rightLine.getLineId();
                result.getRemoveMessages().add(message);
                if(selectables.size() == 1) {
                    message = "Gefunden: Zeile " + rightLine.getLineId() + ", Gruppe " + single.getGroup().getName() + " ist " + selectables.get(0).getName();
                    result.getFoundMessages().add(message);
                }
            }
            allFound |= found;
        }

        if (allFound)
            view.updateLine(problem.getGroups(), rightLine);
    }

    private boolean checkExcludingRelation(LogikRelationType type) {
        switch (type) {
            case PREVIOUS:
            case NEXT:
            case PLUS_MINUS:
            case NOT_EQUAL:
                return true;
            default:
                return false;
        }
    }

    private boolean checkExcluded(LogikLine line, LogikLine findLine) {
// First check non duplicate blocks
        for (LogikBlock block : problem.getBlocks()) {
            if (block.isNoDuplicates() && block.getMainLines().contains(line) && block.getMainLines().contains(findLine)) {
                return true;
            }
        }

        // Check if some group excludes both lines
        for (LogikGroup group : problem.getGroups()) {
            int counter = 0;
            List<LogikElement> selectables = line.getSelectables(group);
            List<LogikElement> findSelectables = findLine.getSelectables(group);
            for (LogikElement element : selectables) {
                if (findSelectables.contains(element)) {
                    counter++;
                    break;
                }
            }
            if (counter == 0) return true;
        }
        return false;
    }

    private List<LogikElement> findSingles(LogikLine line) {
        final List<LogikElement> singles = new ArrayList<>();
        for (LogikGroup group : problem.getGroups()) {
            List<LogikElement> selectables = line.getSelectables(group);
            if (selectables.size() == 1) {
                singles.add(selectables.get(0));
            }
        }
        return singles;
    }

    public ChangeResult findPositives(LogikLine findLine, List<LogikLine> additionalLines) throws LogikException {
        ChangeResult result = new ChangeResult();

        List<LogikElement> singles = findSingles(findLine);
        if (singles.isEmpty() && additionalLines == null)
            return result;

       // Find in lines
        final List<LogikLine> duplicates = new ArrayList<>();
        if (!singles.isEmpty()) {
            for (LogikLine line : problem.getLines()) {
                if (line != findLine) {
                    boolean isDuplicate = checkDuplicate(line, singles);
                    if (isDuplicate) {
                        duplicates.add(line);
                    }
                }
            }
        }

        if (additionalLines != null) {
            for (LogikLine line : additionalLines) {
                if (!duplicates.contains(line))
                    duplicates.add(line);
            }
        }

        if (!duplicates.isEmpty()) {

            // Find min index
            Optional<LogikLine> minLine = duplicates.stream().min(Comparator.comparing(LogikLine::getLineId));
            if (minLine.get().getLineId() < findLine.getLineId()) {
                duplicates.add(findLine);
                duplicates.remove(minLine.get());
                findLine = minLine.get();
            }

            mergeLines(findLine, duplicates);
            problem.replaceLines(findLine, duplicates);
            view.replaceLines(problem.getGroups(), findLine, duplicates);

            result.getChangedLines().add(findLine.getLineId());
            duplicates.forEach(o -> result.getChangedLines().add(o.getLineId()));

            String message = "Vereinige Zeile " + findLine.getLineId() + " mit ";
            String duplicateList = duplicates.stream().map(o -> Integer.toString(o.getLineId())).collect(Collectors.joining(", "));
            message += duplicateList;
            result.getRemoveMessages().add(message);
        }
        return result;
    }

    private void mergeLines(LogikLine findLine, List<LogikLine> duplicates) throws LogikException {
        Set<LogikElement> toUnselect = new HashSet<>();
        for (LogikLine duplicate : duplicates) {
            for (LogikGroup group : problem.getGroups()) {
                List<LogikElement> selectables = findLine.getSelectables(group);
                List<LogikElement> duplicateSelectables = duplicate.getSelectables(group);
                for (LogikElement element : selectables) {
                    if (!duplicateSelectables.contains(element)) {
                        toUnselect.add(element);
                    }
                }
            }
        }

        for (LogikElement unselect : toUnselect) {
            findLine.unselect(unselect);
        }
    }

    private boolean checkDuplicate(LogikLine line, List<LogikElement> singles) {
        for (LogikElement single : singles) {
            List<LogikElement> selectables = line.getSelectables(single.getGroup());
            if (selectables.size() == 1 && selectables.get(0) == single)
                return true;
        }
        return false;
    }

    public boolean mergeLines(LogikLine line1, LogikLine line2) throws LogikException {
        List<LogikLine> duplicates = new ArrayList<>();
        duplicates.add(line2);
        mergeLines(line1, duplicates);
        problem.replaceLines(line1, duplicates);
        view.replaceLines(problem.getGroups(), line1, duplicates);
        return true;
    }

    public void checkSolvability(LogikProblem problem) throws LogikException {
        for (LogikLine line : problem.getLines()) {
            for (List<LogikElement> elements : line.getSelectableElements())
                if (elements.isEmpty())
                    throw new LogikException("Zeile " + line.getLineId() + " hat keine Wahlmöglichkeiten mehr!");
        }

        for (LogikBlock logikBlock : problem.getBlocks()) {
            if (logikBlock.isNoDuplicates()) {
                List<LogikLine> duplicates = logikBlock.getMainLines().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                        .entrySet()
                        .stream()
                        .filter(p -> p.getValue() > 1)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());
                if (!duplicates.isEmpty()) {
                    throw new LogikException("Singulärer Block " + logikBlock.getName() + " hat die Duplikatzeile " + duplicates.get(0).getLineId());
                }
            }
        }
    }

    public List<LogicBlockViewLine> buildGroupViewLines(Integer groupId) {
        LogikGroup group = problem.getGroup(groupId);
        final List<LogicBlockViewLine> groupLines = new ArrayList<>();
        for (LogikElement element : group.getElements()) {
            LogicBlockViewLine groupLine = new LogicBlockViewLine(ViewLineType.LINE, null, null, null);

            // Group element
            List<Integer> selectedIndizes = new ArrayList<>();
            selectedIndizes.add(element.getIndex());
            ValueView valueView = new ValueView(element.getName(), selectedIndizes);
            groupLine.setValueView(0, valueView);

            // Other groups
            List<LogikLine> singleLines = problem.getLines().stream().filter(o -> o.getSelectables(group).size() == 1 && o.getSelectables(group).get(0) == element).collect(Collectors.toList());
            if (!singleLines.isEmpty()) {
                int index = 1;
                LogikLine singleLine = singleLines.get(0);
                for (LogikGroup otherGroup : problem.getGroups()) {
                    if (otherGroup != group) {
                        List<LogikElement> origView = singleLine.getSelectables(otherGroup);
                        ValueView copyView = LogicBlockViewBuilder.buildValueView(otherGroup, origView);
                        groupLine.setValueView(index++, copyView);
                    }
                }
            } else {
                // Try merge
                List<LogikLine> containingLines = problem.getLines().stream().filter(o -> o.getSelectables(group).contains(element)).collect(Collectors.toList());
                List<LogikLine> notContainingLines = problem.getLines().stream().filter(o -> !o.getSelectables(group).contains(element)).collect(Collectors.toList());

                int index = 1;

                // Collect all - condition: one of the containing lines contains really the element. Is this sure?
                for (LogikGroup otherGroup : problem.getGroups()) {
                    if (otherGroup != group) {
                        Set<LogikElement> possibleElements = new HashSet<>();
                        for (LogikLine containingLine : containingLines) {
                            possibleElements.addAll(containingLine.getSelectables(otherGroup));
                        }

                        // Remove singles from not containing lines
                        for (LogikLine notContainingLine : notContainingLines) {
                            List<LogikElement> elements = notContainingLine.getSelectables(otherGroup);
                            if (elements.size() == 1)
                                possibleElements.remove(elements.get(0));
                        }
                        ValueView copyView = LogicBlockViewBuilder.buildValueView(otherGroup, possibleElements.stream().sorted(Comparator.comparing(LogikElement::getIndex)).collect(Collectors.toList()));
                        groupLine.setValueView(index++, copyView);
                    }
                }
            }
            groupLines.add(groupLine);
        }
        return groupLines;
    }
}
