package de.sarux.logik.helper.application;

import lombok.Data;

import java.util.List;

@Data
public class BlockCompareView {
    private List<LogicBlockViewLine> viewLines;
    private List<Integer> block1LineIds;
    private List<Integer> block2LineIds;
    private boolean[][] proposed;
}
