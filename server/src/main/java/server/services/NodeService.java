package server.services;

import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.data.NodeType;
import server.data.Permission;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.logging.Level.SEVERE;

@Service
public class NodeService {
    private final Logger logger = Logger.getLogger(NodeService.class.getName());
    @Autowired
    private MongoOperations mongoOperations;
    @Autowired
    private FsFilesService fileService;

    /****************** get Nodes ************************/
    public Node getNodeById(String id) {
        if (id == null)
            return null;
        return mongoOperations.findOne(new Query(Criteria.where("_id").is(id)), Node.class);
    }

    public Node getNodeByPath(String path) {
        if (path == null)
            return null;
        path = path.endsWith(Node.SEPARATOR) && !path.equals(Node.ROOT_PATH) ? path.substring(0, path.lastIndexOf(Node.SEPARATOR)) : path;
        Node node = mongoOperations.findOne(new Query(Criteria.where("path").is(path)), Node.class);
        return node;
    }

    public List<Node> getNodesByParentPath(String parentPath) {
        Node parentNode = getNodeByPath(parentPath);
        List<Node> list = parentNode != null ? getNodesByParentId(parentNode.getId()) : new ArrayList<>();
        return list;
    }

    public List<Node> getNodesByParentId(String parentId) {
        if (parentId == null)
            return null;
        return mongoOperations.find(new Query(Criteria.where("parentId").is(parentId)), Node.class);
    }

    public List<Node> getNodesByQuery(Map<String, Object> query) {
        // TODO more details are required here (see later)
        if (query.get("parentId") != null) {
            return getNodesByParentId((String) query.get("parentId"));
        }

        if (query.get("parentPath") != null) {
            return getNodesByParentPath((String) query.get("parentPath"));
        }
        return new ArrayList<>();
    }

    public Node getRoot() {
        // TODO to see later because we can have many roots
        Node root = mongoOperations.findOne(new Query(Criteria.where("path").is(Node.ROOT_PATH)), Node.class);
        if (root != null)
            return root;
        return upsertOneNode(new Node());
    }

    public Node getNodeInfoById(String nodeId) {
        if (nodeId == null)
            return null;
        Node node = getNodeById(nodeId);
        if (node == null)
            return null;
        // setting content + content type
        if (node.isFile()) {
            if (node.getFileId() != null) {
                GridFsResource file = fileService.getFileContent(node.getFileId());
                if (file != null) {
                    node.setContent(file.getContent());
                    String contentType = null;
                    long contentLength = 0;
                    try {
                        contentType = file.getContentType();
                        contentLength = file.contentLength();
                    } catch (Exception e) {
                        logger.log(SEVERE, String.format("Error while extracting info of file %s, Error: %s", node.getName() ,e.getMessage()));
                    }
                    node.setFileSize(contentLength ==0 ? node.getFileSize(): contentLength);
                    node.setContentType(contentType == null ? node.getContentType() : contentType);
                }
            }
        }
        /*// setting children
        if(node.isFolder()){
            List<Node> children = getNodesByParentId(node.getId());
            node.setChildren(children);
        }*/
        // setting parent
        if (node.getParentId() != null) {
            Node parent = getNodeById(node.getParentId());
            node.setParent(parent);
        }
        return node;
    }

    /****************** save Nodes ************************/
    public List<Node> upsertNodes(List<Node> list) {
        return list
                .stream()
                .map(this::upsertOneNode)
                .collect(Collectors.toList());
    }

    public Node upsertOneNode(Node node) {
        if (node == null) {
            return null;
        } else if (node.getParentId() != null) { // setting correct path
            Node parentNode = getNodeById(node.getParentId());
            if (parentNode != null) {
                String path = parentNode.getPath();
                path = concatTwoPaths(path, node.getName());
                node.setPath(path);
            } else {
                return null;
            }
        } else if (node.getPath() != null) { // setting correct parentId
            Node parentNode = getNodeByPath(node.getParentFolderPath());
            if (parentNode != null)
                node.setParentId(parentNode.getId());
        }

        if (node.getId() != null && existsWithId(node.getId())) {
            return updateNode(node);
        } else
            return mongoOperations.save(node);
    }

