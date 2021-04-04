/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem;

import de.sarux.logik.helper.application.detektor.CombinationView;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;

/**
 *
 * @author sarux
 */
@Getter
public class BlockCombinationHandler {

    private final List<LogikOptionBlockGroup> nonSingleBlockGroups = new ArrayList<>();
    private final List<List<GeneralLogikBlock>> possibleCombinations = new ArrayList<>();

    BlockCombinationHandler() {
    }

    void notifyNewBlock(LogikOptionBlockGroup optionBlockGroup, GeneralLogikBlock block) {
        int blockIndex = optionBlockGroup.getOptionBlocks().indexOf(block);

        int size = (blockIndex > -1) ? blockIndex + 1 : 1;

        switch (size) {
            case 1:
                break;
            case 2:
                addNewOptionGroup(optionBlockGroup, block);
                break;
            default:
                expandOptionGroup(optionBlockGroup, block);
        }
    }

    public void removeOption(LogikOptionBlockGroup optionBlockGroup, GeneralLogikBlock foundOption) {
        int size = optionBlockGroup.getOptionBlocks().size();
        int index = optionBlockGroup.getOptionBlocks().indexOf(foundOption);
        if (index > -1) {
            size--;
        }

        if (size == 1) {
            int groupIndex = findInsertIndex(optionBlockGroup);
            nonSingleBlockGroups.remove(groupIndex);
            final List<List<GeneralLogikBlock>> newList = new ArrayList<>();
            for (List<GeneralLogikBlock> compareList : possibleCombinations) {
                final List<GeneralLogikBlock> reducedList = new ArrayList<>(compareList);
                reducedList.remove(groupIndex);
                if (!isDuplicate(newList, reducedList)) {
                    newList.add(reducedList);
                }
            }
            possibleCombinations.clear();
            possibleCombinations.addAll(newList);
        } else {
            possibleCombinations.removeIf(combination -> {
                return combination.indexOf(foundOption) == -1;
            });
        }
    }

    public void foundOption(LogikOptionBlockGroup optionBlockGroup, GeneralLogikBlock foundOption) {
        int groupIndex = findInsertIndex(optionBlockGroup);
        nonSingleBlockGroups.remove(groupIndex);
        final List<List<GeneralLogikBlock>> newList = new ArrayList<>();
        for (List<GeneralLogikBlock> compareList : possibleCombinations) {
            final List<GeneralLogikBlock> reducedList = new ArrayList<>(compareList);
            reducedList.remove(groupIndex);
            if (!isDuplicate(newList, reducedList)) {
                newList.add(reducedList);
            }
        }
        possibleCombinations.clear();
        possibleCombinations.addAll(newList);
    }

    private void addNewOptionGroup(LogikOptionBlockGroup optionBlockGroup, GeneralLogikBlock block) {
        if (possibleCombinations.isEmpty()) {
            List<GeneralLogikBlock> blockList = new ArrayList<>();
            blockList.add(optionBlockGroup.getOptionBlocks().get(0));
            possibleCombinations.add(blockList);

            blockList = new ArrayList<>();
            blockList.add(block);
            possibleCombinations.add(blockList);

            nonSingleBlockGroups.add(optionBlockGroup);
        } else {
            int index = findInsertIndex(optionBlockGroup);
            nonSingleBlockGroups.add(index, optionBlockGroup);

            List<List<GeneralLogikBlock>> newList = new ArrayList<>();
            for (List<GeneralLogikBlock> blockList : possibleCombinations) {
                List<GeneralLogikBlock> expandedBlockList = new ArrayList<>(blockList);
                expandedBlockList.add(index, optionBlockGroup.getOptionBlocks().get(0));
                newList.add(expandedBlockList);

                expandedBlockList = new ArrayList<>(blockList);
                expandedBlockList.add(index, block);
                newList.add(expandedBlockList);
            }

            possibleCombinations.clear();
            possibleCombinations.addAll(newList);
        }
    }

