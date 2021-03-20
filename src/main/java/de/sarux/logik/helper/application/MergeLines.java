package de.sarux.logik.helper.application;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MergeLines {
    private int line1Id;
    private int line2Id;
}
