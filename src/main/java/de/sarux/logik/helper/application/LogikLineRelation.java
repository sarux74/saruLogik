package de.sarux.logik.helper.application;

import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogikLineRelation {
    int relationId;
    LogikLine leftLine;
    LogikGroup leftGroup;
    LogikLine rightLine;
    LogikGroup rightGroup;
    LogikRelationType type;
    String relationHint;
    boolean subRelation;

    public void flip() {
        LogikLine flipLine = leftLine;
        leftLine = rightLine;
        rightLine = flipLine;

        LogikGroup flipGroup = leftGroup;
        leftGroup = rightGroup;
        rightGroup = flipGroup;

        switch(type) {
            case PREVIOUS:
                type= LogikRelationType.NEXT;
                break;
            case NEXT:
                type=LogikRelationType.PREVIOUS;
                break;
            case NOT_PREVIOUS:
                type= LogikRelationType.NOT_NEXT;
                break;
            case NOT_NEXT:
                type=LogikRelationType.NOT_PREVIOUS;
                break;
            default:
                break;
        }
    }
}
