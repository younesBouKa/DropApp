package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import server.data.Node;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.SpaceIncomingDto;
import server.services.FsFilesService;
import server.services.NodeService;
import server.services.SpaceService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static server.exceptions.Message.*;

@RestController
@RequestMapping("/api/v0/spaces")
public class SpaceController {

    private static final Logger logger = Logger.getLogger(SpaceController.class.getName());
    private static final String NEW_ORDER_LOG = "New order was created id:{}";
    private static final String ORDER_UPDATED_LOG = "Order:{} was updated";

    private final SpaceService spaceService;

    @Autowired
    public SpaceController(SpaceService spaceService) {
        this.spaceService = spaceService;
    }

    @GetMapping
    public List<Space> getSpaces(@RequestParam(required = false, name = "page", defaultValue = "0") int page,
                             @RequestParam(required = false, name = "size", defaultValue = "20") int size,
                             @RequestParam(required = false, name = "sortField", defaultValue = "creationDate") String sortField,
                             @RequestParam(required = false, name = "direction", defaultValue = "DESC") String direction,
                             @RequestParam(required = false, name = "status") List<String> status,
                             @RequestParam(required = false, name = "search", defaultValue = "") String search) throws CustomException {

        return spaceService.getSpaces(page,size, sortField, direction, null,search);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public Space saveSpace(@RequestBody SpaceIncomingDto spaceIncomingDto) throws CustomException {
        Space space =  spaceService.insertSpace(spaceIncomingDto);
        return spaceService.addRoots(space);
    }

    @GetMapping(value = "/{spaceId}")
    public Space getSpaceById(@PathVariable String spaceId) throws CustomException {
        Space space = spaceService.getSpaceById(spaceId);
        return spaceService.addRoots(space);
    }

    @PutMapping(path = "/{spaceId}", consumes = APPLICATION_JSON_VALUE)
    public Space updateSpace(@PathVariable String spaceId,
                             @RequestBody SpaceIncomingDto spaceIncomingDto,
                             HttpServletRequest request) throws CustomException {

        return spaceService.updateSpace(spaceId, spaceIncomingDto);
    }

    @DeleteMapping(path = "/{spaceId}", consumes = APPLICATION_JSON_VALUE)
    public int deleteSpace(@PathVariable String spaceId,
                             HttpServletRequest request) throws CustomException {

        return spaceService.deleteSpace(spaceId);
    }
}
