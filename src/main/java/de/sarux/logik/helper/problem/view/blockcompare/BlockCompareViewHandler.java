/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem.view.blockcompare;

import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewBuilder;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewLine;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 *
 * @author sarux
 */
public class BlockCompareViewHandler {

    private final GeneralLogikProblem problem;

    public BlockCompareViewHandler(GeneralLogikProblem problem) {
        this.problem = problem;
    }

    
    // TODO: Weniger Spalten akzeptieren und mit ? auffüllen
    public List<IdNamePair> getComparableBlocks() {
        OptionalInt numGroups = problem.getGroups().stream().mapToInt(o -> o.getElements().size()).min();
        List<IdNamePair> comparableBlocks;

        if (numGroups.isPresent()) {
            comparableBlocks = problem.getSingleBlocks().stream()
                    .filter(o -> o.isNoDuplicates() && o.getMainLines().size() == numGroups.getAsInt())
                    .map(o -> new IdNamePair(o.getBlockId(), problem.findBlockGroupForBlock(o).getName()))
                    .collect(Collectors.toList());
        } else {
            comparableBlocks = new ArrayList<>();
        }

        return comparableBlocks;
    }

    public BlockCompareView buildBlockCompareView(int block1Id, int block2Id) {
        final BlockCompareView blockCompareView = new BlockCompareView();
        final List<GeneralLogikBlockViewLine> viewLines = new ArrayList<>();
        final GeneralLogikBlock block1 = problem.findBlock(block1Id);
        final GeneralLogikBlock block2 = problem.findBlock(block2Id);

        final LogikOptionBlockGroup blockGroup1 = problem.findBlockGroupForBlock(block1);
        final LogikOptionBlockGroup blockGroup2 = problem.findBlockGroupForBlock(block2);
        GeneralLogikBlockViewLine blockLine = GeneralLogikBlockViewBuilder.buildBlockViewLine(blockGroup1, block1);
        viewLines.add(blockLine);
        for (LogikLine line : block1.getMainLines()) {
            GeneralLogikBlockViewLine viewLine = GeneralLogikBlockViewBuilder.buildLineViewLine(problem.getGroups(), blockGroup1, block1, line);
            viewLines.add(viewLine);
        }

        GeneralLogikBlockViewLine separatorLine = GeneralLogikBlockViewBuilder.buildMainSeparatorLine(blockGroup1, block1);
        viewLines.add(separatorLine);

        blockLine = GeneralLogikBlockViewBuilder.buildBlockViewLine(blockGroup2, block2);
        viewLines.add(blockLine);
        for (LogikLine line : block2.getMainLines()) {
            GeneralLogikBlockViewLine viewLine = GeneralLogikBlockViewBuilder.buildLineViewLine(problem.getGroups(), blockGroup2, block2, line);
            viewLines.add(viewLine);
        }

        separatorLine = GeneralLogikBlockViewBuilder.buildMainSeparatorLine(blockGroup2, block2);
        viewLines.add(separatorLine);

        List<Integer> block1LineIds = block2.getMainLines().stream().map(LogikLine::getLineId).collect(Collectors.toList());
        List<Integer> block2LineIds = block1.getMainLines().stream().map(LogikLine::getLineId).collect(Collectors.toList());

        List<Integer> sameLines = new ArrayList<>(block1LineIds);
        sameLines.removeIf(o -> !block2LineIds.contains(o));

        int size = block1LineIds.size();
        int reducedSize = block1LineIds.size() - sameLines.size();
        boolean[][] proposed = new boolean[reducedSize][reducedSize];

        int rowIndex = 0;
        for (int i = 0; i < size; i++) {
            LogikLine line1 = block1.getMainLines().get(i);
            if (!sameLines.contains(line1.getLineId())) {
                int colIndex = 0;

                for (int j = 0; j < size; j++) {
                    LogikLine line2 = block2.getMainLines().get(j);
                    if (!sameLines.contains(line2.getLineId())) {
                        boolean propose = !isExcluded(problem.getGroups(), line1, line2);
                        proposed[rowIndex][colIndex] = propose;
                        colIndex++;
                    }
                }
                rowIndex++;
            }
        }

        blockCompareView.setViewLines(viewLines);
        block1LineIds.removeAll(sameLines);
        blockCompareView.setBlock1LineIds(block1LineIds);
        block2LineIds.removeAll(sameLines);
        blockCompareView.setBlock2LineIds(block2LineIds);
        blockCompareView.setProposed(proposed);
        return blockCompareView;
    }

    private boolean isExcluded(List<LogikGroup> groups, LogikLine line1, LogikLine line2) {
        // Check if some group excludes both lines
        for (LogikGroup group : groups) {
            int counter = 0;
            List<LogikElement> selectables = line1.getSelectables(group);
            List<LogikElement> findSelectables = line2.getSelectables(group);
            for (LogikElement element : selectables) {
                if (findSelectables.contains(element)) {
                    counter++;
                    break;
                }
            }
            if (counter == 0) {
                return true;
            }
        }
        return false;
    }

}
