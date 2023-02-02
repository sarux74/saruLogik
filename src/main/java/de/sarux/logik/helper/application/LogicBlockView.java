package de.sarux.logik.helper.application;

import de.sarux.logik.helper.problem.view.ViewLineType;
import de.sarux.logik.helper.problem.view.ValueView;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikRelationType;
import de.sarux.logik.helper.problem.LogikLineRelation;
import de.sarux.logik.helper.problem.LogikBlock;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Deprecated
public class LogicBlockView {

    @Getter
    @Setter
    private int numGroups;
    @Getter
    private final List<LogicBlockViewLine> lines = new ArrayList<>();

    public LogicBlockView(int numGroups) {
        this.numGroups = numGroups;
    }

    public void addLine(LogicBlockViewLine line) {
        lines.add(line);
    }


    public void updateSelection(LogikLine line, LogikGroup group, List<LogikElement> selectedElements) {
        ValueView groupValueView = LogicBlockViewBuilder.buildValueView(group, selectedElements);
        lines.stream().filter(o -> (o.getType() == ViewLineType.LINE || o.getType() == ViewLineType.SUBLINE) && o.getLineId() == line.getLineId()).forEach(
                l -> l.setValueView(group.getIndex(), groupValueView)
        );
    }

    public void newBlock(List<LogikGroup> groups, LogikBlock block, LogikLine line) {
        int insertIndex = lines.size() - 1;

        LogicBlockViewLine blockLine = LogicBlockViewBuilder.buildBlockViewLine(block);
        lines.add(insertIndex++, blockLine);
        if (line != null) {
            LogicBlockViewLine lineViewLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, line);
            lines.add(insertIndex++, lineViewLine);
        }
        LogicBlockViewLine newLineViewLine = LogicBlockViewBuilder.buildNewLineViewLine(block);
        lines.add(insertIndex++, newLineViewLine);

