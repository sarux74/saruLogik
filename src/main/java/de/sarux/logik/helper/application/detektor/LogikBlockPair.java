package de.sarux.logik.helper.application.detektor;

import de.sarux.logik.helper.problem.LogikBlock;
import lombok.Getter;

@Getter
@Deprecated
public class LogikBlockPair {
    private LogikBlock trueBlock;
    private LogikBlock falseBlock;

    public LogikBlockPair() {
    }

    public LogikBlockPair(LogikBlock trueBlock, LogikBlock falseBlock) {
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }


}
