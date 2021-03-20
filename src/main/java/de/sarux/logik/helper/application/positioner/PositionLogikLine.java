package de.sarux.logik.helper.application.positioner;

import de.sarux.logik.helper.application.LogikElement;
import de.sarux.logik.helper.application.LogikException;
import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
public class PositionLogikLine {
    private int lineId;
    private Map<LogikElement, List<LogikElement>> selectableElements = new HashMap<>();

    public PositionLogikLine(int lineId, LogikGroup positionGroup, LogikGroup positionedGroup) {
        this.lineId = lineId;
        for (LogikElement element : positionGroup.getElements()) {
            List<LogikElement> positionedElements = new ArrayList<>(positionedGroup.getElements());
            selectableElements.put(element, positionedElements);
        }
    }

    public PositionLogikLine(int lineId, PositionLogikLine positionLogikLine) {
        this.lineId = lineId;
        for (LogikElement element : positionLogikLine.selectableElements.keySet()) {
            List<LogikElement> positionedElements = new ArrayList<>(positionLogikLine.selectableElements.get(element));
            selectableElements.put(element, positionedElements);
        }
    }

    public  boolean select(LogikElement positionElement, LogikElement positionedElement) throws LogikException {
        if (!selectableElements.containsKey(positionElement))
            throw new LogikException("Position enthält nicht " + positionElement.getName());

        List<ElementPair> selectPairs = new ArrayList<>();
        selectPairs.add(new ElementPair(positionElement, positionedElement));
        while(!selectPairs.isEmpty()) {
            ElementPair pair = selectPairs.remove(0);
            LogikElement selectedPositionElement = pair.getPositionElement();
            LogikElement selectedPositionedElement = pair.getPositionedElement();

            for (Map.Entry<LogikElement, List<LogikElement>> entry : selectableElements.entrySet()) {
                if (entry.getKey() == selectedPositionElement) {
                    if (!entry.getValue().contains(selectedPositionedElement))
                        return false;

                    entry.getValue().clear();
                    entry.getValue().add(selectedPositionedElement);
                } else {
                    boolean moreThan1 = entry.getValue().size() > 1;
                    entry.getValue().remove(selectedPositionedElement);
                    // If reduced to a singleton, unselect it in other positions
                    if(moreThan1 && entry.getValue().size() == 1) {
                        selectPairs.add(new ElementPair(entry.getKey(), entry.getValue().get(0)));
                    }
                }
            }
        }
        return true;
    }

    public List<LogikElement> getSelectables(LogikElement element) {
        return selectableElements.get(element);
    }

    public void unselect(LogikElement positionElement, LogikElement positionedElement) throws LogikException {
        if (!selectableElements.containsKey(positionElement))
            throw new LogikException("Position enthält nicht " + positionElement.getName());

        selectableElements.get(positionElement).remove(positionedElement);
    }

    public void updateSelection(LogikElement element, List<LogikElement> selectedElements) {
        selectableElements.put(element, selectedElements);
    }
}
