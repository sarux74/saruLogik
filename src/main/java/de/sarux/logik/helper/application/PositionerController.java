package de.sarux.logik.helper.application;

import static de.sarux.logik.helper.application.SolverController.SOLVE_VIEW_NAME;
import de.sarux.logik.helper.application.group.LogikGroup;
import de.sarux.logik.helper.application.group.LogikGroupsBean;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikElement;
import de.sarux.logik.helper.problem.LogikLine;
import de.sarux.logik.helper.problem.util.UpdateSelectionInput;
import de.sarux.logik.helper.problem.view.ValueView;
import de.sarux.logik.helper.problem.view.positioner.AddLineInput;
import de.sarux.logik.helper.problem.view.positioner.PositionLogikLine;
import de.sarux.logik.helper.problem.view.positioner.Positioner;
import de.sarux.logik.helper.problem.view.positioner.PositionerInit;
import de.sarux.logik.helper.problem.view.positioner.PositionerView;
import de.sarux.logik.helper.problem.view.positioner.RemoveLineInput;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("positioner")
@CrossOrigin("*")
public class PositionerController {
    private final LogikGroupsBean logikGroupsBean;
    private final GeneralLogikProblemBean problemBean;
    private final PositionerBean positionerBean;

    // standard constructors
    @Autowired
    public PositionerController(GeneralLogikProblemBean problemBean, LogikGroupsBean logikGroupsBean, PositionerBean positionerBean) {
        this.logikGroupsBean = logikGroupsBean;
        this.problemBean = problemBean;
        this.positionerBean = positionerBean;
    }

    @PutMapping("/problems/{problemKey}/init")
    public boolean initPositioner(@PathVariable String problemKey, @RequestBody PositionerInit positionerInit) throws LogikException {
        LogikGroup positionGroup = logikGroupsBean.getGroups().get(positionerInit.getPositionGroupId());
        LogikGroup positionedGroup = logikGroupsBean.getGroups().get(positionerInit.getPositionedGroupId());

        final GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);
        PositionLogikLine positionLogikLine = new PositionLogikLine(-1, positionGroup, positionedGroup);

        // Look for lines with singleton positionElement
        for (LogikElement positionElement : positionGroup.getElements()) {
            final List<LogikLine> singletonLines = problem.getLines().stream().filter(o -> o.getSelectables(positionGroup).size() == 1 && o.getSelectables(positionGroup).get(0) == positionElement).collect(Collectors.toList());
            if (!singletonLines.isEmpty()) {
                Set<LogikElement> positionedElements = getCommons(singletonLines, positionedGroup);
                List<LogikElement> currentElements = positionLogikLine.getSelectables(positionElement);
                currentElements.removeIf(o -> !positionedElements.contains(o));
            } else {
                final List<LogikLine> containingLines = problem.getLines().stream().filter(o -> o.getSelectables(positionGroup).contains(positionElement)).collect(Collectors.toList());
                Set<LogikElement> missingElements = getMissing(containingLines, positionedGroup);
                List<LogikElement> currentElements = positionLogikLine.getSelectables(positionElement);
                currentElements.removeAll(missingElements);
            }
        }

        // Unset singleton of positioned group elements
        List<LogikElement> singletons = positionLogikLine.getSelectableElements().values().stream()
                .filter(logikElements -> logikElements.size() == 1)
                .map(logikElements -> logikElements.get(0))
                .collect(Collectors.toList());
        if (!singletons.isEmpty()) {
            for (Map.Entry<LogikElement, List<LogikElement>> selectable : positionLogikLine.getSelectableElements().entrySet()) {
                if (selectable.getValue().size() > 1)
                    selectable.getValue().removeAll(singletons);
            }
        }

        // Unset for positioned singletons
        for (LogikElement positionedElement : positionedGroup.getElements()) {
            final List<LogikLine> singletonLines = problem.getLines().stream().filter(o -> o.getSelectables(positionedGroup).size() == 1 && o.getSelectables(positionedGroup).get(0) == positionedElement).collect(Collectors.toList());
            if (!singletonLines.isEmpty()) {
                Set<LogikElement> positionElements = getCommons(singletonLines, positionGroup);
                for(LogikElement positionElement: positionGroup.getElements()) {
                    if(!positionElements.contains(positionElement)) {
                        positionLogikLine.unselect(positionElement, positionedElement);
                    }
                }
            }
        }


