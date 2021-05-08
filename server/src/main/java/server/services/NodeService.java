package server.services;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import server.data.FileVersion;
import server.data.IUser;
import server.data.NodeType;
import server.exceptions.CustomException;
import server.data.Node;
import server.models.NodeFtpRequest;
import server.models.NodeWebRequest;
import server.models.ZipRequest;
import server.providers.IFileProvider;
import server.providers.INodeProvider;
import server.tools.HashTool;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static server.exceptions.Message.*;
import static server.tools.MimeTypesMap.getMimeType;

@Service
public class NodeService implements INodeService{
    private final Logger logger = LoggerFactory.getLogger(NodeService.class);
    private final int MAX_HISTORY_SIZE = 10;
    private static final Object NODE_CREATION_LOCK = new Object();

    @Autowired
    private INodeProvider nodeProvider;
    @Autowired
    private IFileProvider fileProvider;

    /********** for nodes from web *******************/
    @Override
    public List<Node> getNodes(IUser user, String parentId, int page, int size, String sortField, String direction, List<String> status, String search) throws CustomException {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.Direction.valueOf(direction), sortField);
        List<Node> nodeList =  nodeProvider.getNodesByUserAndParentIdAndName(user.getId(), parentId, search, pageRequest);
        for (Node node : nodeList){
            addParent(node, user);// adding parents
        }
        return nodeList;
    }

    @Override
    public List<Node> getNodes(IUser user, String parentId) throws CustomException {
        PageRequest pageRequest = PageRequest.of(0, 100, Sort.Direction.valueOf("ASC"), "creationDate");
        List<Node> nodeList =  nodeProvider.getNodesByUserAndParentIdAndName(user.getId(), parentId, "", pageRequest);
        for (Node node : nodeList){
            addParent(node, user);// adding parents
        }
        return nodeList;
    }

    @Override
    public Node getNodeById(IUser user, String nodeId) throws CustomException {
        // canGetResource(nodeId, user.getId())
        Node node = nodeProvider.getNodeById(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        return addChildren(addParent(node, user), user);
    }

    @Override
    public Node getNodeWithContent(IUser user, String nodeId) throws CustomException {
        Node node = nodeProvider.getNodeById(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        return addContentWithFail(node);
    }

    @Override
    public Node getNodeWithContent(IUser user, String nodeId, int startPos, int endPos) throws CustomException {
        Node node = nodeProvider.getNodeById(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID,nodeId));
        return addContentWithFail(node, startPos, endPos);
    }

    @Override
    public Node createZipNode(IUser user, ZipRequest zipRequest) throws CustomException {
        String parentId = zipRequest.getParentId();
        // if parent exist update path of node
        Node parentNode = null;
        List<String> path = new ArrayList<>();
        if(parentId != null){
            parentNode = nodeProvider.getNodeById(parentId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
            zipRequest.setParentId(parentId);
            path = parentNode.getPath();
        }
        path.add(zipRequest.getName());
        // creating zip file
        File zipFile = zipNodesByIds(user, zipRequest.getNodesId(), zipRequest.getName());
        // add zip file to zip request in order to make some operation for us
        // like file size and contentType, ...
        zipRequest.updateWithFile(zipFile);
        // creating node
        Node zipNode = new Node(zipRequest.getName(), user.getId(), parentId, path);
        zipNode.setType(NodeType.FILE);
        zipNode.getFields().putAll(zipRequest.getFields());
        zipNode = fixNameConflict(zipNode, user);
        // saving zip file content to database
        String fileId = saveFile(zipNode, zipRequest.getContent());
        if(fileId!=null){
            // adding file version
            String contentHash = HashTool.hash(zipRequest.getContent());
            FileVersion fileVersion = new FileVersion(fileId, user.getId(),
                    zipRequest.getName(), zipRequest.getFileSize(),
                    zipRequest.getContentType(), zipRequest.getExtension(),
                    zipRequest.getFields(), contentHash).hashVersion();
            zipNode.setCurrentFileVersion(fileVersion);
        }
       // saving node
        zipNode = nodeProvider.insertNode(user.getId(), zipNode);
        return zipNode;
    }

    @Override
    public Node insertNode(IUser user, NodeWebRequest nodeWebRequest) throws CustomException {
        String parentId = nodeWebRequest.getParentId();
        Node parentNode;
        List<String> path = new ArrayList<>();
        if(parentId != null){
            parentNode = nodeProvider.getNodeById(parentId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
            nodeWebRequest.setParentId(parentId);
            path = parentNode.getPath();
        }
        path.add(nodeWebRequest.getName());
        nodeWebRequest.setPath(path);
        Node node = NodeWebRequest.toNode(nodeWebRequest);
        // fix name
        node = fixNameConflict(node, user);
        // save file
        String fileId = saveFile(node, nodeWebRequest.getFileContent());
        // adding file version
        if(fileId!=null){
            String contentHash = HashTool.hash(nodeWebRequest.getContent());
            FileVersion fileVersion = new FileVersion(fileId, user.getId(),
                    nodeWebRequest.getName(), nodeWebRequest.getFileSize(),
                    nodeWebRequest.getContentType(), nodeWebRequest.getExtension(),
                    nodeWebRequest.getFields(), contentHash).hashVersion();
            node.setCurrentFileVersion(fileVersion);
        }
        try {
            Node createdNode = nodeProvider.insertNode(user.getId(), node);
            return addParent(createdNode, user); // just more details
        }catch (org.springframework.dao.DuplicateKeyException e){
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), user.getId(), parentId);
        }catch (Exception e){
            throw new CustomException(e, UNKNOWN_EXCEPTION, node.getName(), user.getId(), parentId);
        }
    }

    @Override
    public Node updateNode(IUser user, String nodeId, NodeWebRequest nodeWebRequest) throws CustomException {
        Node node = nodeProvider.getNodeById(nodeId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_ID, nodeId));
        String parentId = nodeWebRequest.getParentId();
        Node parentNode;
        List<String> path = new ArrayList<>();
        if(parentId != null){
            parentNode = nodeProvider.getNodeById(parentId, user.getId()).orElseThrow(()->new CustomException(NO_NODE_WITH_GIVEN_PARENT_ID,parentId));
            nodeWebRequest.setParentId(parentId);
            path = parentNode.getPath();

        }
        path.add(nodeWebRequest.getName());
        nodeWebRequest.setPath(path);
        NodeWebRequest.updateNodeWith(node, nodeWebRequest);
        // save file
        String fileId = saveFile(node, nodeWebRequest.getFileContent());
        // adding file version
        if(fileId!=null){
            FileVersion currentVersion = node.getCurrentFileVersion();
            String contentHash = HashTool.hash(nodeWebRequest.getContent());
            FileVersion fileVersion = new FileVersion(
                    fileId,
                    user.getId(),
                    getValueOr(nodeWebRequest.getName(), currentVersion.getOriginalName()),
                    Optional.of(nodeWebRequest.getFileSize()).orElse(currentVersion.getFileSize()),
                    getValueOr(nodeWebRequest.getContentType(), currentVersion.getContentType()),
                    getValueOr(nodeWebRequest.getExtension(), currentVersion.getExtension()),
                    getValueOr(nodeWebRequest.getFields(), currentVersion.getFields()),
                    contentHash
            ).hashVersion();
            // set current version in history
            if(
                    currentVersion.getFileId()!=null // file is not null
                    && !currentVersion.getVersionHash().equals(fileVersion.getVersionHash()) // and was updated
            ){
                node.getVersions().add(node.getCurrentFileVersion());
                if(node.getVersions().size()>MAX_HISTORY_SIZE)
                    node.getVersions().remove(0);
            }
            // set current version
            node.setCurrentFileVersion(fileVersion.hashVersion());
        }
        try {
            Node createdNode = nodeProvider.saveNode(user.getId(), node);
            return addParent(createdNode, user);
        }catch (org.springframework.dao.DuplicateKeyException e){// catch duplicate key exception
            throw new CustomException(e, NODE_ALREADY_EXISTS_WITH_SAME_KEYS, node.getName(), user.getId(), parentId);
        }catch (Exception e){
            throw new CustomException(e, UNKNOWN_EXCEPTION, node.getName(), user.getId(), parentId);
        }
    }

    @Override
    public int deleteNode(IUser user, String nodeId) throws CustomException {
        // just counting before and after deleting
        int count = nodeProvider.countById(nodeId, user.getId());
        nodeProvider.deleteById(nodeId, user.getId());
        return count - nodeProvider.countById(nodeId, user.getId());
    }

    @Override
    public List<Node> getRootNodes(IUser user) throws CustomException {
        return nodeProvider.getByOwnerIdAndParentId(user.getId(), null);
    }

    /********** for nodes from ftp server *******************/
    @Override
    public Node insertNode(NodeFtpRequest nodeFtpRequest) throws CustomException {
        IUser user = nodeFtpRequest.getUser();
        // build file node and add it to list
        Node node = getNodeFromPath(user, nodeFtpRequest.getPath(), true);
        if(node != null){
            NodeFtpRequest.updateNodeWith(node, nodeFtpRequest);
            //node = fixNameConflict(node);
            // save file
            String fileId = saveFile(node, nodeFtpRequest.getContent());
            // build Version
            if(fileId!=null){
                FileVersion currentVersion = node.getCurrentFileVersion();
                String contentHash = HashTool.hash(nodeFtpRequest.getContent());
                FileVersion fileVersion = new FileVersion(
                        fileId,
                        user.getId(),
                        getValueOr(nodeFtpRequest.getName(), currentVersion.getOriginalName()),
                        Optional.of(nodeFtpRequest.getFileSize()).orElse(currentVersion.getFileSize()),
                        getValueOr(nodeFtpRequest.getContentType(), currentVersion.getContentType()),
                        getValueOr(nodeFtpRequest.getExtension(), currentVersion.getExtension()),
                        getValueOr(nodeFtpRequest.getFields(), currentVersion.getFields()),
                        contentHash).hashVersion();
                // set current version in history
                if(currentVersion.getFileId()!=null && !currentVersion.getVersionHash().equals(fileVersion.getVersionHash())){
                    node.getVersions().add(node.getCurrentFileVersion());
                    if(node.getVersions().size()>MAX_HISTORY_SIZE)
                        node.getVersions().remove(0);
                }
                // set current version
                node.setCurrentFileVersion(fileVersion);
            }
            node = nodeProvider.saveNode(user.getId(), node);
            logger.info(node.toString());
            return node;
        }
        throw new CustomException(ERROR_WHILE_SAVING_FILE, nodeFtpRequest.getName());
    }

    @Override
    public Node updateNode(NodeFtpRequest nodeFtpRequest) throws CustomException {
        return insertNode(nodeFtpRequest);
    }

    @Override
    public int deleteNode(NodeFtpRequest nodeFtpRequest) throws CustomException {
        IUser user = nodeFtpRequest.getUser();
        Node node = getNodeFromPath(user, nodeFtpRequest.getPath(), false);
        if(node != null){
            deleteNode(user, node.getId());
            return 1;
        }
        return 0;
    }

    @Override
    public Node getNodeFromPath(IUser user, List<String> pathParts, boolean allowInsert) throws CustomException {
        synchronized (NODE_CREATION_LOCK){
            String parentId = null;
            Node createdNode = null;
            pathParts = pathParts.stream().filter(Objects::nonNull).collect(Collectors.toList());
            for (int i = 0 ; i < pathParts.size(); i++){
                String nodeName = pathParts.get(i);
                Node foundedNode = nodeProvider.getByNameAndOwnerIdAndParentId(nodeName, user.getId(), parentId);
                if(foundedNode==null){
                    if(!allowInsert)
                        return null;
                    List<String> path = i==0 ? Collections.singletonList(nodeName) : pathParts.subList(0,i);
                    createdNode = new Node(nodeName, user.getId(), parentId, path);
                    createdNode = nodeProvider.insertNode(user.getId(), createdNode);
                }else{
                    createdNode = foundedNode;
                }
                parentId = createdNode.getId();
                //pathNodes.add(createdNode);
            }
            return createdNode;
        }
    }

    /********** Tools *******************/
    public File zipNodesByIds(IUser user, List<String> nodesId, String zipName) throws CustomException {
        if(nodesId==null)
            throw new CustomException(NULL_PARAMS, "nodesId");
        List<Node> children = new ArrayList<>();
        for (String id: nodesId){
            Node node = getNodeById(user, id);
            if(node!=null)
                children.add(node);
        }
        children = children.stream()
                .filter(nodeNew -> NodeType.FILE.equals(nodeNew.getType()))
                .map(this::addContent)
                .collect(Collectors.toList());
        if(children.size()==0)
            throw new CustomException(EMPTY_RESULT, nodesId);
        return zipNodes(user, children, zipName);
    }

    private File zipNodes(IUser user, List<Node> nodes, String zipName) throws CustomException {
        try {
            if(zipName==null)
                zipName = nodes.get(0).getName();
            zipName = zipName.replaceAll(".zip","");
            String userDir = user.getUsername()+"_"+user.getId();
            Path path = Paths.get("uploads",userDir,user.getHomeDirectory(),zipName+".zip").normalize();
            if (!Files.exists(path)) {
                path = Files.createTempFile(userDir+"_","_"+zipName+".zip");//Files.createFile(path);
            }
            List<ZipEntry> entries = new ArrayList<>();
            try(
                    FileOutputStream fos = new FileOutputStream(path.toFile());
                    ZipOutputStream zos = new ZipOutputStream(fos)
            ){
                zos.setMethod(ZipOutputStream.DEFLATED);
                for (Node node : nodes) {
                    try {
                        String entryName = node.getName();
                        if(entryName.contains(".")){// validate extension
                            String extension = FilenameUtils.getExtension(node.getName());
                            if(getMimeType(extension)==null)
                                entryName = entryName.substring(0, entryName.lastIndexOf(".")).replaceAll("\\.","_");
                        }
                        if(!entryName.contains(".")){
                            entryName +='.'+node.getExtension();
                        }
                        if(!entryName.contains(".")){
                            entryName +='.'+FilenameUtils.getExtension(node.getOriginalName());
                        }
                        ZipEntry zipEntry = new ZipEntry(entryName);
                        zos.putNextEntry(zipEntry);
                        byte[] bytes = new byte[(int) node.getFileSize()];
                        try(BufferedInputStream bis = new BufferedInputStream(node.getContent())) {
                            int readBytes = bis.read(bytes);
                            if(readBytes>0)
                                zos.write(bytes, 0, bytes.length);
                        }finally {
                            zos.closeEntry();
                            entries.add(zipEntry);
                        }
                    } catch (Exception e) {
                        logger.error( String.format("Error inserting file in the zip : %s, Error: %s %n", node.getName(), e.getMessage()));
                    }
                }

            }
            if(entries.size()==0)
                throw new CustomException(ERROR_WHILE_ZIPPING_FILE, path);
            return path.toFile();
        } catch (FileNotFoundException e) {
            logger.error(String.format("Error while zipping, Error: %s %n", e.getMessage()));
            throw new CustomException(e, ZIP_FILE_PATH_NOT_FOUND, e.getMessage());
        } catch (IOException e) {
            logger.error(String.format("Error while zipping, Error: %s %n",e.getMessage()));
            throw new CustomException(e, ERROR_WHILE_ZIPPING_FILE, e.getMessage());
        } catch (Exception e) {
            logger.error(String.format("Error while zipping nodes Error: %s %n", e.getMessage()));
            throw new CustomException(e, UNKNOWN_EXCEPTION, e.getMessage());
        }
    }

    public String saveFile(Node node, InputStream inputStream) throws CustomException {
        if(inputStream!=null){
            node.setType(NodeType.FILE);
            try (InputStream inputStream1 = inputStream){
                return fileProvider.saveFile(inputStream1, node.getName(), node.getContentType(), node);
            }catch (Exception e){
                throw new CustomException(e, ERROR_WHILE_SAVING_FILE, node.getName());
            }
        }
        return null;
    }

    public String saveFile(Node node, byte[] bytes) throws CustomException {
        if(bytes!=null && bytes.length>0){
            try (InputStream in = new ByteArrayInputStream(bytes)){
                return saveFile(node, in);
            }catch (Exception e){
                throw new CustomException(e, ERROR_WHILE_SAVING_FILE, node.getName());
            }
        }
        return null;
    }

    public Node addChildren(Node node, IUser user) throws CustomException {
        // add children if exist
        List<Node> children = nodeProvider.getByOwnerIdAndParentId(user.getId(), node.getId());
        node.setChildren(children);
        return node;
    }

    public Node addParent(Node node, IUser user) throws CustomException {
        // add parent if exist
        if(node.getParentId()==null)
            return node;
        Node parent = nodeProvider.getNodeById(node.getParentId(), user.getId()).orElse(null);
        node.setParent(parent);
        return node;
    }

    public Node addContent(Node node){
        try {
            return addContentWithFail(node);
        } catch (Exception e) {
            logger.warn(e.getMessage());
        }
        return node;
    }

    public Node addContentWithFail(Node node) throws CustomException {
        if(node.getFileId()==null)
            return node;
        InputStream inputStream = fileProvider.getInputStream(node.getFileId());
        node.setContent(inputStream);
        return node;
    }

    public Node addContentWithFail(Node node, int startPos, int endPos) throws CustomException {
        if(node.getFileId()==null)
            return node;
        InputStream inputStream = fileProvider.getInputStream(node.getFileId(), startPos, endPos);
        node.setContent(inputStream);
        return node;
    }

    public Node getWithSameKey(Node node, IUser user) throws CustomException {
        return nodeProvider.getByNameAndOwnerIdAndParentId(node.getName(), user.getId(), node.getParentId());
    }

    public Node fixNameConflict(Node node, IUser user) throws CustomException {
        Node nodeWithSameKey = getWithSameKey(node, user);
        if(nodeWithSameKey!=null){
            String oldName = node.getName();
            String generatedName = oldName+"_"+System.currentTimeMillis();
            if(oldName.contains(".")){
                int index = oldName.lastIndexOf(".");
                generatedName = oldName.substring(0,index)+"_"+System.currentTimeMillis()+oldName.substring(index);
            }
            node.setName(generatedName);
        }
        return node;
    }

    public String getValueOr(String object, String defaultObj){
        return object==null || "".equals(object) ? defaultObj : object;
    }

    public Map getValueOr(Map object, Map defaultObj){
        return object==null || object.isEmpty() ? defaultObj : object;
    }

}
