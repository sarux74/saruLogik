package de.sarux.logik.helper.problem.solve;

import de.sarux.logik.helper.application.LogikException;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.LogikLineRelation;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;
import de.sarux.logik.helper.problem.LogikRelationType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SolveHelper {

    private final GeneralLogikProblem problem;

    public SolveHelper(GeneralLogikProblem problem) {
        this.problem = problem;
    }

    public ChangeResult findNegatives(LogikLine findLine) {

        ChangeResult result = new ChangeResult();

        final LogikOptionBlockGroup findBlockGroup = findBlockGroupForLine(problem, findLine);

        // First try to reduce
        // Only lines in block groups with single option
        List<GeneralLogikBlock> trueBlock = problem.getOptionBlockGroups().stream().filter(o -> o.getOptionBlocks().size() == 1).map(o -> o.getOptionBlocks().get(0)).collect(Collectors.toList());
        
        // For non single blocks, the lines of the same block are allowed
        GeneralLogikBlock ownBlock = null;
        if(findBlockGroup.getOptionBlocks().size() > 1) {
            for(GeneralLogikBlock block: findBlockGroup.getOptionBlocks()) {
                if(block.getMainLines().contains(findLine) || block.getSubLines().contains(findLine)) {
                    ownBlock = block;
                    trueBlock.add(block);
                    break;
                }
            }
        }
        List<LogikLine> trueLines = Stream.concat(trueBlock.stream().map(o -> o.getMainLines()), trueBlock.stream().map(o -> o.getSubLines())).flatMap(List::stream).distinct().collect(Collectors.toList());
        
        for (LogikLine line : trueLines) {
            if (line != findLine) {
                boolean isExcluded = checkExcluded(line, findLine);
                if (isExcluded) {
                    List<LogikElement> singles = findSingles(line);
                    if (!singles.isEmpty()) {
                        updateExclusion(singles, findLine, result);
                    }
                }
            }
        }

        List<LogikElement> singles = findSingles(findLine);
        if (singles.isEmpty()) {
            return result;
        }

        // Find in lines
        // Only if line with negatives is part of a single option group
        if (findBlockGroup.getOptionBlocks().size() == 1) {
            for (LogikLine line : problem.getLines()) {
                if (line != findLine) {
                    boolean isExcluded = checkExcluded(line, findLine);
                    if (isExcluded) {
                        updateExclusion(singles, line, result);
                    }
                }
            }
        } else if(ownBlock != null) {
            for (LogikLine line : ownBlock.getMainLines()) {
                if (line != findLine) {
                    boolean isExcluded = checkExcluded(line, findLine);
                    if (isExcluded) {
                        updateExclusion(singles, line, result);
                    }
                }
            }
            for (LogikLine line : ownBlock.getSubLines()) {
                if (line != findLine) {
                    boolean isExcluded = checkExcluded(line, findLine);
                    if (isExcluded) {
                        updateExclusion(singles, line, result);
                    }
                }
            }
        }

        // Find in relations
        // All allowed
        for (LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                for (LogikLineRelation relation : block.getRelations()) {
                    if (relation.getLeftLine() == findLine) {
                        boolean isExcluded = relation.getLeftGroup() == relation.getRightGroup() && checkExcludingRelation(relation.getType());
                        if (isExcluded) {
                            updateExclusion(singles, relation.getRightLine(), result);
                        }
                    } else if (relation.getRightLine() == findLine) {
                        boolean isExcluded = relation.getLeftGroup() == relation.getRightGroup() && checkExcludingRelation(relation.getType());
                        if (isExcluded) {
                            updateExclusion(singles, relation.getLeftLine(), result);
                        }
                    }
                }
            }
        }
        
        // TODO: Gleiche singles zwischen single option und non-single option abgleichen
        return result;
    }

    private void updateExclusion(List<LogikElement> singles, LogikLine rightLine, ChangeResult result) {
        boolean allFound = false;

        for (LogikElement single : singles) {
            List<LogikElement> selectables = rightLine.getSelectables(single.getGroup());
            boolean found = selectables.remove(single);
            if (found) {
                result.getChangedLines().add(rightLine.getLineId());
                String message = "Entferne " + single + " aus Zeile " + rightLine.getLineId();
                result.getRemoveMessages().add(message);
                if (selectables.size() == 1) {
                    message = "Gefunden: Zeile " + rightLine.getLineId() + ", Gruppe " + single.getGroup().getName() + " ist " + selectables.get(0).getName();
                    result.getFoundMessages().add(message);
                }
            }
            allFound |= found;
        }
    }

    private boolean checkExcludingRelation(LogikRelationType type) {
        switch (type) {
            case PREVIOUS:
            case NEXT:
            case PLUS_MINUS:
            case NOT_EQUAL:
                return true;
            default:
                return false;
        }
    }

    private boolean checkExcluded(LogikLine line, LogikLine findLine) {
// First check non duplicate blocks
        for (LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                if (block.isNoDuplicates() && block.getMainLines().contains(line) && block.getMainLines().contains(findLine)) {
                    return true;
                }
            }
        }

        // Check if some group excludes both lines
        for (LogikGroup group : problem.getGroups()) {
            int counter = 0;
            List<LogikElement> selectables = line.getSelectables(group);
            List<LogikElement> findSelectables = findLine.getSelectables(group);
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

    private List<LogikElement> findSingles(LogikLine line) {
        final List<LogikElement> singles = new ArrayList<>();
        for (LogikGroup group : problem.getGroups()) {
            List<LogikElement> selectables = line.getSelectables(group);
            if (selectables.size() == 1) {
                singles.add(selectables.get(0));
            }
        }
        return singles;
    }

    public ChangeResult findPositives(LogikLine findLine, List<LogikLine> additionalLines) throws LogikException {
        ChangeResult result = new ChangeResult();

        List<LogikElement> singles = findSingles(findLine);
        if (singles.isEmpty() && additionalLines == null) {
            return result;
        }

        // Find lines that are allowed to merge
        final List<LogikLine> duplicates = new ArrayList<>();
        final LogikOptionBlockGroup blockGroup = findBlockGroupForLine(problem, findLine);
        List<LogikLine> allowedMergeLines = null;
        if (blockGroup.getOptionBlocks().size() == 1) {
            List<GeneralLogikBlock> trueBlock = problem.getOptionBlockGroups().stream().filter(o -> o.getOptionBlocks().size() == 1).map(o -> o.getOptionBlocks().get(0)).collect(Collectors.toList());

            allowedMergeLines = Stream.concat(trueBlock.stream().map(o -> o.getMainLines()), trueBlock.stream().map(o -> o.getSubLines())).flatMap(List::stream).distinct().collect(Collectors.toList());
        } else {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                if (block.getMainLines().contains(findLine) || block.getSubLines().contains(findLine)) {
                    allowedMergeLines = new ArrayList<>(block.getMainLines());
                    allowedMergeLines.addAll(block.getSubLines());
                    break;
                }
            }
        }

        if (allowedMergeLines != null && !allowedMergeLines.isEmpty()) {
            if (!singles.isEmpty()) {

                for (LogikLine line : allowedMergeLines) {
                    if (line != findLine) {
                        boolean isDuplicate = checkDuplicate(line, singles);
                        if (isDuplicate) {
                            duplicates.add(line);
                        }
                    }
                }

            }

            if (additionalLines != null) {
                for (LogikLine line : additionalLines) {
                    if (!duplicates.contains(line) && allowedMergeLines.contains(line)) {
                        duplicates.add(line);
                    }
                }
            }
        }
        
        if (!duplicates.isEmpty()) {

            // Find min index
            Optional<LogikLine> minLine = duplicates.stream().min(Comparator.comparing(LogikLine::getLineId));
            if (minLine.get().getLineId() < findLine.getLineId()) {
                duplicates.add(findLine);
                duplicates.remove(minLine.get());
                findLine = minLine.get();
            }

            mergeLines(findLine, duplicates);
            problem.replaceLines(findLine, duplicates);

            result.getChangedLines().add(findLine.getLineId());
            duplicates.forEach(o -> result.getChangedLines().add(o.getLineId()));

            String message = "Vereinige Zeile " + findLine.getLineId() + " mit ";
            String duplicateList = duplicates.stream().map(o -> Integer.toString(o.getLineId())).collect(Collectors.joining(", "));
            message += duplicateList;
            result.getRemoveMessages().add(message);
        }
        return result;
    }

    private void mergeLines(LogikLine findLine, List<LogikLine> duplicates) throws LogikException {
        Set<LogikElement> toUnselect = new HashSet<>();
        for (LogikLine duplicate : duplicates) {
            for (LogikGroup group : problem.getGroups()) {
                List<LogikElement> selectables = findLine.getSelectables(group);
                List<LogikElement> duplicateSelectables = duplicate.getSelectables(group);
                for (LogikElement element : selectables) {
                    if (!duplicateSelectables.contains(element)) {
                        toUnselect.add(element);
                    }
                }
            }
        }

        for (LogikElement unselect : toUnselect) {
            findLine.unselect(unselect);
        }
    }

    private boolean checkDuplicate(LogikLine line, List<LogikElement> singles) {
        for (LogikElement single : singles) {
            List<LogikElement> selectables = line.getSelectables(single.getGroup());
            if (selectables.size() == 1 && selectables.get(0) == single) {
                return true;
            }
        }
        return false;
    }

    public boolean mergeLines(LogikLine line1, LogikLine line2) throws LogikException {
        List<LogikLine> duplicates = new ArrayList<>();
        duplicates.add(line2);
        mergeLines(line1, duplicates);
        problem.replaceLines(line1, duplicates);
        return true;
    }

    public void checkSolvability(GeneralLogikProblem problem) throws LogikException {
        for (LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                for (LogikLine line : block.getMainLines()) {
                    for (List<LogikElement> elements : line.getSelectableElements()) {
                        if (elements.isEmpty()) {
                            // TODO: For options remove this one....
                            throw new LogikException("Zeile " + line.getLineId() + " hat keine Wahlmöglichkeiten mehr!");
                        }
                    }
                }

            }
        }

        for (LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            for (GeneralLogikBlock logikBlock : blockGroup.getOptionBlocks()) {
                if (logikBlock.isNoDuplicates()) {
                    List<LogikLine> duplicates = logikBlock.getMainLines().stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                            .entrySet()
                            .stream()
                            .filter(p -> p.getValue() > 1)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toList());
                    if (!duplicates.isEmpty()) {
                        String blockName = blockGroup.getName();
                        if (logikBlock.getSubName() != null) {
                            blockName += " " + logikBlock.getSubName();
                        }
                        throw new LogikException("Singulärer Block " + blockName + " hat die Duplikatzeile " + duplicates.get(0).getLineId());
                    }
                }
            }
        }
    }

    private LogikOptionBlockGroup findBlockGroupForLine(GeneralLogikProblem problem, LogikLine findLine) {
        for (LogikOptionBlockGroup blockGroup : problem.getOptionBlockGroups()) {
            for (GeneralLogikBlock block : blockGroup.getOptionBlocks()) {
                for (LogikLine line : block.getMainLines()) {
                    if (line == findLine) {
                        return blockGroup;
                    }
                }
                for (LogikLine line : block.getSubLines()) {
                    if (line == findLine) {
                        return blockGroup;
                    }
                }
            }
        }

        // Should not be reached
        return null;
    }
}
