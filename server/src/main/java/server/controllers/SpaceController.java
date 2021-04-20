package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.data.Space;
import server.exceptions.CustomException;
import server.models.SpaceIncomingDto;
import server.services.ISpaceService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static server.exceptions.Message.*;

@RestController
@RequestMapping("/api/v0/spaces")
public class SpaceController {

    private static final Logger logger = Logger.getLogger(SpaceController.class.getName());
    private static final String NEW_ORDER_LOG = "New order was created id:{}";
    private static final String ORDER_UPDATED_LOG = "Order:{} was updated";

    private final ISpaceService spaceService;

    @Autowired
    public SpaceController(ISpaceService spaceService) {
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
        return space;
    }

    @GetMapping(value = "/{spaceId}")
    public Space getSpaceById(@PathVariable String spaceId) throws CustomException {
        Space space = spaceService.getSpaceById(spaceId);
        return space;
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
