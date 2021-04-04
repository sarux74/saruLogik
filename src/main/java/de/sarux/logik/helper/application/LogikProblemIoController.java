package de.sarux.logik.helper.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.sarux.logik.helper.LogikProblem;
import static de.sarux.logik.helper.application.SolverController.SOLVE_VIEW_NAME;
import de.sarux.logik.helper.application.detektor.LogikBlockPair;
import de.sarux.logik.helper.application.detektor.LogikDetektorProblem;
import de.sarux.logik.helper.application.group.LogikGroupsBean;
import de.sarux.logik.helper.problem.GeneralLogikBlock;
import de.sarux.logik.helper.problem.GeneralLogikProblem;
import de.sarux.logik.helper.problem.LogikBlock;
import de.sarux.logik.helper.problem.LogikOptionBlockGroup;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@RestController
@CrossOrigin(origins = "*")
public class LogikProblemIoController {

    private final LogikGroupsBean logikGroupsBean;
    private final GeneralLogikProblemBean problemBean;

    // standard constructors
    @Autowired
    public LogikProblemIoController(LogikGroupsBean logikGroupsBean, GeneralLogikProblemBean problemBean) {
        this.logikGroupsBean = logikGroupsBean;
        this.problemBean = problemBean;
    }

    @PutMapping("problem/new")
    boolean newProblem() {
        logikGroupsBean.clear();
        problemBean.init();
        return true;
    }

    @GetMapping(path = "problem/save", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getFile() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String key = SOLVE_VIEW_NAME + "0";
        String exportedContent = objectMapper.writeValueAsString(problemBean.getProblem(key));

        String filename = "problem.json"; // Ignored?
        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(Collections.singletonList("Content-Disposition"));
        headers.set("Content-Disposition", "attachment; filename=" + filename);
        return new ResponseEntity<>(exportedContent, headers, HttpStatus.OK);
    }

    @PostMapping("problem/load")
    public int handleFileUpload(@RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) throws LogikException {
        int problemType;
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMapper mapper = new ObjectMapper();

            final JsonNode jsonNode = mapper.readTree(inputStream);

// Get the @type
            final JsonNode typeNode = jsonNode.get("@type");
            final JsonNode versionNode = jsonNode.get("version");
            // Create a Class-object
            Class<?> cls;
            if (versionNode != null && versionNode.asText() != null) {
                // Already new problem class
                // Later here distinct between different versions
                cls = GeneralLogikProblem.class;
            } else if (typeNode == null || typeNode.asText() == null) {
                // Old logik problem
                cls = LogikProblem.class;
                ((ObjectNode) jsonNode).put("@type", LogikProblem.class.getName());
            } else if (typeNode.asText().endsWith("DetektorProblem")) {
                // Detektor or something else
                cls = LogikDetektorProblem.class;
                ((ObjectNode) jsonNode).put("@type", LogikDetektorProblem.class.getName());
            } else {
                throw new LogikException("Rätsel konnte nicht gelesen werden!");
            }

            GeneralLogikProblem logikProblem = null;
            if (cls == LogikProblem.class) {
// And convert it
                final LogikProblem problem = mapper.convertValue(jsonNode, LogikProblem.class);
                logikProblem = convert(problem);
                problemType = 1;

            } else if (cls == LogikDetektorProblem.class) {
                final LogikDetektorProblem problem = mapper.convertValue(jsonNode, LogikDetektorProblem.class);
                logikProblem = convert(problem);
                problemType = 2;

            } else {
                logikProblem = mapper.convertValue(jsonNode, GeneralLogikProblem.class);
                if (logikProblem.hasOptions()) {
                    problemType = 2;
                } else {
                    problemType = 1;
                }
                
                // TODO: Remove later
                logikProblem.updateCombinations();
            }
            logikProblem.updateIndices();
            logikGroupsBean.reset(logikProblem.getGroups());
            problemBean.reset(logikProblem);

            /*BugFinder bugFinder = new BugFinder(jsonMap.getGroups());
            bugFinder.checkYetCorrect(problemBean.getView());*/
        } catch (IOException e) {
            throw new LogikException("Ladefehler: " + e.getMessage(), e);
        }
        return problemType;
    }

    private GeneralLogikProblem convert(LogikProblem problem) {
        final GeneralLogikProblem convertedProblem = new GeneralLogikProblem(problem.getGroups());
        for (LogikBlock block : problem.getBlocks()) {
            final LogikOptionBlockGroup blockGroup = convertedProblem.newBlockGroup(block.getName());
            convertedProblem.addBlock(blockGroup, convertBlock(block, null));
        }
        return convertedProblem;
    }

    private GeneralLogikProblem convert(LogikDetektorProblem problem) {
        final GeneralLogikProblem convertedProblem = new GeneralLogikProblem(problem.getGroups());
        for (LogikBlock block : problem.getTrueBlocks()) {
            final LogikOptionBlockGroup blockGroup = convertedProblem.newBlockGroup(block.getName());
            convertedProblem.addBlock(blockGroup, convertBlock(block, null));
        }
        for (LogikBlockPair pair : problem.getBlockPairs()) {
            String commonName = pair.getTrueBlock().getName();
            commonName = commonName.substring(0, commonName.length() - 5);
            final LogikOptionBlockGroup blockGroup = convertedProblem.newBlockGroup(commonName);
            convertedProblem.addBlock(blockGroup, convertBlock(pair.getTrueBlock(), "wahr"));
            convertedProblem.addBlock(blockGroup, convertBlock(pair.getFalseBlock(), "falsch"));
        }
        return convertedProblem;
    }

    private GeneralLogikBlock convertBlock(LogikBlock block, String subName) {
        final GeneralLogikBlock convertedBlock = new GeneralLogikBlock(block.getBlockId(), subName);
        convertedBlock.getMainLines().addAll(block.getMainLines());
        convertedBlock.getSubLines().addAll(block.getSubLines());
        convertedBlock.getRelations().addAll(block.getRelations());
        return convertedBlock;
    }

}
