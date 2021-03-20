package de.sarux.logik.helper.application.detektor;

import de.sarux.logik.helper.application.*;
import de.sarux.logik.helper.application.group.LogikGroup;

import java.util.*;

public class DetektorHelper {
    private final LogikDetektorProblem problem;
    private final LogicBlockView view;

    public DetektorHelper(LogikDetektorProblem problem, LogicBlockView currentView) {
        this.problem = problem;
        this.view = currentView;
    }

    public ChangeResult findNegatives(LogikLine findLine) {

        ChangeResult result = new ChangeResult();

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
        for (LogikBlock block : problem.getTrueBlocks()) {
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
        for (LogikBlock block : problem.getTrueBlocks()) {
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

}
