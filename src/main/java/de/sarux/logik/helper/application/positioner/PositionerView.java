package de.sarux.logik.helper.application.positioner;

import de.sarux.logik.helper.application.LogikElement;
import de.sarux.logik.helper.application.ValueView;
import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class PositionerView {

    public List<String> headers = new ArrayList<>();
    public List<PositionViewLine> lines = new ArrayList<>();
    public PositionerView(LogikGroup group) {
        for(LogikElement element : group.getElements()) {
            headers.add(element.getName());
        }
    }

    public void addLine(int lineId, List<ValueView> valueViews) {
        lines.add(new PositionViewLine(lineId, valueViews));
    }
}
