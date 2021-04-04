/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem.view.group;

import de.sarux.logik.helper.application.LogicBlockViewBuilder;
import de.sarux.logik.helper.application.LogikException;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewLine;
import de.sarux.logik.helper.problem.view.ValueView;
import de.sarux.logik.helper.problem.view.ViewLineType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author sarux
 */
public class GroupViewHandler {
     private final GeneralLogikProblem problem;

    public GroupViewHandler(GeneralLogikProblem problem) {
        this.problem = problem;
    }
    
    
    public List<GeneralLogikBlockViewLine> buildGroupViewLines(Integer groupId) {
        LogikGroup group = problem.getGroup(groupId);
        final List<GeneralLogikBlockViewLine> groupLines = new ArrayList<>();
        for (LogikElement element : group.getElements()) {
            GeneralLogikBlockViewLine groupLine = new GeneralLogikBlockViewLine(ViewLineType.LINE, null, null, null, null);

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
                            if (elements.size() == 1) {
                                possibleElements.remove(elements.get(0));
                            }
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

    public boolean excludeBlockingCandidates(BlockingCandidates blockingCandidates) throws LogikException {
        int groupId = blockingCandidates.getGroupId();
    List<GeneralLogikBlockViewLine> groupLines = buildGroupViewLines(groupId);
        List<Set<LogikElement>> collectedElements = new ArrayList<>();
        for (int i = 0; i < problem.getGroups().size(); i++) {
            collectedElements.add(new HashSet<>());
        }
        List<GeneralLogikBlockViewLine> reducedLines = new ArrayList<>();
        for (int index : blockingCandidates.getSelectedLines()) {
            reducedLines.add(groupLines.get(index));
        }

        for (GeneralLogikBlockViewLine line : reducedLines) {
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
                    changes |= removeOthers(problem, line, collectedElements);
                    break;
                case EXCLUDE:
                    changes |= removeCandidates(problem, line, collectedElements);
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
        return changes;
    }
    
     private boolean removeOthers(GeneralLogikProblem problem, LogikLine line, List<Set<LogikElement>> collectedElements) {
        boolean changes = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> candidateElements = collectedElements.get(i);
            if (candidateElements != null) {
                changes |= line.getSelectableElements().get(i).removeIf(o -> !candidateElements.contains(o));
            }
        }
        return changes;
    }

    private boolean removeCandidates(GeneralLogikProblem problem, LogikLine line, List<Set<LogikElement>> collectedElements) {
        boolean changes = false;
        for (int i = 0; i < collectedElements.size(); i++) {
            Set<LogikElement> candidateElements = collectedElements.get(i);
            if (candidateElements != null) {
                changes |= line.getSelectableElements().get(i).removeIf(candidateElements::contains);
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
                    if (blockingElements.contains(element)) {
                        countInc++;
                    } else {
                        countExc++;
                    }
                }
                if (countInc == 0 && countExc > 0) {
                    exclude = true;
                } else if (countInc > 0 && countExc == 0) {
                    include = true;
                } else if (countInc == 0 && countExc == 0) {
                    throw new LogikException("Widersprüchliche Vergleiche bei Blocking Candidates");
                }
            }
        }
        if (include && exclude) {
            return BlockingType.IMPOSSIBLE;
        } else if (include) {
            return BlockingType.INCLUDE;
        } else if (exclude) {
            return BlockingType.EXCLUDE;
        } else {
            return BlockingType.UNKNOWN;
        }
    }

    private int fixIndex(int index, int frontId) {
        if (index == 0) {
            return frontId;
        }
        if (index <= frontId) {
            return index - 1;
        }
        return index;
    }
}
