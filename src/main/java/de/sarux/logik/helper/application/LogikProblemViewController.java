package de.sarux.logik.helper.application;

import static de.sarux.logik.helper.application.SolverController.SOLVE_VIEW_NAME;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.view.GeneralLogikBlockViewLine;
import de.sarux.logik.helper.problem.view.blockcompare.BlockCompareView;
import de.sarux.logik.helper.problem.view.blockcompare.BlockCompareViewHandler;
import de.sarux.logik.helper.problem.view.blockcompare.IdNamePair;
import de.sarux.logik.helper.problem.view.grid.LogikProblemGrid;
import de.sarux.logik.helper.problem.view.multiplerelation.MultipleRelationBuilder;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("")
public class LogikProblemViewController {

    private final GeneralLogikProblemBean problemBean;

    // standard constructors
    @Autowired
    public LogikProblemViewController(GeneralLogikProblemBean problemBean) {
        this.problemBean = problemBean;
    }


    @GetMapping(path = "/problems/{problemKey}/view/group")
    public List<GeneralLogikBlockViewLine> getBuildViewGroup(@PathVariable String problemKey, @RequestParam(name = "groupId") Integer groupId) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        final GeneralLogikProblem problem = problemBean.getProblem(problemName);
        final LogikProblemGrid grid = problem.ensureGrid();
        return grid.toGroupView(groupId);
    }

    @GetMapping(path = "/problems/{problemKey}/view/block/comparable")
    public List<IdNamePair> getComparableBlocks(@PathVariable String problemKey) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
   
        final BlockCompareViewHandler blockCompareViewHandler = new BlockCompareViewHandler(problem);
        return blockCompareViewHandler.getComparableBlocks();
    }

    @GetMapping(path = "/problems/{problemKey}/view/blockcompare")
    public BlockCompareView getBlockCompareView(@PathVariable String problemKey, @RequestParam(name = "blockId1") Integer blockId1, @RequestParam(name = "blockId2") Integer blockId2) {

        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
   
         final BlockCompareViewHandler blockCompareViewHandler = new BlockCompareViewHandler(problem);
        return blockCompareViewHandler.buildBlockCompareView(blockId1, blockId2);
    }

    @GetMapping(path = "/problems/{problemKey}/view/multiple")
    public List<GeneralLogikBlockViewLine> getMultipleRelations(@PathVariable String problemKey) {
        String problemName = SOLVE_VIEW_NAME + problemKey;
        GeneralLogikProblem problem = problemBean.getProblem(problemName);
   
        MultipleRelationBuilder multipleRelationBuilder = new MultipleRelationBuilder(problem);
        return multipleRelationBuilder.build();
    }
}
