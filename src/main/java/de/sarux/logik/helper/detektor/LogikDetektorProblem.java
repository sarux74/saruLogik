package de.sarux.logik.helper.detektor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.sarux.logik.helper.LogikBlock;
import de.sarux.logik.helper.LogikElement;
import de.sarux.logik.helper.LogikLine;
import de.sarux.logik.helper.group.LogikGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
@Getter
public class LogikDetektorProblem {
    private List<LogikGroup> groups;
    private List<LogikBlock> trueBlocks = new ArrayList<>();
    private List<LogikBlockPair> blockPairs = new ArrayList<>();
    private List<LogikLine> lines = new ArrayList<>();

    private List<List<LogikBlock>> possibleCombinations = new ArrayList<>();

    private List<String> blockNames = new ArrayList<>();

    public LogikDetektorProblem(final List<LogikGroup> groups) {
        this.groups = groups;
    }

    public LogikBlock newBlock(String name) {
        LogikBlock block = new LogikBlock(trueBlocks.size(), name);
        trueBlocks.add(block);
        blockNames.add(name);
        return block;
    }

    public LogikLine newMainLine(LogikBlock block) {
        OptionalInt newIndex = lines.stream().mapToInt(LogikLine::getLineId).max();
        LogikLine line = new LogikLine(newIndex.orElse(0) + 1, groups);
        block.addMainLine(line);
        lines.add(line);
        return line;
    }

    public LogikLine newSubLine(LogikBlock block) {
        OptionalInt newIndex = lines.stream().mapToInt(LogikLine::getLineId).max();
        LogikLine line = new LogikLine(newIndex.orElse(0) + 1, groups);
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

    public LogikLine getLastMainLine(LogikBlock block) {
        List<LogikLine> blockLines = block.getMainLines();
        return blockLines.get(blockLines.size() - 1);
    }

    public List<LogikLine> getLines() {
        return lines;
    }

    public void replaceLines(LogikLine findLine, List<LogikLine> duplicates) {
        lines.removeAll(duplicates);
        for (LogikBlock block : trueBlocks) {
            block.replaceLines(findLine, duplicates);
        }
    }

    @JsonIgnore
    public boolean isEmpty() {
        return blockPairs.isEmpty();
    }

    public LogikBlockPair newBlockPair(String blockName) {
        int index = trueBlocks.size() + blockPairs.size() * 2;
        LogikBlock trueBlock = new LogikBlock(index, blockName + " wahr");
        LogikBlock falseBlock = new LogikBlock(index + 1, blockName + " falsch");
        LogikBlockPair pair = new LogikBlockPair(trueBlock, falseBlock);
        blockPairs.add(pair);

        if (possibleCombinations.isEmpty()) {
            for (LogikBlockPair currentPair : blockPairs) {
                expandCombinations(currentPair);
            }
        } else {
            expandCombinations(pair);
        }
        blockNames.add(blockName);
        return pair;
    }

    private void expandCombinations(LogikBlockPair currentPair) {
        if (possibleCombinations.isEmpty()) {
            List<LogikBlock> blockList = new ArrayList<>();
            blockList.add(currentPair.getTrueBlock());
            possibleCombinations.add(blockList);
            blockList = new ArrayList<>();
            blockList.add(currentPair.getFalseBlock());
            possibleCombinations.add(blockList);
        } else {
            List<List<LogikBlock>> newList = new ArrayList<>();
            for (List<LogikBlock> existingBlock : possibleCombinations) {
                List<LogikBlock> expandedBlock = new ArrayList<>(existingBlock);
                expandedBlock.add(currentPair.getTrueBlock());
                newList.add(expandedBlock);

                expandedBlock = new ArrayList<>(existingBlock);
                expandedBlock.add(currentPair.getFalseBlock());
                newList.add(expandedBlock);
            }

            possibleCombinations.clear();
            possibleCombinations.addAll(newList);
        }
    }

    public LogikBlock findBlock(int blockId) {
        Optional<LogikBlock> block = trueBlocks.stream().filter(o -> o.getBlockId() == blockId).findFirst();
        if (block.isPresent()) return block.get();

        for (LogikBlockPair pair : blockPairs) {
            if (pair.getTrueBlock().getBlockId() == blockId) return pair.getTrueBlock();
            if (pair.getFalseBlock().getBlockId() == blockId) return pair.getFalseBlock();
        }
        return null;
    }

    @JsonIgnore
    public List<LogikBlock> getAllBlocks() {
        final List<LogikBlock> allBlocks = new ArrayList<>(trueBlocks);
        for (LogikBlockPair pair : blockPairs) {
            allBlocks.add(pair.getTrueBlock());
            allBlocks.add(pair.getFalseBlock());
        }
        return allBlocks;
    }

    private boolean blockContainsLine(LogikBlock block, Integer lineId) {
        boolean inMainLines = block.getMainLines().stream().anyMatch(o -> o.getLineId() == lineId);
        if (inMainLines)
            return true;
        return block.getSubLines().stream().anyMatch(o -> o.getLineId() == lineId);
    }

    public void moveLogikBlock(int index, LogikBlock block) {
        blockPairs.remove(index);

        int blockIndex = findBlockIndex(block.getName());
        int insertIndex = 0;
        for (LogikBlock logikBlock : trueBlocks) {
            int compareIndex = findBlockIndex(logikBlock.getName());
            if (blockIndex < compareIndex) {
                break;
            }
            insertIndex++;
        }

        trueBlocks.add(insertIndex, block);
        for (List<LogikBlock> combination : possibleCombinations) {
            combination.remove(index);
        }

    }

    private int findBlockIndex(String name) {
        String shortName = name;
        if (shortName.endsWith(" wahr"))
            shortName = shortName.substring(0, shortName.length() - 5);
        else if (shortName.endsWith(" falsch"))
            shortName = shortName.substring(0, shortName.length() - 7);

        int index = 0;
        for (String blockName : blockNames) {
            if (blockName.equals(shortName))
                return index;
            index++;
        }
        return -1;
    }

    public LineSearchResult searchBlock(Integer blockId) {
        boolean fromTrue = false;
        int pairIndex = -1;
        boolean pairTruth = false;
        for (LogikBlock block : trueBlocks) {
            if (block.getBlockId() == blockId) {
                fromTrue = true;
                break;
            }
        }

        if (!fromTrue) {
            for (int i = 0; i < blockPairs.size(); i++) {
                LogikBlockPair pair = blockPairs.get(i);
                if (pair.getTrueBlock().getBlockId() == blockId) {
                    pairIndex = i;
                    pairTruth = true;
                    break;
                } else if (pair.getFalseBlock().getBlockId() == blockId) {
                    pairIndex = i;
                    pairTruth = false;
                    break;
                }
            }
        }
        return new LineSearchResult(fromTrue, pairIndex, pairTruth);
    }

    public void checkBlockNames() {
        if (blockNames.isEmpty()) {
            for (LogikBlock logikBlock : trueBlocks)
                blockNames.add(logikBlock.getName());
            for (LogikBlockPair pair : blockPairs) {
                String name = pair.getTrueBlock().getName();
                name = name.substring(0, name.length() - 5);
                blockNames.add(name);
            }
        }
    }
}
