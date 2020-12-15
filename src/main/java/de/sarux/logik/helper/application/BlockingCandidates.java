package de.sarux.logik.helper.application;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BlockingCandidates {
    private int groupId;
    private int[] selectedLines;
}
