package de.sarux.logik.helper.application;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LogicBlockViewLine {

    private final ViewLineType type;
    private boolean hideSub;
    private List<ValueView> view = new ArrayList<>();
    private final Integer blockId;
    private final Integer lineId;
    private final Integer rightLineId;

    public LogicBlockViewLine(ViewLineType type, Integer blockId, Integer lineId, Integer rightLineId) {
        this.type = type;
        this.blockId = blockId;
        this.lineId = lineId;
        this.rightLineId = rightLineId;
    }

    public void setValueView(int index, ValueView valueView) {
        while (view.size() < index) {
            view.add(null);
        }
        if (view.size() > index)
            view.remove(index);
        view.add(index, valueView);
    }
}
