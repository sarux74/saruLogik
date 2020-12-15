package de.sarux.logik.helper.application;

import de.sarux.logik.helper.*;
import de.sarux.logik.helper.group.LogikGroup;

import java.util.*;
import java.util.stream.Collectors;

public class MultipleRelationBuilder {
    private final LogikProblem problem;

    public MultipleRelationBuilder(LogikProblem currentProblem) {
        this.problem = currentProblem;
    }

    public List<LogicBlockViewLine> build() {
        List<LogicBlockViewLine> result = new ArrayList<>();

        findNextBeforeRelations(result);

        // Cross-Relations
        findCrossRelations(result);

        return result;
    }

    private void findCrossRelations(List<LogicBlockViewLine> result) {
        final Map<LogikGroup, List<LogikLineRelation>> relationsPerGroup = new HashMap<>();
        for (final LogikBlock block : problem.getBlocks()) {
            for (final LogikLineRelation relation : block.getRelations()) {
                if (relation.getType() == LogikRelationType.EQUAL) {
                    LogikGroup lowerGroup = (relation.getLeftGroup().getIndex() < relation.getRightGroup().getIndex()) ? relation.getLeftGroup() : relation.getRightGroup();
                    List<LogikLineRelation> relations = relationsPerGroup.get(lowerGroup);
                    if (relations == null)
                        relations = new ArrayList<>();
                    relations.add(relation);
                    relationsPerGroup.put(lowerGroup, relations);
                }
            }
        }

        for (Map.Entry<LogikGroup, List<LogikLineRelation>> entry : relationsPerGroup.entrySet()) {
            final LogikGroup group = entry.getKey();
            final List<LogikLineRelation> relations = entry.getValue();

            Set<LogikGroup> differentGroups = relations.stream().map(LogikLineRelation::getLeftGroup).collect(Collectors.toSet());
            differentGroups.addAll(relations.stream().map(LogikLineRelation::getRightGroup).collect(Collectors.toSet()));
            differentGroups.remove(group);

            for (Iterator<LogikGroup> it = differentGroups.stream().sorted(Comparator.comparing(LogikGroup::getIndex)).iterator(); it.hasNext(); ) {
                LogikGroup checkGroup = it.next();
                final List<LogikLineRelation> differentLines = relations.stream().filter(o -> o.getLeftGroup() == checkGroup || o.getRightGroup() == checkGroup).collect(Collectors.toList());
                if (differentLines.size() > 1) {
                    for (LogikLineRelation logikLineRelation : differentLines)


                        buildMultipleRelationBlock(logikLineRelation, group, result);
                }
            }
        }
    }


    private void findNextBeforeRelations(List<LogicBlockViewLine> result) {
        final Map<LogikGroup, List<LogikLineRelation>> relationsPerGroup = new HashMap<>();
        for (final LogikBlock block : problem.getBlocks()) {
            for (final LogikLineRelation relation : block.getRelations()) {
                if (relation.getLeftGroup() == relation.getRightGroup() && (relation.getType() == LogikRelationType.NEXT || relation.getType() == LogikRelationType.PREVIOUS)) {
                    List<LogikLineRelation> relations = relationsPerGroup.get(relation.getLeftGroup());
                    if (relations == null)
                        relations = new ArrayList<>();
                    relations.add(relation);
                    relationsPerGroup.put(relation.getLeftGroup(), relations);
                }
            }
        }

        for (Map.Entry<LogikGroup, List<LogikLineRelation>> entry : relationsPerGroup.entrySet()) {
            final LogikGroup group = entry.getKey();
            final List<LogikLineRelation> relations = entry.getValue();

            final Set<LogikLine> differentLines = new HashSet<>();
            for (final LogikLineRelation relation : relations) {
                differentLines.add(relation.getLeftLine());
                differentLines.add(relation.getRightLine());
            }

            final List<LogikLine> sortedLines = differentLines.stream().sorted(Comparator.comparing(LogikLine::getLineId)).collect(Collectors.toList());
            for (final LogikLine line : sortedLines) {
                Set<LogikLine> findAllLowers = findRelatedRecursive(line, relations, LogikRelationType.PREVIOUS);
                Set<LogikLine> findAllHighers = findRelatedRecursive(line, relations, LogikRelationType.NEXT);
                if (findAllLowers.size() + findAllHighers.size() >= 1) {
                    // Filter duplicate 2-side relations
                    if ((findAllLowers.size() == 1 && findAllHighers.isEmpty() && line.getLineId() > findAllLowers.iterator().next().getLineId())
                            || ((findAllHighers.size() == 1 && findAllLowers.isEmpty() && line.getLineId() > findAllHighers.iterator().next().getLineId())))
                        continue;
                    String relationHint = null;
                    if (findAllHighers.size() + findAllLowers.size() == 1) {
                        LogikLine relatedLine;
                        if (!findAllLowers.isEmpty())
                            relatedLine = findAllLowers.iterator().next();
                        else
                            relatedLine = findAllHighers.iterator().next();

                        final LogikLine findLine = relatedLine;
                        Optional<LogikLineRelation> foundRelation = relations.stream().filter(
                                o -> (o.getLeftLine() == line && o.getRightLine() == findLine) || (o.getLeftLine() == findLine && o.getRightLine() == line)
                        ).findFirst();
                        if (foundRelation.isPresent())
                            relationHint = foundRelation.get().getRelationHint();
                    }
                    buildMultipleRelationBlock(group, line, findAllLowers, findAllHighers, result, relationHint);
                }

            }
        }
    }