        // Template prepared -> create positioner
        Positioner positioner = new Positioner(positionGroup, positionedGroup, positionLogikLine);
        positionerBean.addPositioner(problemKey, positioner);
        return true;
    }

    @GetMapping("/problems/{problemKey}/view")
    public PositionerView getView(@PathVariable String problemKey) {
        Positioner positioner = positionerBean.getPositioner(problemKey);
        final PositionerView positionerView = new PositionerView(positioner.getPositionGroup());
        for (PositionLogikLine line : positioner.getPositionLines()) {
            final List<ValueView> valueViews = new ArrayList<>();
            for (LogikElement element : positioner.getPositionGroup().getElements()) {
                List<LogikElement> selectables = line.getSelectables(element);
                final ValueView valueView = LogicBlockViewBuilder.buildValueView(positioner.getPositionedGroup(), selectables);
                valueViews.add(valueView);
            }
            positionerView.addLine(line.getLineId(), valueViews);
        }
        return positionerView;
    }

    @PutMapping("/problems/{problemKey}/add")
    boolean addLine(@PathVariable String problemKey, @RequestBody AddLineInput addLineInput) throws LogikException {
        Integer lineId = addLineInput.getLineId();;
        if(lineId == null)
            positionerBean.getPositioner(problemKey).addTemplateLine();
        else if(addLineInput.getDirection() == 0) {
            positionerBean.getPositioner(problemKey).copyLine(lineId);
        } else if(addLineInput.getDirection() == -1) {
            positionerBean.getPositioner(problemKey).copyLineShiftLeft(lineId);
        } else {
            positionerBean.getPositioner(problemKey).copyLineShiftRight(lineId);
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/remove")
    boolean removeLine(@PathVariable String problemKey, @RequestBody RemoveLineInput removeLineInput) {
        positionerBean.getPositioner(problemKey).removeLine(removeLineInput.getLineId());
        return true;
    }

    @PutMapping("/problems/{problemKey}/selection")
    boolean updateSelection(@PathVariable String problemKey, @RequestBody UpdateSelectionInput updateSelectionInput) throws LogikException {
        Positioner positioner = positionerBean.getPositioner(problemKey);
        PositionLogikLine line = positioner.findLineById(updateSelectionInput.getLineId());
        LogikElement element = positioner.getPositionGroup().getElements().get(updateSelectionInput.getGroupId());
        if (updateSelectionInput.getSelection().size() == 1) {
            positioner.set(updateSelectionInput.getLineId(), element, positioner.getPositionedGroup().getElements().get(updateSelectionInput.getSelection().get(0)));
        } else {
            final List<LogikElement> selectedElements = new ArrayList<>();
            for (Integer index : updateSelectionInput.getSelection()) {
                selectedElements.add(positioner.getPositionedGroup().getElements().get(index));
            }
            positioner.updateSelection(line.getLineId(), element, selectedElements);
        }
        return true;
    }

    @PutMapping("/problems/{problemKey}/overtake")
    boolean overtake(@PathVariable String problemKey) throws LogikException {
        Positioner positioner = positionerBean.getPositioner(problemKey);
        final GeneralLogikProblem problem = problemBean.getProblem(SOLVE_VIEW_NAME + problemKey);

        // Position -> Positioned
        Map<LogikElement, Set<LogikElement>> allowedElements = new HashMap<>();
        for (LogikElement positionElement : positioner.getPositionGroup().getElements()) {
            Set<LogikElement> positionedElements = new HashSet<>();
            for (PositionLogikLine line : positioner.getPositionLines()) {
                positionedElements.addAll(line.getSelectables(positionElement));
            }
            allowedElements.put(positionElement, positionedElements);
        }

        for (LogikLine line : problem.getLines()) {

            Set<LogikElement> activeElements = new HashSet<>();
            List<LogikElement> possibleElements = line.getSelectables(positioner.getPositionGroup());
            for (LogikElement possibleElement : possibleElements) {
                activeElements.addAll(allowedElements.get(possibleElement));
            }
            // Invert
            List<LogikElement> inactiveElements = new ArrayList<>(positioner.getPositionedGroup().getElements());
            inactiveElements.removeAll(activeElements);

            for (LogikElement inactiveElement : inactiveElements) {
                line.unselect(inactiveElement);
            }
        }

        // Positioned -> Positione
        allowedElements.clear();
        for(LogikElement element: positioner.getPositionedGroup().getElements()) {
            allowedElements.put(element, new HashSet<>());
        }

        for (LogikElement positionElement : positioner.getPositionGroup().getElements()) {

            for (PositionLogikLine line : positioner.getPositionLines()) {
                for(LogikElement positionedElement: line.getSelectables(positionElement)) {
                    allowedElements.get(positionedElement).add(positionElement);
                }
            }
        }

        for (LogikLine line : problem.getLines()) {
            Set<LogikElement> activeElements = new HashSet<>();
            List<LogikElement> possibleElements = line.getSelectables(positioner.getPositionedGroup());
            for (LogikElement possibleElement : possibleElements) {
                activeElements.addAll(allowedElements.get(possibleElement));
            }
            // Invert
            List<LogikElement> inactiveElements = new ArrayList<>(positioner.getPositionGroup().getElements());
            inactiveElements.removeAll(activeElements);

            for (LogikElement inactiveElement : inactiveElements) {
                line.unselect(inactiveElement);
            }
        }
        return true;
    }

    private Set<LogikElement> getCommons(List<LogikLine> lines, LogikGroup positionedGroup) {
        Set<LogikElement> commons = new HashSet<>(positionedGroup.getElements());
        lines.forEach(o -> commons.removeIf(p -> !o.getSelectables(positionedGroup).contains(p)));
        return commons;
    }

    private Set<LogikElement> getMissing(List<LogikLine> lines, LogikGroup positionedGroup) {
        Set<LogikElement> missing = new HashSet<>(positionedGroup.getElements());
        lines.forEach(o -> missing.removeIf(p -> o.getSelectables(positionedGroup).contains(p)));
        return missing;
    }
}
