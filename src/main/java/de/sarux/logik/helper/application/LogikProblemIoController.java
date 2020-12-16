package de.sarux.logik.helper.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.sarux.logik.helper.LogikException;
import de.sarux.logik.helper.LogikProblem;
import de.sarux.logik.helper.detektor.DetektorBean;
import de.sarux.logik.helper.detektor.LogikDetektorProblem;
import de.sarux.logik.helper.group.LogikGroupsBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

@RestController
@CrossOrigin(origins = "*")
public class LogikProblemIoController {

    private final LogikGroupsBean logikGroupsBean;
    private final ProblemBean problemBean;
    private final DetektorBean detektorBean;
    private final ProblemViewBean problemViewBean;

    // standard constructors
    @Autowired
    public LogikProblemIoController(LogikGroupsBean logikGroupsBean, ProblemBean problemBean, DetektorBean detektorBean, ProblemViewBean problemViewBean) {
        this.logikGroupsBean = logikGroupsBean;
        this.problemBean = problemBean;
        this.detektorBean = detektorBean;
        this.problemViewBean = problemViewBean;
    }

    @PutMapping("problem/new")
    boolean newProblem() {
        problemBean.clear();
        detektorBean.clear();
        logikGroupsBean.clear();
        problemViewBean.clear();
        return true;
    }

    @GetMapping(path = "problem/save", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getFile() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String exportedContent;
        // First check if detektor
        if (detektorBean.getCurrentProblem() != null && !detektorBean.getCurrentProblem().isEmpty()) {
            exportedContent = objectMapper.writeValueAsString(detektorBean.getCurrentProblem());
        } else {
            // TODO
            exportedContent = objectMapper.writeValueAsString(problemBean.getProblem("solve0"));
        }

        String filename = "problem.json"; // Ignored?
        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlExposeHeaders(Collections.singletonList("Content-Disposition"));
        headers.set("Content-Disposition", "attachment; filename=" + filename);
        return new ResponseEntity<>(exportedContent, headers, HttpStatus.OK);
    }

    @PostMapping("problem/load")
    public int handleFileUpload(@RequestParam("file") MultipartFile file,
                                RedirectAttributes redirectAttributes) throws LogikException {
        int problemType = 0;
        try {
            InputStream inputStream = file.getInputStream();
            ObjectMapper mapper = new ObjectMapper();

            final JsonNode jsonNode = mapper.readTree(inputStream);

// Get the @type
            final JsonNode typeNode = jsonNode.get("@type");
            // Create a Class-object
            Class<?> cls;
            if (typeNode == null || typeNode.asText() == null) {
                cls = LogikProblem.class;
                ((ObjectNode) jsonNode).put("@type", LogikProblem.class.getName());
            } else {
                cls = Class.forName(typeNode.asText());
            }

            if (cls == LogikProblem.class) {
// And convert it
                final LogikProblem problem = mapper.convertValue(jsonNode, LogikProblem.class);

                logikGroupsBean.reset(problem.getGroups());
                problemBean.reset(problem);
                detektorBean.clear();
                problemViewBean.buildProblemView(problem);
                problemType = 1;

            } else if (cls == LogikDetektorProblem.class) {
                final LogikDetektorProblem problem = mapper.convertValue(jsonNode, LogikDetektorProblem.class);

                logikGroupsBean.reset(problem.getGroups());
                problemBean.clear();
                detektorBean.reset(problem);
                problemViewBean.buildDetektorView(problem);
                problemType = 2;

            } else {
                throw new LogikException("Unbekannter Problemtyp: " + cls.getName());
            }
            /*BugFinder bugFinder = new BugFinder(jsonMap.getGroups());
            bugFinder.checkYetCorrect(problemBean.getView());*/
        } catch (IOException | ClassNotFoundException e) {
            throw new LogikException("Ladefehler: " + e.getMessage(), e);
        }
        return problemType;
    }

}