    private Set<LogikLine> findRelatedRecursive(LogikLine line, List<LogikLineRelation> relations, LogikRelationType type) {
        final Set<LogikLine> checked = new HashSet<>();

        // First round
        final List<LogikLine> unchecked = findRelated(line, relations, type);
        while (!unchecked.isEmpty()) {
            LogikLine nextLine = unchecked.remove(0);
            final List<LogikLine> newLines = findRelated(nextLine, relations, type);
            for (LogikLine newLine : newLines) {
                if (!unchecked.contains(newLine) && !checked.contains(newLine)) {
                    unchecked.add(newLine);
                }
            }
            checked.add(nextLine);
        }
        return checked;
    }

    private List<LogikLine> findRelated(LogikLine line, List<LogikLineRelation> relations, LogikRelationType type) {
        final LogikRelationType inverseType = (type == LogikRelationType.PREVIOUS) ? LogikRelationType.NEXT : LogikRelationType.PREVIOUS;
        final List<LogikLine> unchecked = new ArrayList<>();
        for (LogikLineRelation lineRelation : relations) {
            if ((lineRelation.getLeftLine() != line && lineRelation.getRightLine() == line && lineRelation.getType() == type) ||
                    (lineRelation.getRightLine() != line && lineRelation.getLeftLine() == line && lineRelation.getType() == inverseType)) {
                LogikLine showLine = (lineRelation.getLeftLine() == line) ? lineRelation.getRightLine() : lineRelation.getLeftLine();
                unchecked.add(showLine);
            }
        }
        return unchecked;
    }

    private void buildMultipleRelationBlock(LogikGroup group, LogikLine key, Collection<LogikLine> lowerLines, Collection<LogikLine> higherLines, List<LogicBlockViewLine> result, String relationHint) {
        String name = "Gruppe " + group.getName() + " - Zeile " + key.getLineId();
        LogicBlockViewLine blockLine = new LogicBlockViewLine(ViewLineType.BLOCK, 0, null, null);
        ValueView valueView = new ValueView(name);
        blockLine.setValueView(0, valueView);
        result.add(blockLine);

        // Lower lines
        for (LogikLine showLine : lowerLines) {
            result.add(LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), 0, showLine));
        }

        LogicBlockViewLine rightViewLine = new LogicBlockViewLine(ViewLineType.RELATION_UPPER, 0, key.getLineId(), 0);
        String text = "* < " + ((relationHint != null) ? relationHint : "");
        ValueView relationValueView = new ValueView(text);
        rightViewLine.setValueView(group.getIndex(), relationValueView);

        // Line itself
        if (!lowerLines.isEmpty()) {
            result.add(rightViewLine);
        }
        result.add(LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), 0, key));
        if (!higherLines.isEmpty())
            result.add(rightViewLine);
        // Higher lines
        // Lower lines
        for (LogikLine showLine : higherLines) {
            result.add(LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), 0, showLine));
        }


        result.add(new LogicBlockViewLine(ViewLineType.MAIN_SEPARATOR, 0, null, null));
    }

    private void  buildMultipleRelationBlock(LogikLineRelation logikLineRelation, LogikGroup group, List<LogicBlockViewLine> result) {
        LogikGroup leftGroup;
        LogikGroup rightGroup;

        boolean toSwitch = logikLineRelation.getRightGroup() == group;

        leftGroup = (toSwitch) ? logikLineRelation.getRightGroup(): logikLineRelation.getLeftGroup();
        rightGroup = (toSwitch) ? logikLineRelation.getLeftGroup(): logikLineRelation.getRightGroup();

        String name = "Gruppe " + leftGroup.getName() + " <-> Gruppe " + rightGroup.getName();
        LogicBlockViewLine blockLine = new LogicBlockViewLine(ViewLineType.BLOCK, 0, null, null);
        ValueView valueView = new ValueView(name);
        blockLine.setValueView(0, valueView);
        result.add(blockLine);

        // Upper lines
        LogikLine showLine = (toSwitch) ? logikLineRelation.getRightLine() :  logikLineRelation.getLeftLine();
            result.add(LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), 0, showLine));


        LogicBlockViewLine rightViewLine = new LogicBlockViewLine(ViewLineType.RELATION_UPPER, 0, showLine.getLineId(), 0);
        String text = "* = ";
        ValueView relationValueView = new ValueView(text);
        rightViewLine.setValueView(leftGroup.getIndex(), relationValueView);

        result.add(rightViewLine);

        LogikLine lowerLine = (toSwitch) ? logikLineRelation.getLeftLine() :  logikLineRelation.getRightLine();

        rightViewLine = new LogicBlockViewLine(ViewLineType.RELATION_LOWER, 0, lowerLine.getLineId(), 0);
        text = "*";
        relationValueView = new ValueView(text);
        rightViewLine.setValueView(rightGroup.getIndex(), relationValueView);

        result.add(rightViewLine);
        result.add(LogicBlockViewBuilder.buildLineViewLine(problem.getGroups(), 0, lowerLine));
        result.add(new LogicBlockViewLine(ViewLineType.MAIN_SEPARATOR, 0, null, null));
    }

}