package de.sarux.logik.helper.application.positioner;

import de.sarux.logik.helper.application.LogikElement;
import de.sarux.logik.helper.application.LogikException;
import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Positioner {
    @Getter
    private final LogikGroup positionGroup;
    @Getter
    private final LogikGroup positionedGroup;
    private final PositionLogikLine templateLine;
    @Getter
    private final List<PositionLogikLine> positionLines = new ArrayList<>();
    private int counter = 0;

    public Positioner(LogikGroup positionGroup, LogikGroup positionedGroup, PositionLogikLine positionLogikLine) {
        this.positionGroup = positionGroup;
        this.positionedGroup = positionedGroup;
        this.templateLine = positionLogikLine;
        addTemplateLine();
    }

    public PositionLogikLine addTemplateLine() {
        PositionLogikLine newLine = new PositionLogikLine(counter, templateLine);
        counter++;
        positionLines.add(newLine);
        return newLine;
    }

    public void copyLine(int lineId) {
        Optional<PositionLogikLine> copyLine = positionLines.stream().filter(o -> o.getLineId() == lineId).findFirst();
        if (copyLine.isPresent()) {
            PositionLogikLine newLine = new PositionLogikLine(counter, copyLine.get());
            counter++;
            int index = positionLines.indexOf(copyLine.get());
            positionLines.add(index +1, newLine);
        }
    }

    public void copyLineShiftLeft(int lineId) throws LogikException {
        Optional<PositionLogikLine> copyLine = positionLines.stream().filter(o -> o.getLineId() == lineId).findFirst();
        if (copyLine.isPresent()) {
            PositionLogikLine newLine = new PositionLogikLine(counter, templateLine);

            for (int i = 0; i < positionedGroup.getElements().size() - 1; i++) {
                if (!tryToSet(copyLine.get(), newLine, i, i + 1))
                    return;
            }

            if (!tryToSet(copyLine.get(), newLine, positionedGroup.getElements().size() - 1, 0))
                return;

            counter++;
            positionLines.add(newLine);
        }
    }

    public void copyLineShiftRight(int lineId) throws LogikException {
        Optional<PositionLogikLine> copyLine = positionLines.stream().filter(o -> o.getLineId() == lineId).findFirst();
        if (copyLine.isPresent()) {
            PositionLogikLine newLine = new PositionLogikLine(counter, templateLine);

            if (!tryToSet(copyLine.get(), newLine, 0, positionedGroup.getElements().size() - 1))
                return;

            for (int i = 0; i < positionedGroup.getElements().size() - 1; i++) {
                if (!tryToSet(copyLine.get(), newLine, i + 1, i))
                    return;
            }

            counter++;
            positionLines.add(newLine);
        }
    }

    private boolean tryToSet(PositionLogikLine copyLine, PositionLogikLine newLine, int setIndex, int fetchIndex) throws LogikException {
        List<LogikElement> templateSelectables = templateLine.getSelectables(positionGroup.getElements().get(setIndex));
        List<LogikElement> copySelectables = copyLine.getSelectables(positionGroup.getElements().get(setIndex));
        List<LogikElement> shiftTemplateSelectables = templateLine.getSelectables(positionGroup.getElements().get(fetchIndex));
        List<LogikElement> shiftCopySelectables = copyLine.getSelectables(positionGroup.getElements().get(fetchIndex));
        if (templateSelectables.size() == 1 && copySelectables.size() == 1)
            return shiftCopySelectables.size() > 1;
        if (shiftTemplateSelectables.size() == 1 && shiftCopySelectables.size() == 1)
            return templateSelectables.size() > 1;
        if (shiftCopySelectables.size() == 1) {
            return newLine.select(positionGroup.getElements().get(setIndex), shiftCopySelectables.get(0));
        }

        return true;
    }

    public void removeLine(Integer lineId) {
        positionLines.removeIf(o -> o.getLineId() == lineId);
    }

    public void updateSelection(int lineId, LogikElement element, List<LogikElement> selectedElements) {
        Optional<PositionLogikLine> optLine = positionLines.stream().filter(o -> o.getLineId() == lineId).findFirst();
        optLine.ifPresent(positionLogikLine -> positionLogikLine.updateSelection(element, selectedElements));
    }

    public PositionLogikLine findLineById(int lineId) {
        Optional<PositionLogikLine> lineOpt = positionLines.stream().filter(o -> o.getLineId() == lineId).findFirst();
        return lineOpt.orElse(null);
    }

    public void set(int lineId, LogikElement positionElement, LogikElement positionedElement) throws LogikException {
        PositionLogikLine line = findLineById(lineId);
        if (line != null) {
            line.select(positionElement, positionedElement);
        }
    }
}
