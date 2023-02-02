package de.sarux.logik.helper.problem;

import com.fasterxml.jackson.annotation.*;
import de.sarux.logik.helper.application.group.LogikGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property="@id")
public class LogikElement {

    private int index;
    private String name;
    private String shortName;
    private LogikGroup group;

    public LogikElement(int index, String name, String shortName, LogikGroup group) {
        this.index = index;
        this.name = name;
        this.shortName = shortName;
        this.group = group;
    }

    @Override
    public String toString() {
        return name + " (" + shortName + ")";
    }
}
