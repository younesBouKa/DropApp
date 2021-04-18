package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import server.data.Node;
import server.exceptions.CustomException;
import server.services.FsFilesService;
import server.services.NodeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static server.exceptions.Message.*;

@RestController
@RequestMapping("/api/node")
public class NodeController {

    private final Logger logger = Logger.getLogger(NodeController.class.getName());
    @Autowired
    FsFilesService fileService;
    @Autowired
    NodeService nodeService;

    @PostMapping(value = "/getNodeById")
    public Node getNodeById(@RequestBody  Map<String, Object> body,  HttpServletRequest request) throws CustomException {
        String nodeId = (String)body.getOrDefault("nodeId",null);
        Node node = nodeService.getNodeById(nodeId);
        if(node==null)
            throw new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId);
        return node;
    }

    @GetMapping("/getNodeInfoById/{fileId}")
    public Node getNodeInfoById(@PathVariable String fileId,  HttpServletRequest request) throws CustomException {
        Node node = nodeService.getNodeInfoById(fileId);
        if(node==null)
            throw new CustomException(NO_NODE_WITH_GIVEN_ID,fileId);
        return node;
    }

    @PostMapping("/getNodeByPath")
    public Node getNodeByPath(@RequestBody  Map<String, Object> body,  HttpServletRequest request) throws Exception {
        String nodePath = (String)body.getOrDefault("nodePath",null);
        Node node = nodeService.getNodeByPath(nodePath);
        if(node==null)
            throw new CustomException(NO_NODE_WITH_GIVEN_PATH, nodePath);
        return node;
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
    public Node createFolderWithMetaData(@RequestBody Map<String, Object> metaData, HttpServletRequest request) throws Exception {
        Node node = nodeService.createFolder(metaData);
        if(node==null)
            throw new CustomException(CANT_CREATE_FOLDER, metaData);
        return node;
    }

    @PostMapping("/createFolderNodeByPath")
    public Node createFolderWithPath(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        String path = (String)body.getOrDefault("path",null);
        Node node = nodeService.createFolderFromPath(path);
        if(node==null)
            throw new CustomException(CANT_CREATE_FOLDER_FROM_PATH, path);
        return node;
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

    /*@PostMapping("/hasPermissionById")
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
    }*/

    @PostMapping("/copyNodeByPath")
    public Node copyNodeByPath(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        String srcPath = (String)body.getOrDefault("srcPath",null);
        String destPath = (String)body.getOrDefault("destPath",null);
        return nodeService.copyNodeWithPath(srcPath,destPath,false);
    }

    @PostMapping("/copyNodeById")
    public Node copyNodeById(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        String srcId = (String)body.getOrDefault("srcId",null);
        String destId = (String)body.getOrDefault("destId",null);
        return nodeService.copyNodeWithId(srcId,destId,false);
    }

    @PostMapping("/moveNodeByPath")
    public Node moveNodeByPath(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        String srcPath = (String)body.getOrDefault("srcPath",null);
        String destPath = (String)body.getOrDefault("destPath",null);
        return nodeService.copyNodeWithPath(srcPath,destPath,true);
    }

    @PostMapping("/moveNodeById")
    public Node moveNodeById(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        String srcId = (String)body.getOrDefault("srcId",null);
        String destId = (String)body.getOrDefault("destId",null);
        return nodeService.copyNodeWithId(srcId,destId,true);
    }

    @GetMapping("/streamContent/{nodeId}")
    public void streamFileContent(@PathVariable String nodeId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.log(INFO, String.format("streaming node content %s %n", nodeId));
        Node node = nodeService.getNodeInfoById(nodeId);
        if(node==null)
            throw new CustomException(NO_NODE_WITH_GIVEN_ID, nodeId);
        response.setHeader("content-type",node.getContentType());
        try {
            FileCopyUtils.copy(node.getContent(), response.getOutputStream());
        } catch (IOException e) {
            logger.log(SEVERE, String.format("Error while streaming file content: %s, Error: %s %n", node.getName(), e.getMessage()));
            throw new CustomException(ERROR_WHILE_STREAMING_FILE_CONTENT, node.getName());
        }
    }

    @GetMapping("/getCompressedFolder/{folderId}")
    public void getCompressedFolder(@PathVariable String folderId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.log(INFO, String.format("streaming zip content %s %n", folderId));
        File zipFile = nodeService.zipFolderContentById(folderId, null);
        try {
            response.setHeader("content-type", Files.probeContentType(zipFile.toPath()));
            FileCopyUtils.copy(Files.readAllBytes(zipFile.toPath()), response.getOutputStream());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Error while streaming file content: %s, Error: %s %n", folderId, e.getMessage()));
            throw new CustomException(ERROR_WHILE_STREAMING_FILE_CONTENT, folderId);
        } finally {
            deleteFile(zipFile);
        }
    }

    @PostMapping("/getCompressedNodes")
    public void getCompressedNodes(@RequestBody List<String> nodesId, HttpServletRequest request, HttpServletResponse response) throws Exception {
        logger.log(INFO, String.format("streaming zip content %s %n", nodesId));
        File zipFile = nodeService.zipNodesById(nodesId, null);
        try {
            response.setHeader("content-type", Files.probeContentType(zipFile.toPath()));
            FileCopyUtils.copy(Files.readAllBytes(zipFile.toPath()), response.getOutputStream());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Error while streaming file content: %s, Error: %s %n", nodesId, e.getMessage()));
            throw new CustomException(ERROR_WHILE_STREAMING_FILE_CONTENT, nodesId);
        }finally {
            deleteFile(zipFile);
        }
    }

    public void deleteFile(File file) throws Exception {
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Error while deleting file: %s, Error: %s%n", file.getName(),e.getMessage()));
            throw new CustomException(ERROR_WHILE_DELETING_FILE, file);
        }
    }
}
