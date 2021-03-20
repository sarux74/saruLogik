package de.sarux.logik.helper.application;

import de.sarux.logik.helper.application.group.LogikGroup;

import java.util.ArrayList;
import java.util.List;

public class BugFinder {

    private final List<List<LogikElement>> shouldbe;

    public BugFinder(List<LogikGroup> logikGroups) {
        int[][] solutionIndices = {
                {0, 4, 3, 0, 3, 2},
                {1, 0, 1, 1, 0, 0},
                {2, 5, 2, 5, 5, 5},
                {3, 2, 4, 3, 2, 3},
                {4, 3, 0, 2, 1, 1},
                {5, 1, 5, 4, 4, 4}
        };

        List<List<LogikElement>> elementSolution = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            final List<LogikElement> elementList = new ArrayList<>();
            for (int j = 0; j < 6; j++) {
                final LogikElement element = logikGroups.get(j).getElements().get(solutionIndices[i][j]);
                elementList.add(element);
            }
            elementSolution.add(elementList);
        }

        shouldbe = new ArrayList<>();
        // Block 1
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(null);

        // Block 2
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);
        shouldbe.add(null);

        // Block3
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(null);

        // Block 4
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(elementSolution.get(3)); // 1 3 5
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(null);

        // Block 5
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);

        // Block 6
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(elementSolution.get(4));
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(5)); // N1
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(1));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0)); // N2
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(1)); // N3
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(5));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3)); // N4
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(4)); // N5
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(3));
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(2)); // N6
        shouldbe.add(null);
        shouldbe.add(null);
        shouldbe.add(elementSolution.get(0));
        shouldbe.add(null);
        shouldbe.add(null);
    }

    public void checkYetCorrect(LogicBlockView view) throws LogikException {
        for (int i = 0; i < view.getLines().size(); i++) {
            LogicBlockViewLine line = view.getLines().get(i);
            List<LogikElement> shouldBeLine = shouldbe.get(i);

            if (line.getType() == ViewLineType.LINE && shouldBeLine == null)
                throw new LogikException("Zeile " + i + " sollte Daten enthalten");
            else if (line.getType() != ViewLineType.LINE && shouldBeLine != null)
                throw new LogikException("Zeile " + i + " sollte keine Daten enthalten");
            else if (line.getType() == ViewLineType.LINE && shouldBeLine != null) {
                for (int j = 0; j < shouldBeLine.size(); j++) {
                    List<Integer> selectableValues = line.getView().get(j).getSelectableValues();
                    LogikElement element = shouldBeLine.get(j);
                    if (!selectableValues.contains(element.getIndex())) {
                        throw new LogikException("In Zeile " + i + ", Anzeige: " + line.getLineId() + ", Gruppe " + element.getGroup().getName() + " fehlt " + element.getName());
                    }
                }
            }
        }
    }
}
