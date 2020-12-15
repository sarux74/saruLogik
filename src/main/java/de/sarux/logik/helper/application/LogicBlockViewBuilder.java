package de.sarux.logik.helper.application;

import de.sarux.logik.helper.*;
import de.sarux.logik.helper.group.LogikGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogicBlockViewBuilder {
    public static ValueView buildValueView(LogikGroup group, List<LogikElement> selectables) {
        String text = "";
        List<Integer> selectedValues = new ArrayList<>();
        if (selectables.size() == 1) {
            text = selectables.get(0).getName();
            selectedValues.add(selectables.get(0).getIndex());
        } else {
            for (LogikElement element : group.getElements()) {
                if (selectables.contains(element)) {
                    text += element.getShortName() + " ";
                    selectedValues.add(element.getIndex());
                } else {
                    text += "- ";
                }
            }
        }
        return new ValueView(text, selectedValues);
    }

    public static LogicBlockViewLine buildBlockViewLine(LogikBlock block) {
        LogicBlockViewLine blockLine = new LogicBlockViewLine(ViewLineType.BLOCK, block.getBlockId(), null, null);
        ValueView valueView = new ValueView("Block " + block.getName());
        blockLine.setValueView(0, valueView);
        return blockLine;
    }

    public static LogicBlockViewLine buildLineViewLine(List<LogikGroup> groups, LogikBlock block, LogikLine logikLine) {
        return buildLineViewLine(groups, block.getBlockId(), logikLine);
    }

    public static LogicBlockViewLine buildLineViewLine(List<LogikGroup> groups, int blockId, LogikLine logikLine) {
        LogicBlockViewLine line = new LogicBlockViewLine(ViewLineType.LINE, blockId, logikLine.getLineId(), null);
        for (int j = 0; j < groups.size(); j++) {
            final LogikGroup group = groups.get(j);
            List<LogikElement> selectables = logikLine.getSelectables(group);
            ValueView groupValueView = LogicBlockViewBuilder.buildValueView(group, selectables);
            line.setValueView(j, groupValueView);
        }
        return line;
    }


    public static LogicBlockViewLine buildNewLineViewLine(LogikBlock block) {
        return new LogicBlockViewLine(ViewLineType.ADD_LINE, block.getBlockId(), null, null);
    }

    public static List<LogicBlockViewLine> addRelationLines(List<LogikLineRelation> relations, LogikBlock block) throws LogikException {

        final List<LogicBlockViewLine> relationLines = new ArrayList<>();
        LogicBlockViewLine leftViewLine = buildUpperRelationViewLine(relations, block);
        if (leftViewLine != null)
            relationLines.add(leftViewLine);

        LogicBlockViewLine rightViewLine = buildLowerRelationViewLine(relations, block);
        if (rightViewLine != null)
            relationLines.add(rightViewLine);
        return relationLines;
    }

    public static LogicBlockViewLine buildLowerRelationViewLine(List<LogikLineRelation> relations, LogikBlock block) throws LogikException {

        LogikLine leftLine = null;
        LogikLine rightLine = null;
        for (LogikLineRelation relation : relations) {
            if (relation.getType() != LogikRelationType.NONE) {
                if (leftLine != null && relation.getLeftLine() != leftLine)
                    throw new LogikException("Obere Zeile ist nicht eindeutig!");
                if (rightLine != null && relation.getRightLine() != rightLine)
                    throw new LogikException("Untere Zeile ist nicht eindeutig!");
                leftLine = relation.getLeftLine();
                rightLine = relation.getRightLine();
            }
        }
        if (leftLine == null || rightLine == null)
            return null;
        LogicBlockViewLine rightViewLine = new LogicBlockViewLine(ViewLineType.RELATION_LOWER, block.getBlockId(), leftLine.getLineId(), rightLine.getLineId());

        for (LogikLineRelation relation : relations) {
            ValueView relationValueView;
            final LogikGroup rightGroup = relation.getRightGroup();
            relationValueView = new ValueView("   *");
            rightViewLine.setValueView(rightGroup.getIndex(), relationValueView);
        }
        return rightViewLine;
    }

    public static LogicBlockViewLine buildUpperRelationViewLine(List<LogikLineRelation> relations, LogikBlock block) throws LogikException {

        LogikLine leftLine = null;
        LogikLine rightLine = null;
        for (LogikLineRelation relation : relations) {
            if (relation.getType() != LogikRelationType.NONE) {
                if (leftLine != null && relation.getLeftLine() != leftLine)
                    throw new LogikException("Obere Zeile ist nicht eindeutig!");
                if (rightLine != null && relation.getRightLine() != rightLine)
                    throw new LogikException("Untere Zeile ist nicht eindeutig!");
                leftLine = relation.getLeftLine();
                rightLine = relation.getRightLine();
            }
        }
        if (leftLine == null || rightLine == null)
            return null;

        LogicBlockViewLine leftViewLine = new LogicBlockViewLine(ViewLineType.RELATION_UPPER, block.getBlockId(), leftLine.getLineId(), rightLine.getLineId());

        for (LogikLineRelation relation : relations) {
            if (relation.getType() == LogikRelationType.NONE)
                continue;
            final LogikGroup leftGroup = relation.getLeftGroup();
            String text = "* ";
            String relationHint = relation.getRelationHint();
            switch (relation.getType()) {

                case PREVIOUS:
                    text += "<" + ((relationHint != null) ? " " + relationHint : "");
                    break;
                case NEXT:
                    text += ">" + ((relationHint != null) ? " " + relationHint : "");
                    break;

                case NOT_PREVIOUS:
                    text += "nicht <" + ((relationHint != null) ? " " + relationHint : "");
                    break;
                case NOT_NEXT:
                    text += "nicht > " + ((relationHint != null) ? " " + relationHint : "");
                    break;
                case PLUS_MINUS:
                    text += "+- " + ((relationHint != null) ? " " + relationHint : "");
                    break;
                case NOT_PLUS_MINUS:
                    text += "nicht +- " + ((relationHint != null) ? " " + relationHint : "");
                    break;
                case EQUAL:
                    text += "=" + ((relationHint != null) ? " " + relationHint : "");
                    break;
                case NOT_EQUAL:
                    text += "<>" + ((relationHint != null) ? " " + relationHint : "");
                    break;
            }

            ValueView relationValueView = new ValueView(text);
            leftViewLine.setValueView(leftGroup.getIndex(), relationValueView);
        }
        return leftViewLine;
    }

    public static LogicBlockViewLine buildMainSeparatorLine(LogikBlock block) {
        return new LogicBlockViewLine(ViewLineType.MAIN_SEPARATOR, block.getBlockId(), null, null);
    }

    public static LogicBlockViewLine buildSubSeparatorLine(LogikBlock block) {
        return new LogicBlockViewLine(ViewLineType.SUB_SEPARATOR, block.getBlockId(), null, null);
    }

    public static List<LogicBlockViewLine> buildBlockLines(List<LogikGroup> groups, LogikBlock block) throws LogikException {
        final List<LogicBlockViewLine> blockLines = new ArrayList<>();
        final List<LogikLine> lines = block.getMainLines();
        final List<LogikLineRelation> usedRelations = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            final LogikLine logikLine = lines.get(i);
            LogicBlockViewLine viewLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, logikLine);
            blockLines.add(viewLine);

            // Relation to next

            if (i + 1 < lines.size()) {
                final LogikLine nextLogikLine = lines.get(i + 1);
                List<LogikLineRelation> relations = block.getRelations().stream().filter(o -> o.getLeftLine() == logikLine && o.getRightLine() == nextLogikLine && !o.isSubRelation() && !usedRelations.contains(o)).collect(Collectors.toList());

                blockLines.addAll(LogicBlockViewBuilder.addRelationLines(relations, block));
                usedRelations.addAll(relations);
            }
        }

        final LogicBlockViewLine addViewLine = LogicBlockViewBuilder.buildNewLineViewLine(block);
        blockLines.add(addViewLine);

        final LogicBlockViewLine separatorLine = LogicBlockViewBuilder.buildMainSeparatorLine(block);
        blockLines.add(separatorLine);

        final List<LogikLineRelation> subRelations = block.getRelations().stream().filter(LogikLineRelation::isSubRelation).collect(Collectors.toList());
        while (!subRelations.isEmpty()) {
            LogikLineRelation relation = subRelations.get(0);
            List<LogikLineRelation> similarRelations = subRelations.stream().filter(o -> o.getLeftLine() == relation.getLeftLine() && o.getRightLine() == relation.getRightLine()).collect(Collectors.toList());
            subRelations.removeAll(similarRelations);

            final LogicBlockViewLine subMainLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, relation.getLeftLine());
            blockLines.add(subMainLine);
            blockLines.addAll(LogicBlockViewBuilder.addRelationLines(similarRelations, block));
            final LogicBlockViewLine subSubLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, relation.getRightLine());
            blockLines.add(subSubLine);
            final LogicBlockViewLine subSeparatorLine = LogicBlockViewBuilder.buildSubSeparatorLine(block);
            blockLines.add(subSeparatorLine);
        }

        return blockLines;
    }
}


