package de.sarux.logik.helper.application.positioner;

import de.sarux.logik.helper.application.ValueView;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;

@Value
@AllArgsConstructor
public class PositionViewLine {
    Integer lineId;
    List<ValueView> view;
}
