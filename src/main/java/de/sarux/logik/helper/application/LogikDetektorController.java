package de.sarux.logik.helper.application;

import de.sarux.logik.helper.group.LogikGroupsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class LogikDetektorController {

    private final LogikGroupsBean logikGroupsBean;
    //private final ProblemBean problemBean;

    // standard constructors
    @Autowired
    public LogikDetektorController(LogikGroupsBean logikGroupsBean) {
        //this.problemBean = problemBean;
        this.logikGroupsBean = logikGroupsBean;
    }

    @GetMapping("/detektor")
    public int dummy() {
        return 0;
    }

}
