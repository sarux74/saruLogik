package de.sarux.logik.helper.problem.edit;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewBlockInput {
    private String blockName;
    private boolean noDuplicates;
    private Integer groupId;
    private boolean excludeSameShortNames;
    private List<BlockOption> options;
}
