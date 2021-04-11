package de.sarux.logik.helper.problem.util;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSelectionInput {
    int lineId;
    int groupId;
    List<Integer> selection;
}