    public Node updateNode(Node node) {
        if (node.getId() != null && existsWithId(node.getId())) {
            Update update = node.createUpdateObj();
            UpdateResult updateResult = mongoOperations.updateFirst(new Query(Criteria.where("_id").in(node.getId())), update, Node.class);
            if (updateResult.getModifiedCount() > 0) {
                return getNodeById(node.getId());
            }
        }
        return null;
    }

    /****************** delete Nodes ************************/
    public List<String> deleteNodesByIds(List<String> listIds, boolean recursive) {
        if (listIds == null || listIds.size() == 0)
            return new ArrayList<>();
        return listIds
                .stream()
                .filter(nodeId -> deleteNodeById(nodeId, recursive) > 0)
                .collect(Collectors.toList());
    }

    public long deleteNodeByPath(String nodePath, boolean recursive) {
        Node node = getNodeByPath(nodePath);
        if (node == null)
            return 0;
        return deleteNodeById(node.getId(), recursive);
    }

    public long deleteNodeById(String nodeId, boolean recursive) {
        if (nodeId == null)
            return 0;
        Node node = getNodeById(nodeId);
        long count = 0;
        if (node == null)
            return 0;
        if (node.isFile()) {
            count = mongoOperations.remove(new Query(Criteria.where("_id").is(node.getId())), Node.class).getDeletedCount();
            if (count > 0)
                fileService.deleteWithId(nodeId);
        } else if (node.isFolder() && recursive) {
            count = mongoOperations.remove(new Query(Criteria.where("_id").is(node.getId())), Node.class).getDeletedCount();
            if (count > 0) {
                List<Node> children = getNodesByParentId(node.getId());
                count = count + children.stream()
                        .map(child -> deleteNodeById(child.getId(), true))
                        .reduce((acc, co) -> co + acc).orElse(0l);
            }
        }
        // TODO should i fire an exception if recursive=false and node if folder ?!
        return count;
    }

    /****************** permissions ************************/
    // TODO permissions should be in another collection with relations with groups or users
    public boolean hasPermissionById(String nodeId, Object permission) {
        Node node = getNodeById(nodeId);
        return hasPermission(node, Permission.getFrom(permission));
    }

    public boolean hasPermissionByPath(String path, Object permission) {
        Node node = getNodeByPath(path);
        return hasPermission(node, Permission.getFrom(permission));
    }

    public boolean hasPermission(Node node, Permission permission) {
        return node != null && permission != null && ((permission.getCode() & node.getPermission().getCode()) == permission.getCode());
    }

    /****************** exists ************************/
    public boolean existsWithId(String nodeId) {
        return nodeId != null && mongoOperations.exists(new Query(Criteria.where("_id").is(nodeId)), Node.class);
    }

    public boolean existsWithPath(String nodePath) {
        return nodePath != null && mongoOperations.exists(new Query(Criteria.where("path").is(nodePath)), Node.class);
    }

    /****************** create node ************************/
    public Node createFolder(Map<String, Object> metaData) {
        Node parentNode = null;
        if (metaData.get("parentId") != null)
            parentNode = getNodeById((String) metaData.get("parentId"));
        if (parentNode == null && metaData.get("path") != null)
            parentNode = createFolderFromPath((String) metaData.get("path"));
        if (parentNode == null)
            parentNode = getRoot();
        Permission permission = Permission.getFrom(metaData.getOrDefault("permission", parentNode.getPermission()));
        String path = parentNode.getPath();
        String name = (String) metaData.getOrDefault("name", Node.getDefaultFileName());
        name = name.replaceAll("\\.", "_").trim();
        path = concatTwoPaths(path, name);
        Node node = new Node(path, permission, parentNode.getId(), new Date(), NodeType.FOLDER, name, null, null, 0);
        Node oldNode = getNodeByPath(path);
        if (oldNode != null) {
            return oldNode;
        }
        return mongoOperations.save(node);
    }

