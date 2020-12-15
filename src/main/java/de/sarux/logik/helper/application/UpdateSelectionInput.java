package de.sarux.logik.helper.application;

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
