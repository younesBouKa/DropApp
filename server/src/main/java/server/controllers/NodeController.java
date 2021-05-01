package server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.data.IUser;
import server.exceptions.CustomException;
import server.exceptions.Message;
import server.models.NodeWebRequest;
import server.data.Node;
import server.models.ZipRequest;
import server.services.INodeService;
import server.user.services.CustomUserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static server.exceptions.Message.ERROR_WHILE_STREAMING_FILE_CONTENT;

@RestController
@RequestMapping("/api/v0/nodes")
public class NodeController {

    private static final Logger logger = Logger.getLogger(NodeController.class.getName());
    private static final String NEW_ORDER_LOG = "New order was created id:{}";
    private static final String ORDER_UPDATED_LOG = "Order:{} was updated";

    private final INodeService nodeService;

    @Autowired
    public NodeController(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping("")
    public List<Node> getRootNodes() throws CustomException {
        return nodeService.getRootNodes(currentUser());
    }

    @PostMapping
    public Node saveRootNode(@RequestPart(value = "file", required = false) MultipartFile file,
                             @RequestParam(value = "nodeInfo") String nodeInfo) throws CustomException {
        NodeWebRequest node = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeWebRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.setFile(file);
        Node createdNode = nodeService.insertNode(currentUser(), node);
        return createdNode;
    }

    @GetMapping(value = "/{nodeId}")
    public Node getNodeById(@PathVariable String nodeId) throws CustomException {
        Node node = nodeService.getNodeById(currentUser(), nodeId);
        return node;
    }

    @GetMapping(value = "/{nodeId}/stream")
    public Node getNodeContent(@PathVariable String nodeId, HttpServletRequest request, HttpServletResponse response) throws CustomException {
        logger.log(INFO, String.format("streaming file %s %n", nodeId));
        Node node = nodeService.getNodeContent(currentUser(), nodeId);
        try(InputStream inputStream = node.getContent()){
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Error while streaming file content: %s, Error: %s %n", nodeId, e.getMessage()));
            throw new CustomException(ERROR_WHILE_STREAMING_FILE_CONTENT, nodeId);
        }
        return node;
    }

    @PostMapping(value = "/compress")
    public Node zipNodes(@RequestBody ZipRequest zipRequest,
                         HttpServletRequest request,
                         HttpServletResponse response) throws CustomException {
        logger.log(INFO, String.format("zipped nodes %s %n", zipRequest));
        return nodeService.createZipNode(currentUser(), zipRequest);
    }

    @GetMapping("/{parentId}/children")
    public List<Node> getNodes(@PathVariable String parentId,
                               @RequestParam(required = false, name = "page", defaultValue = "0") int page,
                               @RequestParam(required = false, name = "size", defaultValue = "20") int size,
                               @RequestParam(required = false, name = "sortField", defaultValue = "creationDate") String sortField,
                               @RequestParam(required = false, name = "direction", defaultValue = "DESC") String direction,
                               @RequestParam(required = false, name = "status") List<String> status,
                               @RequestParam(required = false, name = "search", defaultValue = "") String search) throws CustomException {

        return nodeService.getNodes(currentUser(), parentId, page, size, sortField, direction, status, search);
    }

    @PostMapping("/{parentId}")
    public Node saveNode(@PathVariable(required = false, name = "parentId") String parentId,
                         @RequestPart(value = "file", required = false) MultipartFile file,
                         @RequestParam(value = "nodeInfo") String nodeInfo) throws CustomException {
        NodeWebRequest node = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeWebRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.setParentId(parentId);
        node.setFile(file);
        Node createdNode = nodeService.insertNode(currentUser(), node);
        return createdNode;
    }

    @PutMapping(path = "/{nodeId}")
    public Node updateNode(@PathVariable String nodeId,
                           @RequestPart (value = "file", required = false)  MultipartFile file,
                           @RequestParam(value = "nodeInfo") String nodeInfo,
                           HttpServletRequest request) throws CustomException {
        NodeWebRequest node ;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeWebRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.setFile(file);
        Node createdNode = nodeService.updateNode(currentUser(), nodeId, node);
        return createdNode;
    }

    @DeleteMapping(path = "/{nodeId}", consumes = APPLICATION_JSON_VALUE)
    public int deleteNode(@PathVariable String nodeId,
                             HttpServletRequest request) throws CustomException {

        return nodeService.deleteNode(currentUser(), nodeId);
    }
    /********** tools **********************/
    public static IUser currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
