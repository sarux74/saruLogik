package de.sarux.logik.helper.problem.view.blockcompare;

import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewLine;
import java.util.List;
import lombok.Data;

@Data
public class BlockCompareView {
    private List<GeneralLogikBlockViewLine> viewLines;
    private List<Integer> block1LineIds;
    private List<Integer> block2LineIds;
    private boolean[][] proposed;
}
