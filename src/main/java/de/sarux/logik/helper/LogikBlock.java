package de.sarux.logik.helper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.sarux.logik.helper.group.LogikGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property="@id")
public class LogikBlock {
    private String name;
    private int blockId;
    private final List<LogikLine> mainLines = new ArrayList<>();
    private final List<LogikLine> subLines = new ArrayList<>();
    private boolean noDuplicates;

    private final List<LogikLineRelation> relations = new ArrayList<>();

    public LogikBlock(int blockId, String name) {
        this.blockId = blockId;
        this.name = name;
    }

    LogikBlock(int blockId, String name, boolean noDuplicates) {
        this(blockId, name);
        this.noDuplicates = noDuplicates;
    }

    public void addMainLine(LogikLine line) {
        mainLines.add(line);
    }

    public void addSubLine(LogikLine line) {
        subLines.add(line);
    }

    public LogikLineRelation newRelation(LogikLine leftLine, LogikGroup leftGroup, LogikLine rightLine, LogikGroup rightGroup, LogikRelationType relationType, String relationHint, boolean isSubLine) {
        LogikLineRelation relation = new LogikLineRelation(relations.size(), leftLine, leftGroup, rightLine, rightGroup, relationType, relationHint, isSubLine);
        relations.add(relation);
        return relation;
    }

    public void flip() {
// Flip relations
        relations.forEach(r -> {
            if (!r.isSubRelation()) {
                r.flip();
            }
        });

        // Reverse main lines
        Collections.reverse(mainLines);
    }

    public void replaceLines(LogikLine findLine, List<LogikLine> duplicates) {
        mainLines.replaceAll(o -> {
            if (duplicates.contains(o)) return findLine;
            else return o;
        });
        subLines.replaceAll(o -> {
            if (duplicates.contains(o)) return findLine;
            else return o;
        });

        for (LogikLineRelation relation : relations) {
            if (duplicates.contains(relation.getLeftLine()))
                relation.setLeftLine(findLine);
            if (duplicates.contains(relation.getRightLine()))
                relation.setRightLine(findLine);
        }
    }

    public Optional<LogikLineRelation> findRelation(Integer leftLineId, Integer rightLineId, LogikGroup groupFrom) {
        return relations.stream().filter(o -> o.getLeftLine().getLineId() == leftLineId && o.getRightLine().getLineId() == rightLineId && o.getLeftGroup() == groupFrom).findFirst();
    }
}