        lines.add(insertIndex, LogicBlockViewBuilder.buildMainSeparatorLine(block));
    }

    public void newRelation(LogikBlock block, List<LogikGroup> groups, LogikLineRelation newRelation) throws LogikException {
        boolean isSubRelation = newRelation.isSubRelation();
        final List<LogikLineRelation> relations = new ArrayList<>();
        relations.add(newRelation);
        if (isSubRelation) {
            if (newRelation.getType() == LogikRelationType.NONE) return;

            int insertIndex = findLastBlockIndex(block);
            final LogicBlockViewLine subMainLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, newRelation.getLeftLine());
            lines.add(insertIndex++, subMainLine);
            lines.addAll(insertIndex, LogicBlockViewBuilder.addRelationLines(relations, block));
            insertIndex += 2;
            final LogicBlockViewLine subSubLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, newRelation.getRightLine());
            lines.add(insertIndex++, subSubLine);
            final LogicBlockViewLine subSeparatorLine = LogicBlockViewBuilder.buildSubSeparatorLine(block);
            lines.add(insertIndex, subSeparatorLine);
        } else {
            Optional<LogicBlockViewLine> indexLine = lines.stream().filter(o -> o.getType() == ViewLineType.LINE && o.getBlockId() == block.getBlockId() && o.getLineId() == newRelation.getLeftLine().getLineId()).findFirst();
            if (indexLine.isPresent()) {
                int insertIndex = lines.indexOf(indexLine.get()) + 1;
                if (newRelation.getType() != LogikRelationType.NONE) {
                    LogicBlockViewLine upperLine = LogicBlockViewBuilder.buildUpperRelationViewLine(relations, block);
                    if (upperLine != null) {
                        lines.add(insertIndex, upperLine);
                        insertIndex++;
                    }
                    LogicBlockViewLine lowerLine = LogicBlockViewBuilder.buildLowerRelationViewLine(relations, block);
                    if (lowerLine != null) {
                        lines.add(insertIndex, lowerLine);
                        insertIndex++;
                    }
                }

                LogicBlockViewLine lineViewLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, newRelation.getRightLine());
                lines.add(insertIndex, lineViewLine);
            } // TODO else
        }
    }

    private int findLastBlockIndex(LogikBlock block) {
        boolean foundBlockStart = false;
        for (int i = 0; i < lines.size(); i++) {
            LogicBlockViewLine line = lines.get(i);
            if (!foundBlockStart && line.getBlockId() == block.getBlockId())
                foundBlockStart = true;
            else if (foundBlockStart && (line.getBlockId() == null || line.getBlockId() != block.getBlockId()))
                return i;
        }
        return -1;
    }

    public void flipBlock(List<LogikGroup> groups, LogikBlock block) throws LogikException {
        hideBlock(block);
        showBlock(groups, block);
    }

    public void hideBlock(LogikBlock block) {
        Optional<LogicBlockViewLine> blockLine = lines.stream().filter(l -> l.getBlockId() == block.getBlockId() && l.getType() == ViewLineType.BLOCK).findFirst();
        if (blockLine.isPresent() && !blockLine.get().isHideSub()) {
            lines.removeIf(l -> l.getBlockId() != null && l.getBlockId() == block.getBlockId() && l.getType() != ViewLineType.BLOCK);
            blockLine.get().setHideSub(true);
        }
    }

    public void showBlock(List<LogikGroup> groups, LogikBlock block) throws LogikException {
        Optional<LogicBlockViewLine> blockLine = lines.stream().filter(l -> l.getBlockId() == block.getBlockId() && l.getType() == ViewLineType.BLOCK).findFirst();
        if (blockLine.isPresent() && blockLine.get().isHideSub()) {
            List<LogicBlockViewLine> blockLines = LogicBlockViewBuilder.buildBlockLines(groups, block);
            int index = lines.indexOf(blockLine.get()) + 1;
            lines.addAll(index, blockLines);
            blockLine.get().setHideSub(false);
        }
    }

    public void addLines(List<LogicBlockViewLine> blockLines) {
        lines.addAll(blockLines);
    }

    public void updateLine(List<LogikGroup> groups, LogikLine line) {
        final List<LogicBlockViewLine> linesToChange = lines.stream().filter(o -> o.getLineId() != null && o.getLineId() == line.getLineId() && (o.getType() == ViewLineType.LINE || o.getType() == ViewLineType.SUBLINE)).collect(Collectors.toList());
        for (LogicBlockViewLine lineToChange : linesToChange) {
            int index = lines.indexOf(lineToChange);
            LogicBlockViewLine old = lines.remove(index);
            LogicBlockViewLine replace = LogicBlockViewBuilder.buildLineViewLine(groups, old.getBlockId(), line);
            replace.setHideSub(old.isHideSub());
            lines.add(index, replace);
        }
    }

    public void replaceLines(List<LogikGroup> groups, LogikLine findLine, List<LogikLine> duplicates) {
        final List<Integer> lineIds = duplicates.stream().map(LogikLine::getLineId).collect(Collectors.toList());
        final List<LogicBlockViewLine> duplicateLines = lines.stream().filter(o -> (o.getType() == ViewLineType.LINE || o.getType() == ViewLineType.SUBLINE) && (lineIds.contains(o.getLineId()) || o.getLineId() == findLine.getLineId())).collect(Collectors.toList());

        for (LogicBlockViewLine duplicateLine : duplicateLines) {
            int index = lines.indexOf(duplicateLine);
            LogicBlockViewLine replace = LogicBlockViewBuilder.buildLineViewLine(groups, duplicateLine.getBlockId(), findLine);

            replace.setHideSub(duplicateLine.isHideSub());
            lines.remove(index);
            lines.add(index, replace);
        }
    }

    public void updateRelation(LogikBlock block, List<LogikLineRelation> relations) throws LogikException {
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
            return;

        LogikLine finalLeftLine = leftLine;
        LogikLine finalRightLine = rightLine;

        Optional<LogicBlockViewLine> upperLine = lines.stream().filter(o -> o.getType() == ViewLineType.RELATION_UPPER && o.getLineId() == finalLeftLine.getLineId()
                && o.getRightLineId() == finalRightLine.getLineId()).findFirst();
        Optional<LogicBlockViewLine> lowerLine = lines.stream().filter(o -> o.getType() == ViewLineType.RELATION_LOWER && o.getLineId() == finalLeftLine.getLineId()
                && o.getRightLineId() == finalRightLine.getLineId()).findFirst();
        if (upperLine.isPresent() && lowerLine.isPresent()) {
            LogicBlockViewLine newUpperLine = LogicBlockViewBuilder.buildUpperRelationViewLine(relations, block);
            int index = lines.indexOf(upperLine.get());
            lines.remove(index);
            if (newUpperLine != null)
                lines.add(index, newUpperLine);

            LogicBlockViewLine newLowerLine = LogicBlockViewBuilder.buildLowerRelationViewLine(relations, block);
            index = lines.indexOf(lowerLine.get());
            lines.remove(index);
            if (newLowerLine != null)
                lines.add(index, newLowerLine);
        }
    }

    public void addRelation(LogikBlock block, List<LogikLineRelation> relations) throws LogikException {
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
            return;

        LogikLine finalLeftLine = leftLine;
        LogikLine finalRightLine = rightLine;

        Optional<LogicBlockViewLine> upperLine = lines.stream().filter(o -> o.getType() == ViewLineType.RELATION_UPPER && o.getLineId() == finalLeftLine.getLineId()
                && o.getRightLineId() == finalRightLine.getLineId()).findFirst();
        Optional<LogicBlockViewLine> lowerLine = lines.stream().filter(o -> o.getType() == ViewLineType.RELATION_LOWER && o.getLineId() == finalLeftLine.getLineId()
                && o.getRightLineId() == finalRightLine.getLineId()).findFirst();
        if (upperLine.isPresent() && lowerLine.isPresent()) {
            LogicBlockViewLine newUpperLine = LogicBlockViewBuilder.buildUpperRelationViewLine(relations, block);
            int index = lines.indexOf(upperLine.get());
            lines.remove(index);
            if (newUpperLine != null)
                lines.add(index, newUpperLine);

            LogicBlockViewLine newLowerLine = LogicBlockViewBuilder.buildLowerRelationViewLine(relations, block);
            index = lines.indexOf(lowerLine.get());
            lines.remove(index);
            if (newLowerLine != null)
                lines.add(index, newLowerLine);
        }
    }

    public void addBlockLine(LogikBlock block, List<LogikGroup> groups, LogikLine newLine) {
        Optional<LogicBlockViewLine> addLineLine = lines.stream().filter(o -> o.getType() == ViewLineType.ADD_LINE && o.getBlockId() == block.getBlockId()).findFirst();
        if (addLineLine.isPresent()) {
            int insertIndex = lines.indexOf(addLineLine.get());
            final LogicBlockViewLine subMainLine = LogicBlockViewBuilder.buildLineViewLine(groups, block, newLine);
            lines.add(insertIndex, subMainLine);
        }
    }

    public void refresh(List<LogikGroup> groups, List<LogikBlock> blocks) throws LogikException {
        for(LogikBlock block: blocks) {
            Optional<LogicBlockViewLine> blockLine = lines.stream().filter(l -> l.getBlockId() == block.getBlockId() && l.getType() == ViewLineType.BLOCK).findFirst();
            if (blockLine.isPresent() && !blockLine.get().isHideSub()) {
                hideBlock(block);
                showBlock(groups, block);
            }
        }
    }
}
