package de.sarux.logik.helper.application;

import de.sarux.logik.helper.LogikElement;
import de.sarux.logik.helper.group.LogikGroup;
import de.sarux.logik.helper.group.LogikGroupsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class LogikGroupController {

    // standard constructors
    @Autowired
    public LogikGroupController(LogikGroupsBean logikGroupsBean) {
        this.logikGroupsBean = logikGroupsBean;
    }

    private final LogikGroupsBean logikGroupsBean;

    @GetMapping("/groups")
    public List<LogikGroup> getGroups() {
        return logikGroupsBean.getGroups();
    }

    @PutMapping("/groups")
    boolean updateGroup(@RequestBody LogikGroup group) {
        for (LogikElement element : group.getElements())
            element.setGroup(group);
        logikGroupsBean.updateGroup(group);
        return true;
    }

}
