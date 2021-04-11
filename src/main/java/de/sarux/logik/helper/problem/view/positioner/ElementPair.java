package de.sarux.logik.helper.problem.view.positioner;

import de.sarux.logik.helper.problem.LogikElement;
import lombok.Value;

@Value
public class ElementPair {
    LogikElement positionElement;
    LogikElement positionedElement;
}
