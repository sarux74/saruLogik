package de.sarux.logik.helper.application;

import de.sarux.logik.helper.application.detektor.DetektorBean;
import de.sarux.logik.helper.application.group.LogikGroupsBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
public class BeanConfiguration {

    private LogikGroupsBean groupsBean;

    @Bean
    public LogikGroupsBean groups() {
        groupsBean = new LogikGroupsBean();
        return groupsBean;
    }

    @Bean
    @Lazy
    public ProblemBean problem() {
        return new ProblemBean();
    }

    @Bean
    @Lazy
    public DetektorBean detektor() {
        return new DetektorBean();
    }
}
