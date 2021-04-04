package de.sarux.logik.helper.problem.view;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralLogikBlockViewLine {

    private final ViewLineType type;
    private boolean hideSub;
    private List<ValueView> view = new ArrayList<>();
    private final Integer blockGroupId;
    private final Integer blockId;
    private final Integer lineId;
    private final Integer rightLineId;

    public GeneralLogikBlockViewLine(ViewLineType type) {
       this(type, null, null, null, null);
    }
    
    public GeneralLogikBlockViewLine(ViewLineType type, Integer blockGroupId, Integer blockId, Integer lineId, Integer rightLineId) {
        this.type = type;
        this.blockGroupId = blockGroupId;
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
