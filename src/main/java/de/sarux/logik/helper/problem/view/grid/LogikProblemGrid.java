/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem.view.grid;

import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewBuilder;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewLine;
import de.sarux.logik.helper.problem.view.ValueView;
import de.sarux.logik.helper.problem.view.ViewLineType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;

/**
 *
 * @author sarux
 */
@Getter
public class LogikProblemGrid {

    private final List<LogikElementRelation> elementRelations = new ArrayList<>();
    private List<LogikGroup> groups;
    private final List<LogikElementRelation> foundUniqueRelations = new ArrayList<>();

    public LogikProblemGrid() {
    }

    public LogikProblemGrid(GeneralLogikProblem problem) {
        this.groups = problem.getGroups();

        int groupSize = problem.getGroups().size();
        for (int i = 0; i < groupSize; i++) {
            final LogikGroup group1 = problem.getGroup(i);
            for (int j = i + 1; j < groupSize; j++) {
                final LogikGroup group2 = problem.getGroup(j);
                for (LogikElement element1 : group1.getElements()) {
                    for (LogikElement element2 : group2.getElements()) {
                        elementRelations.add(new LogikElementRelation(element1, element2));
                    }
                }
            }
        }

        // Unset findings from lines
        final List<GeneralLogikBlock> blocks = problem.getSingleBlocks();

        final Set<LogikLine> nonOptionLines = new HashSet<>();
        blocks.forEach(o -> {
            nonOptionLines.addAll(o.getMainLines());
            nonOptionLines.addAll(o.getSubLines());
        });

        for (int i = 0; i < groupSize; i++) {
            final LogikGroup group1 = problem.getGroup(i);
            for (int j = i + 1; j < groupSize; j++) {
                for (LogikElement element : group1.getElements()) {
                    // Other groups
                    List<LogikLine> singleLines = nonOptionLines.stream().filter(o -> o.getSelectables(group1).size() == 1 && o.getSelectables(group1).get(0) == element).collect(Collectors.toList());
                    if (!singleLines.isEmpty()) {
                        LogikLine singleLine = singleLines.get(0);
                        for (LogikGroup otherGroup : problem.getGroups()) {
                            if (otherGroup != group1) {
                                List<LogikElement> notSelected = new ArrayList<>(otherGroup.getElements());
                                notSelected.removeAll(singleLine.getSelectables(otherGroup));
                                for (LogikElement element2 : notSelected) {
                                    unsetRelation(element, element2);
                                }
                            }
                        }
                    } else {
                        // Try merge
                        List<LogikLine> containingLines = nonOptionLines.stream().filter(o -> o.getSelectables(group1).contains(element)).collect(Collectors.toList());
                        List<LogikLine> notContainingLines = nonOptionLines.stream().filter(o -> !o.getSelectables(group1).contains(element)).collect(Collectors.toList());

                        // Collect all - condition: one of the containing lines contains really the element. Is this sure?
                        for (LogikGroup otherGroup : problem.getGroups()) {
                            if (otherGroup != group1) {
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

                                if (!possibleElements.isEmpty()) {
                                    List<LogikElement> notSelected = new ArrayList<>(otherGroup.getElements());
                                    notSelected.removeAll(possibleElements);
                                    for (LogikElement element2 : notSelected) {
                                        unsetRelation(element, element2);
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    public final void unsetRelation(LogikElement element1, LogikElement element2) {
        final Optional<LogikElementRelation> relation = findRelation(element1, element2);
        if (relation.isPresent()) {
            LogikElementRelation foundRelation = relation.get();

            elementRelations.remove(foundRelation);

            LogikElement leftElement = foundRelation.getLeftElement();
            LogikElement rightElement = foundRelation.getRightElement();

            final List<LogikElementRelation> newUniqueRelations = new ArrayList<>(2);
            checkNewUniqueRelations(newUniqueRelations, leftElement, rightElement);

            final List<LogikElementRelation> fittingRelations = foundUniqueRelations.stream().filter(o -> o.getLeftElement() == leftElement || o.getLeftElement() == rightElement || o.getRightElement() == leftElement || o.getRightElement() == rightElement).collect(Collectors.toList());
            for (LogikElementRelation fittingRelation : fittingRelations) {
                LogikElement fitElement1 = null;
                LogikElement fitElement2 = null;
                if (fittingRelation.getLeftElement() == leftElement) {
                    fitElement1 = fittingRelation.getRightElement();
                    fitElement2 = rightElement;
                } else if (fittingRelation.getLeftElement() == rightElement) {
                    fitElement1 = fittingRelation.getRightElement();
                    fitElement2 = leftElement;
                } else if (fittingRelation.getRightElement() == leftElement) {
                    fitElement1 = fittingRelation.getLeftElement();
                    fitElement2 = rightElement;
                } else if (fittingRelation.getRightElement() == rightElement) {
                    fitElement1 = fittingRelation.getLeftElement();
                    fitElement2 = leftElement;
                }

                if (fitElement1 != fitElement2) {
                    Optional<LogikElementRelation> fittingCrossRelation = findRelation(fitElement1, fitElement2);
                    if (fittingCrossRelation.isPresent()) {
                        elementRelations.remove(fittingCrossRelation.get());
                        checkNewUniqueRelations(newUniqueRelations, fitElement1, fitElement2);
                    }
                }
            }

            // Handle new unique
            if (!newUniqueRelations.isEmpty()) {
                handleNewUniqueRelations(newUniqueRelations);
            }
        }

    }

    public List<GeneralLogikBlockViewLine> toGroupView(Integer groupId) {
        LogikGroup group = this.groups.stream().filter(o -> o.getIndex() == groupId).findFirst().orElse(null);

        final List<GeneralLogikBlockViewLine> groupLines = new ArrayList<>();
        for (LogikElement element : group.getElements()) {
            GeneralLogikBlockViewLine groupLine = new GeneralLogikBlockViewLine(ViewLineType.LINE, null, null, element.getIndex(), null);

            // Group element
            List<Integer> selectedIndizes = new ArrayList<>();
            selectedIndizes.add(element.getIndex());
            ValueView valueView = new ValueView(element.getName(), selectedIndizes);
            groupLine.setValueView(0, valueView);

            // Other groups
            int index = 1;
            for (LogikGroup otherGroup : groups) {
                if (otherGroup != group) {
                    List<LogikElement> origView = findPartners(element, otherGroup);
                    ValueView copyView = GeneralLogikBlockViewBuilder.buildValueView(otherGroup, origView);
                    groupLine.setValueView(index++, copyView);
                }
            }

            groupLines.add(groupLine);
        }
        return groupLines;
    }

    private void handleNewUniqueRelations(List<LogikElementRelation> newUniqueRelations) {
        while (!newUniqueRelations.isEmpty()) {
            LogikElementRelation newUnique = newUniqueRelations.get(0);
            LogikGroup leftGroup = newUnique.getLeftElement().getGroup();
            LogikGroup rightGroup = newUnique.getRightElement().getGroup();
            LogikElement leftElement = newUnique.getLeftElement();
            LogikElement rightElement = newUnique.getRightElement();

            boolean removed = elementRelations.removeIf(o -> o.getLeftElement() == leftElement && o.getRightElement() != rightElement && o.getRightElement().getGroup() == rightElement.getGroup());
            if (removed) {
                for (LogikElement element : rightElement.getGroup().getElements()) {
                    if (element != rightElement) {
                        checkNewUniqueRelations(newUniqueRelations, leftElement, element);
                    }
                }
            }
            removed = elementRelations.removeIf(o -> o.getRightElement() == rightElement && o.getLeftElement() != leftElement && o.getLeftElement().getGroup() == leftElement.getGroup());
            if (removed) {
                for (LogikElement element : leftElement.getGroup().getElements()) {
                    if (element != leftElement) {
                        checkNewUniqueRelations(newUniqueRelations, rightElement, element);
                    }
                }
            }
            for (LogikGroup group : this.groups) {
                if (group != leftGroup && group != rightGroup) {
                    for (LogikElement element : group.getElements()) {
                        Optional<LogikElementRelation> leftToElement = findRelation(leftElement, element);
                        Optional<LogikElementRelation> rightToElement = findRelation(rightElement, element);
                        if (leftToElement.isPresent() && !rightToElement.isPresent()) {
                            elementRelations.remove(leftToElement.get());
                            checkNewUniqueRelations(newUniqueRelations, leftElement, element);
                        } else if (!leftToElement.isPresent() && rightToElement.isPresent()) {
                            elementRelations.remove(rightToElement.get());
                            checkNewUniqueRelations(newUniqueRelations, rightElement, element);

                        }
                    }
                }
            }

            newUniqueRelations.remove(newUnique);
            foundUniqueRelations.add(newUnique);
        }
    }

    private Optional<LogikElementRelation> findRelation(LogikElement element1, LogikElement element2) {
        boolean switchElements = element1.getGroup().getIndex() > element2.getGroup().getIndex();
        LogikElement leftElement = (switchElements) ? element2 : element1;
        LogikElement rightElement = (switchElements) ? element1 : element2;
        return elementRelations.stream().filter(o -> o.getLeftElement() == leftElement && o.getRightElement() == rightElement).findFirst();
    }

    private List<LogikElementRelation> findRelations(LogikElement element, LogikGroup group) {
        boolean switchElements = element.getGroup().getIndex() > group.getIndex();
        if (switchElements) {
            return elementRelations.stream().filter(o -> o.getLeftElement().getGroup() == group && o.getRightElement() == element).collect(Collectors.toList());
        } else {
            return elementRelations.stream().filter(o -> o.getRightElement().getGroup() == group && o.getLeftElement() == element).collect(Collectors.toList());
        }
    }

    private List<LogikElement> findPartners(LogikElement element, LogikGroup group) {
        boolean switchElements = element.getGroup().getIndex() > group.getIndex();
        if (switchElements) {
            return elementRelations.stream().filter(o -> o.getLeftElement().getGroup() == group && o.getRightElement() == element).map(LogikElementRelation::getLeftElement).collect(Collectors.toList());
        } else {
            return elementRelations.stream().filter(o -> o.getRightElement().getGroup() == group && o.getLeftElement() == element).map(LogikElementRelation::getRightElement).collect(Collectors.toList());
        }
    }

    private void checkNewUniqueRelations(List<LogikElementRelation> newUniqueRelations, LogikElement leftElement, LogikElement rightElement) {
        // Check left
        final List<LogikElementRelation> leftRelations = findRelations(leftElement, rightElement.getGroup());
        if (leftRelations.size() == 1) {
            final LogikElementRelation foundRelation = leftRelations.get(0);
            if (!foundUniqueRelations.contains(foundRelation) && !newUniqueRelations.contains(foundRelation)) {
                newUniqueRelations.add(foundRelation);
            }
        }

        // Check right
        final List<LogikElementRelation> rightRelations = findRelations(rightElement, leftElement.getGroup());
        if (rightRelations.size() == 1) {
            final LogikElementRelation foundRelation = rightRelations.get(0);
            if (!foundUniqueRelations.contains(foundRelation) && !newUniqueRelations.contains(foundRelation)) {
                newUniqueRelations.add(foundRelation);
            }
        }
    }

}
