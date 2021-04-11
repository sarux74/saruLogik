package de.sarux.logik.helper.problem.view.group;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlockingCandidates {
    private int groupId;
    private int[] selectedLines;
}
