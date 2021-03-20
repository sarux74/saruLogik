package de.sarux.logik.helper;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.sarux.logik.helper.application.LogikBlock;
import de.sarux.logik.helper.application.LogikElement;
import de.sarux.logik.helper.application.LogikLine;
import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public class LogikProblem {
    private List<LogikGroup> groups;
    private List<LogikBlock> blocks = new ArrayList<>();
    private List<LogikLine> lines = new ArrayList<>();

    // For case
    @Getter
    private String parentProblemKey;
    @Getter
    private int line1Id;
    @Getter
    private int line2Id;

    public LogikProblem(List<LogikGroup> groups) {
        this.groups = groups;
    }

    public LogikBlock newBlock(String name) {
        LogikBlock block = new LogikBlock(blocks.size(), name);
        blocks.add(block);
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

    public List<LogikGroup> getGroups() {
        return groups;
    }

    public void updateGroup(LogikGroup group) {
        int index = group.getIndex();
        if (index == groups.size())
            groups.add(group);
        else {
            LogikGroup orig = groups.get(group.getIndex());
            orig.setName(group.getName());
            List<LogikElement> elements = group.getElements();
            List<LogikElement> origElements = orig.getElements();
            for (int i = 0; i < elements.size(); i++) {
                if (orig.getElements().size() > i) {
                    origElements.get(i).setName(elements.get(i).getName());
                    origElements.get(i).setShortName(elements.get(i).getShortName());
                } else {
                    origElements.add(elements.get(i));
                }
            }
            while (origElements.size() > elements.size()) {
                origElements.remove(origElements.size() - 1);
            }
        }
    }

    public List<LogikBlock> getBlocks() {
        return blocks;
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
        for (LogikBlock block : blocks) {
            block.replaceLines(findLine, duplicates);
        }
    }

    public void setCaseData(String parentProblemKey, int line1Id, int line2Id) {
        this.parentProblemKey = parentProblemKey;
        this.line1Id = line1Id;
        this.line2Id = line2Id;
    }
}
