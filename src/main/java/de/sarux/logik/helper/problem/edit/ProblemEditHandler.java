/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.sarux.logik.helper.problem.edit;

import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author sarux
 */
public class ProblemEditHandler {

    private final GeneralLogikProblem problem;

    public ProblemEditHandler(GeneralLogikProblem problem) {
        this.problem = problem;
    }

    public void newBlock(NewBlockInput newBlockInput) {
        final List<BlockOption> options = newBlockInput.getOptions();
        if (options == null || options.size() <= 1) {
            GeneralLogikBlock block = problem.newBlockGroupWithBlock(newBlockInput.getBlockName());
            if (newBlockInput.isNoDuplicates() || newBlockInput.getGroupId() != null) {
                block.setNoDuplicates(true);
            }

            if (newBlockInput.getGroupId() == null) {
                problem.newMainLine(block);
            } else {
                LogikGroup blockGroup = problem.getGroup(newBlockInput.getGroupId());
                for (LogikElement element : blockGroup.getElements()) {
                    LogikLine blockLine = problem.newMainLine(block);
                    List<LogikElement> elements = blockLine.getSelectableElements().get(newBlockInput.getGroupId());
                    elements.removeIf(o -> o != element);
                    if (newBlockInput.isExcludeSameShortNames()) {
                        int groupId = 0;
                        for (List<LogikElement> otherElements : blockLine.getSelectableElements()) {
                            if (groupId != newBlockInput.getGroupId()) {
                                otherElements.removeIf(o -> o.getShortName().equals(element.getShortName()));
                            }
                            groupId++;
                        }
                    }
                }
            }
        } else {
            problem.newBlockGroup(newBlockInput.getBlockName(), newBlockInput.getOptions().stream().map(BlockOption::getName).collect(Collectors.toList()));
        }
    }
}
