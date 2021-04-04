package de.sarux.logik.helper.problem.util;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UpdateSelectionInput {
    int lineId;
    int groupId;
    List<Integer> selection;
}
