package de.sarux.logik.helper.problem.view;

import de.sarux.logik.helper.application.*;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikRelationType;
import de.sarux.logik.helper.problem.LogikLineRelation;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GeneralLogikBlockViewBuilder {

    public static GeneralLogikBlockView build(GeneralLogikProblem problem) throws LogikException {
        final GeneralLogikBlockView view = new GeneralLogikBlockView(problem.getGroups().size());
        for (LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            if (blockGroup.isHide()) {
                final GeneralLogikBlockViewLine hiddenBlockLine = buildBlockViewLine(blockGroup, blockGroup.getOptionBlocks().get(0));
                hiddenBlockLine.setHideSub(true);
                view.addLine(hiddenBlockLine);
            } else {
                for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                    view.addLines(buildBlockLines(problem.getGroups(), blockGroup, block));
                }
            }
        }
        final GeneralLogikBlockViewLine addViewLine = new GeneralLogikBlockViewLine(ViewLineType.ADD_BLOCK, null, null, null, null);
        view.addLine(addViewLine);
        return view;
    }

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

    public static GeneralLogikBlockViewLine buildBlockViewLine(LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        GeneralLogikBlockViewLine blockLine = new GeneralLogikBlockViewLine(ViewLineType.BLOCK, blockGroup.getBlockGroupId(), block.getBlockId(), null, null);
        String blockName = "Block " + blockGroup.getName();
        if(block.getSubName() != null)
            blockName += " " + block.getSubName();
        ValueView valueView = new ValueView(blockName);
        blockLine.setValueView(0, valueView);
        return blockLine;
    }

    public static GeneralLogikBlockViewLine buildLineViewLine(List<LogikGroup> groups, LogikOptionBlockGroup blockGroup, GeneralLogikBlock block, LogikLine logikLine) {
        return buildLineViewLine(groups, blockGroup.getBlockGroupId(), block.getBlockId(), logikLine);
    }

    public static GeneralLogikBlockViewLine buildLineViewLine(List<LogikGroup> groups, int blockGroupId, int blockId, LogikLine logikLine) {
        GeneralLogikBlockViewLine line = new GeneralLogikBlockViewLine(ViewLineType.LINE, blockGroupId, blockId, logikLine.getLineId(), null);
        for (int j = 0; j < groups.size(); j++) {
            final LogikGroup group = groups.get(j);
            List<LogikElement> selectables = logikLine.getSelectables(group);
            ValueView groupValueView = GeneralLogikBlockViewBuilder.buildValueView(group, selectables);
            line.setValueView(j, groupValueView);
        }
        return line;
    }

    public static GeneralLogikBlockViewLine buildNewLineViewLine(LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        return new GeneralLogikBlockViewLine(ViewLineType.ADD_LINE, blockGroup.getBlockGroupId(), block.getBlockId(), null, null);
    }

    public static List<GeneralLogikBlockViewLine> addRelationLines(List<LogikLineRelation> relations, LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) throws LogikException {

        final List<GeneralLogikBlockViewLine> relationLines = new ArrayList<>();
        GeneralLogikBlockViewLine leftViewLine = buildUpperRelationViewLine(relations, blockGroup, block);
        if (leftViewLine != null) {
            relationLines.add(leftViewLine);
        }

        GeneralLogikBlockViewLine rightViewLine = buildLowerRelationViewLine(relations, blockGroup, block);
        if (rightViewLine != null) {
            relationLines.add(rightViewLine);
        }
        return relationLines;
    }

    public static GeneralLogikBlockViewLine buildLowerRelationViewLine(List<LogikLineRelation> relations, LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) throws LogikException {

        LogikLine leftLine = null;
        LogikLine rightLine = null;
        for (LogikLineRelation relation : relations) {
            if (relation.getType() != LogikRelationType.NONE) {
                if (leftLine != null && relation.getLeftLine() != leftLine) {
                    throw new LogikException("Obere Zeile ist nicht eindeutig!");
                }
                if (rightLine != null && relation.getRightLine() != rightLine) {
                    throw new LogikException("Untere Zeile ist nicht eindeutig!");
                }
                leftLine = relation.getLeftLine();
                rightLine = relation.getRightLine();
            }
        }
        if (leftLine == null || rightLine == null) {
            return null;
        }
        GeneralLogikBlockViewLine rightViewLine = new GeneralLogikBlockViewLine(ViewLineType.RELATION_LOWER, blockGroup.getBlockGroupId(), block.getBlockId(), leftLine.getLineId(), rightLine.getLineId());

        relations.forEach(relation -> {
            ValueView relationValueView;
            final LogikGroup rightGroup = relation.getRightGroup();
            relationValueView = new ValueView("   *");
            rightViewLine.setValueView(rightGroup.getIndex(), relationValueView);
        });
        return rightViewLine;
    }

    public static GeneralLogikBlockViewLine buildUpperRelationViewLine(List<LogikLineRelation> relations, LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) throws LogikException {

        LogikLine leftLine = null;
        LogikLine rightLine = null;
        for (LogikLineRelation relation : relations) {
            if (relation.getType() != LogikRelationType.NONE) {
                if (leftLine != null && relation.getLeftLine() != leftLine) {
                    throw new LogikException("Obere Zeile ist nicht eindeutig!");
                }
                if (rightLine != null && relation.getRightLine() != rightLine) {
                    throw new LogikException("Untere Zeile ist nicht eindeutig!");
                }
                leftLine = relation.getLeftLine();
                rightLine = relation.getRightLine();
            }
        }
        if (leftLine == null || rightLine == null) {
            return null;
        }

        GeneralLogikBlockViewLine leftViewLine = new GeneralLogikBlockViewLine(ViewLineType.RELATION_UPPER, blockGroup.getBlockGroupId(), block.getBlockId(), leftLine.getLineId(), rightLine.getLineId());

        for (LogikLineRelation relation : relations) {
            if (relation.getType() == LogikRelationType.NONE) {
                continue;
            }
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

    public static GeneralLogikBlockViewLine buildMainSeparatorLine(LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        return new GeneralLogikBlockViewLine(ViewLineType.MAIN_SEPARATOR, blockGroup.getBlockGroupId(), block.getBlockId(), null, null);
    }

    public static GeneralLogikBlockViewLine buildSubSeparatorLine(LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        return new GeneralLogikBlockViewLine(ViewLineType.SUB_SEPARATOR, blockGroup.getBlockGroupId(), block.getBlockId(), null, null);
    }

    public static List<GeneralLogikBlockViewLine> buildBlockLines(List<LogikGroup> groups, LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) throws LogikException {
        final List<GeneralLogikBlockViewLine> blockLines = new ArrayList<>();
        final List<LogikLine> lines = block.getMainLines();
        final List<LogikLineRelation> usedRelations = new ArrayList<>();

        final GeneralLogikBlockViewLine blockLine = buildBlockViewLine(blockGroup, block);
        blockLines.add(blockLine);
        
        for (int i = 0; i < lines.size(); i++) {
            final LogikLine logikLine = lines.get(i);
            GeneralLogikBlockViewLine viewLine = GeneralLogikBlockViewBuilder.buildLineViewLine(groups, blockGroup, block, logikLine);
            blockLines.add(viewLine);

            // Relation to next
            if (i + 1 < lines.size()) {
                final LogikLine nextLogikLine = lines.get(i + 1);
                List<LogikLineRelation> relations = block.getRelations().stream().filter(o -> o.getLeftLine() == logikLine && o.getRightLine() == nextLogikLine && !o.isSubRelation() && !usedRelations.contains(o)).collect(Collectors.toList());

                blockLines.addAll(GeneralLogikBlockViewBuilder.addRelationLines(relations, blockGroup, block));
                usedRelations.addAll(relations);
            }
        }

        final GeneralLogikBlockViewLine addViewLine = GeneralLogikBlockViewBuilder.buildNewLineViewLine(blockGroup, block);
        blockLines.add(addViewLine);

        final GeneralLogikBlockViewLine separatorLine = GeneralLogikBlockViewBuilder.buildMainSeparatorLine(blockGroup, block);
        blockLines.add(separatorLine);

        final List<LogikLineRelation> subRelations = block.getRelations().stream().filter(LogikLineRelation::isSubRelation).collect(Collectors.toList());
        while (!subRelations.isEmpty()) {
            LogikLineRelation relation = subRelations.get(0);
            List<LogikLineRelation> similarRelations = subRelations.stream().filter(o -> o.getLeftLine() == relation.getLeftLine() && o.getRightLine() == relation.getRightLine()).collect(Collectors.toList());
            subRelations.removeAll(similarRelations);

            final GeneralLogikBlockViewLine subMainLine = GeneralLogikBlockViewBuilder.buildLineViewLine(groups, blockGroup, block, relation.getLeftLine());
            blockLines.add(subMainLine);
            blockLines.addAll(GeneralLogikBlockViewBuilder.addRelationLines(similarRelations, blockGroup, block));
            final GeneralLogikBlockViewLine subSubLine = GeneralLogikBlockViewBuilder.buildLineViewLine(groups, blockGroup, block, relation.getRightLine());
            blockLines.add(subSubLine);
            final GeneralLogikBlockViewLine subSeparatorLine = GeneralLogikBlockViewBuilder.buildSubSeparatorLine(blockGroup, block);
            blockLines.add(subSeparatorLine);
        }

        return blockLines;
    }
}
