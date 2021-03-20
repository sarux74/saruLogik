package de.sarux.logik.helper.application.positioner;

import de.sarux.logik.helper.application.LogikElement;
import lombok.Value;

@Value
public class ElementPair {
    LogikElement positionElement;
    LogikElement positionedElement;
}
