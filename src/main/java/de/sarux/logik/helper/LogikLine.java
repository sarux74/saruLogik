package de.sarux.logik.helper;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.sarux.logik.helper.group.LogikGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property="@id")
public class LogikLine {
    private int lineId;
    private List<List<LogikElement>> selectableElements = new ArrayList<>();


    public LogikLine(int lineId, final List<LogikGroup> groups) {
        this.lineId = lineId;
        for (LogikGroup group : groups) {
            List<LogikElement> selectable = new ArrayList<>(group.getElements());
            selectableElements.add(selectable);
        }
    }

    public void select(LogikElement logikElement) throws LogikException {
        LogikGroup group = logikElement.getGroup();
        List<LogikElement> selectable = selectableElements.get(group.getIndex());
        selectable.removeIf(s -> s != logikElement);
        if (selectable.isEmpty())
            throw new LogikException("Gewähltes Element " + logikElement.getName() + " von Gruppe " + group.getName() + " war in Zeile " + lineId + " bereits abgewählt");
    }

    public List<LogikElement> getSelectables(LogikGroup logikGroup) {
        return selectableElements.get(logikGroup.getIndex());
    }

    public void unselect(LogikElement logikElement) throws LogikException {
        LogikGroup group = logikElement.getGroup();
        List<LogikElement> selectable = selectableElements.get(group.getIndex());
        selectable.remove(logikElement);
        if (selectable.isEmpty())
            throw new LogikException("Gewähltes Element " + logikElement.getName() + " von Gruppe " + group.getName() + " war in Zeile " + lineId + " bereits abgewählt");

    }

    public void copyFrom(LogikLine line) {
        selectableElements.clear();
        for(List<LogikElement> elementList: line.getSelectableElements()) {
            List<LogikElement> copiedElements = new ArrayList<>(elementList);
            selectableElements.add(copiedElements);
        }
        lineId = line.getLineId();
    }
}
