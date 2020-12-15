package de.sarux.logik.helper.application;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NewRelationInput {
    private int blockId;
    private int groupFrom;
    private int groupTo;
    private String relationType;
    private String relationHint;
    private Boolean subLine;
    private Integer leftLineId;
    private Integer rightLineId;

}