    public Node createFolderFromPath(String nodePath) {
        if (existsWithPath(nodePath))
            return getNodeByPath(nodePath);
        String[] pathParts = nodePath.split(Node.SEPARATOR);
        Node currentNode = null;
        Node parentNode = getRoot();
        int length = pathParts.length;
        String path = getRoot().getPath();
        for (String pathPart : pathParts) {
            if (pathPart.isEmpty())
                continue;
            currentNode = getNodeByPath(concatTwoPaths(path, pathPart));
            if (currentNode == null) {
                currentNode = new Node(concatTwoPaths(path, pathPart), parentNode.getPermission(), parentNode.getId(), new Date(), NodeType.FOLDER, pathPart, null, null, 0);
                currentNode = upsertOneNode(currentNode);
                if (currentNode == null)
                    continue;
            }
            path = concatTwoPaths(path, pathPart);
            parentNode = currentNode;
        }
        return currentNode;
    }

    public Node createFile(MultipartFile file, Map<String, Object> metaData, boolean upsert) throws Exception {
        Node parentNode = null;
        if (metaData.get("parentId") != null) {
            parentNode = getNodeById((String) metaData.get("parentId"));
        }
        if (parentNode == null && metaData.get("parentPath") != null) {
            parentNode = getNodeByPath((String) metaData.get("parentPath"));
            if (parentNode == null)
                parentNode = createFolderFromPath((String) metaData.get("parentPath"));
        }
        if (parentNode == null)
            parentNode = getRoot();

        String name = (String) metaData.getOrDefault("name", file.getOriginalFilename());
        String path = concatTwoPaths(parentNode.getPath(), name);

        Node nodeInDb = getNodeByPath(path);
        Node node = new Node(
                path,
                Permission.getFrom(metaData.getOrDefault("permission", parentNode.getPermission())),
                parentNode.getId(),
                new Date(),
                NodeType.FILE,
                name,
                null,
                file.getContentType(),
                file.getSize()
        );

        if (nodeInDb != null && upsert) {
            node.setId(nodeInDb.getId());
        } else if (nodeInDb != null) {
            throw new Exception("file already exist with same name");
        }
        ObjectId id = fileService.saveFile(file, node);
        node.setFileId(id.toString());
        return upsertOneNode(node);
    }

   /****************** copy and move node ************************/