    private void expandOptionGroup(LogikOptionBlockGroup optionBlockGroup, GeneralLogikBlock block) {
        int index = findInsertIndex(optionBlockGroup);

        List<List<GeneralLogikBlock>> newList = new ArrayList<>();
        for (List<GeneralLogikBlock> blockList : possibleCombinations) {
            newList.add(blockList);
            List<GeneralLogikBlock> expandedBlockList = new ArrayList<>(blockList);
            expandedBlockList.remove(index);
            expandedBlockList.add(index, block);
            if (!isDuplicate(newList, expandedBlockList)) {
                newList.add(expandedBlockList);
            }
        }

        possibleCombinations.clear();
        possibleCombinations.addAll(newList);
    }

    private int findInsertIndex(LogikOptionBlockGroup optionBlockGroup) {
        for (int i = 0; i < nonSingleBlockGroups.size(); i++) {
            if (nonSingleBlockGroups.get(i).getBlockGroupId() >= optionBlockGroup.getBlockGroupId()) {
                return i;
            }
        }
        return nonSingleBlockGroups.size();
    }

    private boolean isDuplicate(List<List<GeneralLogikBlock>> newList, List<GeneralLogikBlock> expandedBlockList) {
        for (List<GeneralLogikBlock> compareList : newList) {
            boolean isDuplicate = true;
            for (int i = 0; i < compareList.size(); i++) {
                if (expandedBlockList.get(i) != compareList.get(i)) {
                    isDuplicate = false;
                    break;
                }
            }
            if (isDuplicate) {
                return true;
            }
        }
        return false;
    }

    int excludeCombinations(List<Integer> sortedResults) {
        final List<List<GeneralLogikBlock>> removedCombinations = new ArrayList<>();
        for (List<GeneralLogikBlock> combination : possibleCombinations) {
            if (isRemoved(combination, sortedResults)) {
                removedCombinations.add(combination);
            }
        }

        possibleCombinations.removeAll(removedCombinations);
        return possibleCombinations.size();
    }

    private boolean isRemoved(List<GeneralLogikBlock> combination, List<Integer> sortedResults) {
        final List<Integer> blockIds = combination.stream().map(o -> o.getBlockId()).collect(Collectors.toList());
        return sortedResults.stream().allMatch(o -> blockIds.contains(o));
    }

    List<GeneralLogikBlock> checkNewSingleBlocksByCombinations() {
        List<GeneralLogikBlock> newSingles = new ArrayList<>();

        List<GeneralLogikBlock> firstCombination = possibleCombinations.get(0);
        int length = firstCombination.size();
        for (int i = 0; i < length; i++) {
            boolean allEqual = true;
            GeneralLogikBlock firstBlock = firstCombination.get(i);

            for (int j = 1; j < possibleCombinations.size(); j++) {
                if (possibleCombinations.get(j).get(i) != firstBlock) {
                    allEqual = false;
                    break;
                }
            }
            if (allEqual) {
                newSingles.add(firstBlock);
            }
        }
        return newSingles;
    }

    CombinationView buildCombinationView() {
        final List<String> blockIds = new ArrayList<>();
        for (LogikOptionBlockGroup blockGroup : nonSingleBlockGroups) {
            String name = blockGroup.getName();
            blockIds.add(name);
        }

        final List<List<Boolean>> truths = buildTruths();
        return new CombinationView(blockIds, truths);
    }

    private List<List<Boolean>> buildTruths() {
        final List<List<Boolean>> truths = new ArrayList<>();
        for (List<GeneralLogikBlock> combination : possibleCombinations) {
            List<Boolean> booleanCombination = new ArrayList<>();
            for (int i = 0; i < combination.size(); i++) {
                final GeneralLogikBlock block = combination.get(i);
                boolean isTrue = "wahr".equals(block.getSubName());
                booleanCombination.add(isTrue);
            }
            truths.add(booleanCombination);
        }
        return truths;
    }

}
