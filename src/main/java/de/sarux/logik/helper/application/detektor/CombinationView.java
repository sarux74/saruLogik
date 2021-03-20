package de.sarux.logik.helper.application.detektor;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CombinationView {

    private List<String> blockIds;
    private List<List<Boolean>> truths;
}
