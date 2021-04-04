package de.sarux.logik.helper.problem.view;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class GeneralLogikBlockView {

    @Getter
    @Setter
    private int numGroups;
    @Getter
    private final List<GeneralLogikBlockViewLine> lines = new ArrayList<>();

    public GeneralLogikBlockView(int numGroups) {
        this.numGroups = numGroups;
    }

    public void addLines(List<GeneralLogikBlockViewLine> blockLines) {
        lines.addAll(blockLines);
    }
    
    public void addLine(GeneralLogikBlockViewLine blockLine) {
        lines.add(blockLine);
    }

}
