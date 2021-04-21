package server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.exceptions.CustomException;
import server.exceptions.Message;
import server.models.NodeIncomingDto;
import server.data.NodeNew;
import server.services.INodeService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/v0/spaces/{spaceId}/nodes")
public class NodeNewController {

    private static final Logger logger = Logger.getLogger(NodeNewController.class.getName());
    private static final String NEW_ORDER_LOG = "New order was created id:{}";
    private static final String ORDER_UPDATED_LOG = "Order:{} was updated";

    private final INodeService nodeService;

    @Autowired
    public NodeNewController(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping("/children/{parentId}")
    public List<NodeNew> getNodes(@PathVariable String parentId,
                                  @RequestParam(required = false, name = "page", defaultValue = "0") int page,
                                 @RequestParam(required = false, name = "size", defaultValue = "20") int size,
                                 @RequestParam(required = false, name = "sortField", defaultValue = "creationDate") String sortField,
                                 @RequestParam(required = false, name = "direction", defaultValue = "DESC") String direction,
                                 @RequestParam(required = false, name = "status") List<String> status,
                                 @RequestParam(required = false, name = "search", defaultValue = "") String search) throws CustomException {

        return nodeService.getNodes(parentId, page, size, sortField, direction, status, search);
    }

    @PostMapping
    public NodeNew saveNode(@PathVariable String spaceId,
                            @RequestPart MultipartFile file,
                            @RequestParam("nodeInfo") String nodeInfo) throws CustomException {
        NodeIncomingDto node = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeIncomingDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.setFile(file);
        NodeNew createdNode = nodeService.insertNode(spaceId, node);
        return createdNode;
    }

    @GetMapping(value = "/{nodeId}")
    public NodeNew getNodeById(@PathVariable String spaceId,
                               @PathVariable String nodeId) throws CustomException {

        NodeNew node = nodeService.getNodeById(spaceId, nodeId);
        return node;
    }

    @PutMapping(path = "/{nodeId}")
    public NodeNew updateNode(@PathVariable String spaceId,
                              @PathVariable String nodeId,
                              @RequestPart MultipartFile file,
                              @RequestParam("nodeInfo") String nodeInfo,
                             HttpServletRequest request) throws CustomException {
        NodeIncomingDto node = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeIncomingDto.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.setFile(file);
        NodeNew createdNode = nodeService.updateNode(spaceId, nodeId, node);
        return createdNode;
    }

    @DeleteMapping(path = "/{nodeId}", consumes = APPLICATION_JSON_VALUE)
    public int deleteNode(@PathVariable String spaceId,
                          @PathVariable String nodeId,
                             HttpServletRequest request) throws CustomException {

        return nodeService.deleteNode(spaceId, nodeId);
    }
}