    public Node copyNodeWithPath(String srcPath, String destPath, boolean move) throws Exception {
        if (!existsWithPath(srcPath))
            throw new Exception("no node with source path: " + srcPath);
        Node srcNode = getNodeByPath(srcPath);
        Node destNode = getNodeByPath(destPath);
        if (destNode == null) {
            destNode = createFolderFromPath(destPath);
        }
        if (destNode == null)
            throw new Exception("can't create node with destination path: " + destPath);
        destPath = destNode.getPath();

        if (srcNode.isFolder() && destNode.isFolder()) {
            // copy folder
            Node folderCopy = duplicateNodeWithGivenId(srcNode.getId());
            folderCopy.setParentId(destNode.getId());
            folderCopy.setPath(concatTwoPaths(destPath, folderCopy.getName()));
            Node createdFolderCopy = upsertOneNode(folderCopy);
            if (createdFolderCopy == null)
                return null;
            if (move)
                deleteNodeById(srcNode.getId(), false);
            // copy children
            List<Node> children = getNodesByParentId(srcNode.getId());
            Node finalDestNode = createdFolderCopy;
            children.stream().forEach(child -> {
                String dest = concatTwoPaths(finalDestNode.getPath(), child.getName());
                try {
                    copyNodeWithPath(child.getPath(), dest, move);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            return createdFolderCopy;
        } else if (srcNode.isFile()) {
            // copy file
            Node fileCopy = duplicateNodeWithGivenId(srcNode.getId());
            if (destNode.isFolder()) {
                fileCopy.setParentId(destNode.getId());
                fileCopy.setPath(concatTwoPaths(destPath, fileCopy.getName()));
            } else {
                fileCopy.setParentId(destNode.getParentId());
                fileCopy.setPath(concatTwoPaths(destNode.getParentFolderPath(), fileCopy.getName()));
                fileCopy.setId(destNode.getId());
            }
            Node createdFileCopy = upsertOneNode(fileCopy);
            if (createdFileCopy == null)
                return null;
            if (move)
                deleteNodeById(srcNode.getId(), false);
            return createdFileCopy;
        } else {
            throw new Exception(String.format("You are trying to copy  folder %s  to file %s ", srcNode.getPath(), destNode.getPath()));
        }
    }

    public Node copyNodeWithId(String srcId, String destId, boolean move) throws Exception {
        Node srcNode = getNodeById(srcId), destNode = getNodeById(destId);
        if (srcNode != null && destNode != null)
            return copyNodeWithPath(srcNode.getPath(), destNode.getPath(), move);
        return null;
    }

    /****************** duplicate node ************************/
    public String concatTwoPaths(String firstPath, String secondPath) {
        return firstPath +
                (firstPath.endsWith(Node.SEPARATOR) || secondPath.startsWith(Node.SEPARATOR) ? "" : Node.SEPARATOR) +
                secondPath;
    }

    public Node duplicateNodeWithGivenId(String nodeId) {
        Node node = getNodeById(nodeId);
        if (node == null)
            return null;
        node.setId(null);
        return upsertOneNode(node);
    }

    /***************** compress content **********************/
    public InputStream getFileStream(File file){
        //byte[] bytes = new byte[(int) file.length()];
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            //bis.read(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bis;
    }
    public File zipNodesById(List<String> nodesId, String zipPath){
        if(nodesId==null || nodesId.size()==0)
            return null;
        List<Node> children = new ArrayList<>();
        nodesId
                .forEach(id->{
                    Node node = getNodeInfoById(id);
                    if(node!=null)
                        children.add(node);
                });
        if(children.size()==0)
            return null;
        zipPath = zipPath!=null
                ? zipPath
                : children.get(0).getName().replaceAll("\\.","_")+".zip";
        return zipNodes(children, zipPath);
    }

    public File zipFolderContentById(String folderId, String zipPath){
        if(folderId==null)
            return null;
        Node folderNode = getNodeById(folderId);
        if(folderNode==null)
            return null;
        List<Node> children = getNodesByParentId(folderId);
        if(children==null || children.size()==0)
            return null;
        zipPath = zipPath!=null
                ? zipPath
                : folderNode.getName().replaceAll("\\.","_")+".zip";
        return zipNodes(children, zipPath);
    }

    public File zipNodes(List<Node> nodes, String zipPath) {
        try {
            Path path = Paths.get(zipPath);
            if (!Files.exists(path)) {
                path = Files.createFile(path);
            }
            FileOutputStream fos = new FileOutputStream(path.toFile());
            ZipOutputStream zos = new ZipOutputStream(fos);
            zos.setMethod(ZipOutputStream.DEFLATED);
            // get files
            List<ZipEntry> entries =
                    nodes.stream()
                    .filter(Node::isFile)
                    .map(node -> {
                        try {
                            ZipEntry zipEntry = new ZipEntry(node.getName());
                            zos.putNextEntry(zipEntry);
                            byte[] bytes = new byte[(int) node.getFileSize()];
                            BufferedInputStream bis = new BufferedInputStream(node.getContent());
                            bis.read(bytes);
                            zos.write(bytes, 0, bytes.length);
                            zos.closeEntry();
                            return zipEntry;
                        } catch (Exception e) {
                            logger.log(SEVERE,
                                    String.format("Error putting file in the zip : %s, Error: %s %n", node.getName(), e.getMessage())
                            );
                        }
                        return null;
                    })
                    .collect(Collectors.toList());
            zos.close();
            return path.toFile();
        } catch (FileNotFoundException e) {
            logger.log(SEVERE, String.format("Error creating zip file : %s %n", e.getMessage()));
        } catch (IOException e) {
            logger.log(SEVERE, String.format("Error while zipping files : %s %n", e.getMessage()));
        }
        return null;
    }
}
