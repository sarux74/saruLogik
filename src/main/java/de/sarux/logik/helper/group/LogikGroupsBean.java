package de.sarux.logik.helper.group;

import de.sarux.logik.helper.LogikElement;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Getter
public class LogikGroupsBean {
    private List<LogikGroup> groups = new ArrayList<>();

    public LogikGroup newGroup(String name) {
        LogikGroup group = new LogikGroup(groups.size(), name);
        groups.add(group);
        return group;
    }

    public void updateGroup(LogikGroup group) {
        int index = group.getIndex();
        if (index == groups.size())
            groups.add(group);
        else {
            LogikGroup orig = groups.get(group.getIndex());
            orig.setName(group.getName());
            List<LogikElement> elements = group.getElements();
            List<LogikElement> origElements = orig.getElements();
            for (int i = 0; i < elements.size(); i++) {
                if (orig.getElements().size() > i) {
                    origElements.get(i).setName(elements.get(i).getName());
                    origElements.get(i).setShortName(elements.get(i).getShortName());
                } else {
                    origElements.add(elements.get(i));
                }
            }
            while (origElements.size() > elements.size()) {
                origElements.remove(origElements.size() - 1);
            }
        }
    }

    public void clear() {
        groups.clear();
    }

    public void reset(List<LogikGroup> groups) {
        this.groups = groups;
    }
}
