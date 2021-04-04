package de.sarux.logik.helper.problem.view.positioner;

import de.sarux.logik.helper.problem.view.ValueView;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
public class PositionViewLine {
    Integer lineId;
    List<ValueView> view;
}
