package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import server.data.Node;
import server.data.Permission;
import server.services.FsFilesService;
import server.services.NodeService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/node")
public class NodeController {

    private final Logger logger = Logger.getLogger(NodeController.class.getName());
    @Autowired
    FsFilesService fileService;
    @Autowired
    NodeService nodeService;

    @PostMapping(value = "/getNodeById")
    public Node getNodeById(@RequestBody  Map<String, Object> body,  HttpServletRequest request) {
        String nodeId = (String)body.getOrDefault("nodeId",null);
        return nodeService.getNodeById(nodeId);
    }

    @PostMapping("/getNodeByPath")
    public Node getNodeByPath(@RequestBody  Map<String, Object> body,  HttpServletRequest request) {
        String nodePath = (String)body.getOrDefault("nodePath",null);
        return nodeService.getNodeByPath(nodePath);
    }

    @PostMapping("/getNodesByParentId")
    public List<Node> getNodeByParentId(@RequestBody Map<String, Object> body,  HttpServletRequest request) {
        String parentId = (String)body.getOrDefault("parentId",null);
        return nodeService.getNodesByParentId(parentId);
    }
    @PostMapping("/getNodesByParentPath")
    public List<Node> getNodeByParentPath(@RequestBody Map<String, Object> body,  HttpServletRequest request) {
        String parentPath = (String)body.getOrDefault("parentPath",null);
        return nodeService.getNodesByParentPath(parentPath);
    }

    @PostMapping("/getNodesByQuery")
    public List<Node> getNodesByQuery(@RequestBody Map<String, Object> query,  HttpServletRequest request) {
        return nodeService.getNodesByQuery(query);
    }

    @PostMapping("/createFolderNodeWithMetaData")
    public Node createFolderWithMetaData(@RequestBody Map<String, Object> metaData, HttpServletRequest request) {
        return nodeService.createFolder(metaData);
    }

    @PostMapping("/createFolderNodeByPath")
    public Node createFolderWithPath(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String path = (String)body.getOrDefault("path",null);
        return nodeService.createFolderFromPath(path);
    }

    @PostMapping("/deleteNodeByPath")
    public long deleteNodeByPath(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String path = (String)body.getOrDefault("path",null);
        boolean recursive = (boolean)body.getOrDefault("recursive",false);
        return nodeService.deleteNodeByPath(path, recursive);
    }

    @PostMapping("/deleteNodeById")
    public long deleteNodeById(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String nodeId = (String)body.getOrDefault("nodeId",null);
        boolean recursive = (boolean)body.getOrDefault("recursive",false);
        return nodeService.deleteNodeById(nodeId, recursive);
    }

    @PostMapping("/deleteNodesById")
    public List<String> deleteNodesById(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        List<String> nodesId = (List<String>) body.getOrDefault("nodesId",null);
        boolean recursive = (boolean)body.getOrDefault("recursive",false);
        return nodeService.deleteNodesByIds(nodesId, recursive);
    }

    @PostMapping("/hasPermissionById")
    public boolean hasPermissionById(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String nodeId = (String)body.getOrDefault("nodeId",null);
        Object permission = body.getOrDefault("permission", Permission.READ_WRITE);
        return nodeService.hasPermissionById(nodeId,permission);
    }

    @PostMapping("/hasPermissionByPath")
    public boolean hasPermissionByPath(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String nodePath = (String)body.getOrDefault("nodePath",null);
        Object permission = body.getOrDefault("permission", Permission.READ_WRITE);
        return nodeService.hasPermissionByPath(nodePath,permission);
    }

    @PostMapping("/copyNodeByPath")
    public Node copyNodeByPath(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String srcPath = (String)body.getOrDefault("srcPath",null);
        String destPath = (String)body.getOrDefault("destPath",null);
        return nodeService.copyNodeWithPath(srcPath,destPath,false);
    }

    @PostMapping("/copyNodeById")
    public Node copyNodeById(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String srcId = (String)body.getOrDefault("srcId",null);
        String destId = (String)body.getOrDefault("destId",null);
        return nodeService.copyNodeWithId(srcId,destId,false);
    }

    @PostMapping("/moveNodeByPath")
    public Node moveNodeByPath(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String srcPath = (String)body.getOrDefault("srcPath",null);
        String destPath = (String)body.getOrDefault("destPath",null);
        return nodeService.copyNodeWithPath(srcPath,destPath,true);
    }

    @PostMapping("/moveNodeById")
    public Node moveNodeById(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        String srcId = (String)body.getOrDefault("srcId",null);
        String destId = (String)body.getOrDefault("destId",null);
        return nodeService.copyNodeWithId(srcId,destId,true);
    }
}
