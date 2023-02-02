/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.sarux.logik.helper.application.detektor.CombinationView;
import de.sarux.logik.helper.application.detektor.LineSearchResult;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.view.grid.LogikProblemGrid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * @author sarux
 */
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class GeneralLogikProblem {

    private List<LogikGroup> groups;

    @Getter
    private final List<LogikOptionBlockGroup> optionBlockGroups = new ArrayList<>();
    private final List<LogikLine> lines = new ArrayList<>();

    @Getter
    private final BlockCombinationHandler combinationHandler = new BlockCombinationHandler();

    // For case
    @Getter
    @JsonIgnore
    private String parentProblemKey;
    @Getter
    @JsonIgnore
    private int line1Id;
    @Getter
    @JsonIgnore
    private int line2Id;

    @Getter
    private final String version = "1.0";

    @Getter
    private LogikProblemGrid grid;

    private int nextBlockIndex = 1;
    private int nextBlockGroupIndex = 1;
    private int nextLineIndex = 1;

    public GeneralLogikProblem(List<LogikGroup> groups) {
        this.groups = groups;
    }

    public LogikOptionBlockGroup newBlockGroup(String blockName) {
        final LogikOptionBlockGroup blockGroup = new LogikOptionBlockGroup(nextBlockGroupIndex++, blockName);
        optionBlockGroups.add(blockGroup);
        return blockGroup;
    }

    public GeneralLogikBlock newBlock(LogikOptionBlockGroup blockGroup, String blockName) {
        final GeneralLogikBlock block = new GeneralLogikBlock(nextBlockIndex++, blockName);
        blockGroup.append(block);
        combinationHandler.notifyNewBlock(blockGroup, block);
        return block;
    }

    public LogikOptionBlockGroup newBlockGroup(String mainName, List<String> appendices) {

        final LogikOptionBlockGroup blockGroup = new LogikOptionBlockGroup(nextBlockGroupIndex++, mainName);
        optionBlockGroups.add(blockGroup);
        appendices.stream().map(appendix -> new GeneralLogikBlock(nextBlockIndex++, appendix)).forEachOrdered(optionBlock -> {
            blockGroup.append(optionBlock);
            newMainLine(optionBlock);
            combinationHandler.notifyNewBlock(blockGroup, optionBlock);
        });

        return blockGroup;
    }

    public GeneralLogikBlock newBlockGroupWithBlock(String blockName) {
        final LogikOptionBlockGroup newGroup = newBlockGroup(blockName);
        final GeneralLogikBlock newBlock = newBlock(newGroup, null);
        return newBlock;
    }

    public LogikLine newMainLine(GeneralLogikBlock block) {
        LogikLine line = new LogikLine(nextLineIndex++, groups);
        block.addMainLine(line);
        lines.add(line);
        return line;
    }

    public LogikLine newSubLine(GeneralLogikBlock block) {
        LogikLine line = new LogikLine(nextLineIndex++, groups);
        block.addSubLine(line);
        lines.add(line);
        return line;
    }

    public Optional<LogikElement> getElement(String groupName, String elementName) {
        Optional<LogikGroup> logikGroup = findGroupByName(groupName);

        Optional<List<LogikElement>> logikElement = logikGroup.map(LogikGroup::getElements);
        return logikElement.orElse(new ArrayList<>()).stream().filter(o -> o.getName().equals(elementName)).findFirst();
    }

    private Optional<LogikGroup> findGroupByName(String groupName) {
        return groups.stream().filter(o -> o.getName().equals(groupName)).findFirst();
    }

    public Optional<LogikGroup> getGroup(String groupName) {
        return findGroupByName(groupName);
    }

    public List<LogikGroup> getGroups() {
        return groups;
    }

    public LogikLine getLine(int lineId) {
        return lines.stream().filter(o -> o.getLineId() == lineId).findFirst().orElse(null);
    }

    public LogikGroup getGroup(int groupId) {
        return groups.get(groupId);
    }

    public void updateSelection(LogikLine line, LogikGroup group, List<LogikElement> selectedElements) {
        final List<LogikElement> elements = line.getSelectables(group);
        elements.clear();
        elements.addAll(selectedElements);
    }

    public LogikLine getLastMainLine(GeneralLogikBlock block) {
        List<LogikLine> blockLines = block.getMainLines();
        return blockLines.get(blockLines.size() - 1);
    }

    public List<LogikLine> getLines() {
        return lines;
    }

    /*public void replaceLines(LogikLine findLine, List<LogikLine> duplicates) {
        lines.removeAll(duplicates);
        blocks.forEach(block -> {
            block.replaceLines(findLine, duplicates);
        });
    }*/
    public void setCaseData(String parentProblemKey, int line1Id, int line2Id) {
        this.parentProblemKey = parentProblemKey;
        this.line1Id = line1Id;
        this.line2Id = line2Id;
    }

    public GeneralLogikBlock findBlock(int blockId) {
        for (LogikOptionBlockGroup optionBlockGroup : optionBlockGroups) {
            for (final GeneralLogikBlock block : optionBlockGroup.getOptionBlocks()) {
                if (block.getBlockId() == blockId) {
                    return block;
                }
            }
        }
        return null;
    }

    public void handleFoundOption(final LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        combinationHandler.foundOption(blockGroup, block);
        final List<GeneralLogikBlock> otherBlocks = new ArrayList<>(blockGroup.getOptionBlocks());
        otherBlocks.remove(block);
        for (GeneralLogikBlock otherBlock : otherBlocks) {
            lines.removeIf(o -> otherBlock.getMainLines().contains(o));
            lines.removeIf(o -> otherBlock.getSubLines().contains(o));
            blockGroup.getOptionBlocks().remove(otherBlock);
        }
    }

    public void replaceLines(LogikLine findLine, List<LogikLine> duplicates) {
        // Check if replacement is not in options, is done before....
        lines.removeAll(duplicates);
        for (LogikOptionBlockGroup blockGroup : getOptionBlockGroups()) {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                block.replaceLines(findLine, duplicates);
            }
        }
    }

    @JsonIgnore
    public List<GeneralLogikBlock> getSingleBlocks() {
        return getOptionBlockGroups().stream().filter(o -> o.getOptionBlocks().size() == 1).map(o -> o.getOptionBlocks().get(0)).collect(Collectors.toList());
    }

    public LogikOptionBlockGroup findBlockGroup(Integer blockGroupId) {
        for (LogikOptionBlockGroup blockGroup : getOptionBlockGroups()) {

            if (blockGroup.getBlockGroupId() == blockGroupId) {
                return blockGroup;
            }

        }
        return null;
    }

    public boolean hasOptions() {
        return optionBlockGroups.stream().anyMatch(o -> o.getOptionBlocks().size() > 1);
    }

    public void addBlock(LogikOptionBlockGroup blockGroup, GeneralLogikBlock block) {
        blockGroup.append(block);
        combinationHandler.notifyNewBlock(blockGroup, block);
        for (LogikLine line : block.getMainLines()) {
            if (!lines.contains(line)) {
                lines.add(line);
            }
        }
        for (LogikLine line : block.getSubLines()) {
            if (!lines.contains(line)) {
                lines.add(line);
            }
        }
        if (block.getBlockId() >= nextBlockIndex) {
            nextBlockIndex = block.getBlockId() + 1;
        }
        nextLineIndex = lines.stream().mapToInt(LogikLine::getLineId).max().orElse(0) + 1;
    }

    public LineSearchResult searchBlock(Integer blockId) {
        boolean fromTrue = false;
        int pairIndex = -1;
        boolean pairTruth = false;
        for (final LogikOptionBlockGroup blockGroup : optionBlockGroups) {
            for (final GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                if (block.getBlockId() == blockId) {
                    fromTrue = blockGroup.getOptionBlocks().size() == 1;
                    pairIndex = block.getBlockId();
                    pairTruth = "wahr".equals(block.getSubName());
                }
            }
        }
        return new LineSearchResult(fromTrue, pairIndex, pairTruth);
    }

    public int excludeCombinations(List<Integer> sortedResults) {
        return combinationHandler.excludeCombinations(sortedResults);
    }

    public List<GeneralLogikBlock> checkNewSingleBlocksByCombinations() {
        return combinationHandler.checkNewSingleBlocksByCombinations();
    }

    public LogikOptionBlockGroup findBlockGroupForBlock(GeneralLogikBlock searchBlock) {
        for (final LogikOptionBlockGroup blockGroup : optionBlockGroups) {
            for (final GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                if (block == searchBlock) {
                    return blockGroup;
                }
            }
        }
        return null;
    }

    public void updateIndices() {
        nextLineIndex = lines.stream().mapToInt(LogikLine::getLineId).max().orElse(0) + 1;
        nextBlockGroupIndex = optionBlockGroups.stream().mapToInt(LogikOptionBlockGroup::getBlockGroupId).max().orElse(0) + 1;

        for (LogikOptionBlockGroup blockGroup : optionBlockGroups) {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                if (nextBlockIndex <= block.getBlockId()) {
                    nextBlockIndex = block.getBlockId() + 1;
                }
            }
        }
    }

    public void updateCombinations() {
        if (combinationHandler.getNonSingleBlockGroups().isEmpty()) {
            for (LogikOptionBlockGroup blockGroup : optionBlockGroups) {
                for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                    combinationHandler.notifyNewBlock(blockGroup, block);
                }
            }
        }
    }

    public CombinationView buildCombinationView() {
        return combinationHandler.buildCombinationView();
    }

    public LogikProblemGrid ensureGrid() {
        if (grid == null) {
            LogikProblemGrid grid = new LogikProblemGrid(this);
            this.grid = grid;

        }
        return grid;
    }
}
