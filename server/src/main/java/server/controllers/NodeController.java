package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.data.Permission;
import server.exceptions.FileStorageException;
import server.services.FsFilesService;
import server.services.NodeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;

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

    @GetMapping("/getNodeInfoById/{fileId}")
    public Node getNodeInfoById(@PathVariable String fileId,  HttpServletRequest request) {
        return nodeService.getNodeInfoById(fileId);
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
    public void streamFileContent(@PathVariable String nodeId, HttpServletRequest request, HttpServletResponse response){
        logger.log(INFO, String.format("streaming node content %s %n", nodeId));
        Node node = nodeService.getNodeInfoById(nodeId);
        try {
            response.setHeader("content-type",node.getContentType());
            FileCopyUtils.copy(node.getContent(), response.getOutputStream());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Could not read node %s %n", node.getName()));
            throw new FileStorageException("Could not read node " + node.getName() + ". Please try again!");
        }
    }

    @GetMapping("/getCompressedFolder/{folderId}")
    public void getCompressedFolder(@PathVariable String folderId, HttpServletRequest request, HttpServletResponse response){
        logger.log(INFO, String.format("streaming zip content %s %n", folderId));
        File zipFile = nodeService.zipFolderContentById(folderId, null);
        try {
            response.setHeader("content-type", Files.probeContentType(zipFile.toPath()));
            FileCopyUtils.copy(Files.readAllBytes(zipFile.toPath()), response.getOutputStream());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Could not read node %s %n", folderId));
            throw new FileStorageException("Could not read node " + folderId + ". Please try again!");
        } finally {
            deleteFile(zipFile);
        }
    }

    @PostMapping("/getCompressedNodes")
    public void getCompressedNodes(@RequestBody List<String> nodesId, HttpServletRequest request, HttpServletResponse response){
        logger.log(INFO, String.format("streaming zip content %s %n", nodesId));
        File zipFile = nodeService.zipNodesById(nodesId, null);
        try {
            response.setHeader("content-type", Files.probeContentType(zipFile.toPath()));
            FileCopyUtils.copy(Files.readAllBytes(zipFile.toPath()), response.getOutputStream());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Could not read node %s %n", nodesId));
            throw new FileStorageException("Could not read node " + nodesId + ". Please try again!");
        }finally {
            deleteFile(zipFile);
        }
    }

    public void deleteFile(File file){
        try {
            Files.deleteIfExists(file.toPath());
        } catch (Exception e) {
            logger.log(SEVERE, String.format("Error while deleting file: %s, Error: %s%n", file.getName(),e.getMessage()));
        }
    }
}
