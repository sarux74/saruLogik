package de.sarux.logik.helper.application.group;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import de.sarux.logik.helper.problem.LogikElement;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property = "@id")
public class LogikGroup {

    private int index;
    @Setter
    private String name;
    private List<LogikElement> elements;

    LogikGroup(int index, String name) {
        this.index = index;
        this.name = name;
        elements = new ArrayList<>();
    }

    public void addElement(String name, String shortName) {
        int index = elements.size();
        elements.add(new LogikElement(index, name, shortName, this));
    }

    @Override
    public String toString() {
        return name;
    }
}
